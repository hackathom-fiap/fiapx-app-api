from fastapi import FastAPI, UploadFile, File, Depends, HTTPException, status, Request
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel
from sqlalchemy.orm import Session
import os
from datetime import timedelta
from typing import List
import time

# Prometheus Metrics
from prometheus_client import generate_latest, Counter, Histogram
from starlette.responses import Response

# Import layers
import models
import auth
import boto3 # Import boto3
from database import engine, get_db
from src.domain.entities import User as UserEntity
from src.adapters.db.repositories import PostgresUserRepository, PostgresVideoRepository
from src.adapters.messaging.producer import RabbitMQProducer
from src.adapters.storage import S3FileStorage
from src.adapters.cache.redis_cache import RedisVideoCache
from src.use_cases.register_user import RegisterUserUseCase
from src.use_cases.upload_video import UploadVideoUseCase
from src.use_cases.list_videos import ListVideosUseCase
from src.use_cases.list_users import ListUsersUseCase
from jose import JWTError, jwt

# Init DB Tables
models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="FIAP X Video Processor Gateway (Clean Arch)")

# Prometheus Metrics initialization
REQUEST_COUNT = Counter(
    "http_requests_total", "Total HTTP requests", ["method", "endpoint", "status_code"]
)
REQUEST_DURATION = Histogram(
    "http_request_duration_seconds", "HTTP request duration", ["method", "endpoint"]
)

# Middleware for metrics
@app.middleware("http")
async def add_process_time_header(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    process_time = time.time() - start_time
    
    REQUEST_DURATION.labels(
        method=request.method, endpoint=request.url.path
    ).observe(process_time)
    REQUEST_COUNT.labels(
        method=request.method, endpoint=request.url.path, status_code=response.status_code
    ).inc()
    
    return response

# Prometheus Metrics endpoint
@app.get("/metrics")
def metrics():
    return Response(content=generate_latest(), media_type="text/plain")

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

# Dependency Injection Setup
RABBITMQ_URL = os.getenv("RABBITMQ_URL", "amqp://user:password@rabbitmq:5672/")

# Obtém as variáveis de ambiente individuais do Redis
REDIS_USER = os.getenv("REDIS_USER", "default")
REDIS_PASSWORD = os.getenv("REDIS_PASSWORD", "password")
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = os.getenv("REDIS_PORT", "6379")

# Constrói a URL de conexão do Redis (rediss:// para SSL/Transit Encryption)
REDIS_URL = f"rediss://{REDIS_USER}:{REDIS_PASSWORD}@{REDIS_HOST}:{REDIS_PORT}/0"

S3_BUCKET_NAME = os.getenv("S3_BUCKET_NAME")

class UserCreate(BaseModel):
    username: str
    email: str
    password: str

class UserResponse(BaseModel):
    id: int
    username: str
    email: str # Assuming email is part of UserEntity, even if not explicitly used in UserCreate

    class Config:
        from_attributes = True # for Pydantic v2 or orm_mode = True for v1

def get_current_user_entity(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)) -> UserEntity:
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, auth.SECRET_KEY, algorithms=[auth.ALGORITHM])
        username: str = payload.get("sub")
        if username is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
        
    repo = PostgresUserRepository(db)
    user = repo.get_by_username(username)
    if user is None:
        raise credentials_exception
    return user

@app.post("/register")
def register(user_data: UserCreate, db: Session = Depends(get_db)):
    repo = PostgresUserRepository(db)
    use_case = RegisterUserUseCase(repo, auth.get_password_hash)
    try:
        use_case.execute(user_data.username, user_data.email, user_data.password)
        return {"message": f"User {user_data.username} criado com sucesso!"}
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail=str(e))

@app.post("/token")
def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    repo = PostgresUserRepository(db)
    user = repo.get_by_username(form_data.username)
    
    if not user or not auth.verify_password(form_data.password, user.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Usuario ou senha incorretos. Verifique seu username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    access_token_expires = timedelta(minutes=auth.ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = auth.create_access_token(
        data={"sub": user.username}, expires_delta=access_token_expires
    )
    return {"access_token": access_token, "token_type": "bearer"}

@app.post("/upload")
async def upload_video(
    file: UploadFile = File(...), 
    current_user: UserEntity = Depends(get_current_user_entity),
    db: Session = Depends(get_db)
):
    video_repo = PostgresVideoRepository(db)
    broker = RabbitMQProducer(RABBITMQ_URL, "video_processing")
    storage = S3FileStorage(S3_BUCKET_NAME)
    
    use_case = UploadVideoUseCase(video_repo, broker, storage)
    
    video = use_case.execute(file.file, file.filename, current_user)
    
    return {
        "id": video.id,
        "filename": video.original_name,
        "status": video.status
    }

@app.get("/status")
def get_status(current_user: UserEntity = Depends(get_current_user_entity), db: Session = Depends(get_db)):
    video_repo = PostgresVideoRepository(db)
    cache = RedisVideoCache(REDIS_URL)
    use_case = ListVideosUseCase(video_repo, cache)
    videos = use_case.execute(current_user)

    if not videos:
        return {"message": "Não há vídeos para download."}
    
    s3_client = boto3.client('s3')
    response_data = []
    
    for v in videos:
        presigned_url = None
        if v.zip_path:
            try:
                # Extract key from S3 URL: https://bucket.s3.amazonaws.com/key
                # Simple split by bucket name to get the key
                key = v.zip_path.split(f"https://{S3_BUCKET_NAME}.s3.amazonaws.com/")[-1]
                if key:
                    presigned_url = s3_client.generate_presigned_url(
                        'get_object',
                        Params={'Bucket': S3_BUCKET_NAME, 'Key': key},
                        ExpiresIn=3600 # URL valid for 1 hour
                    )
            except Exception as e:
                print(f"Error generating presigned URL: {e}")
                
        response_data.append({
            "id": v.id,
            "original_name": v.original_name,
            "status": v.status,
            "created_at": v.created_at,
            "zip_url": presigned_url
        })
    
    return response_data

@app.get("/users", response_model=List[UserResponse])
def list_users(db: Session = Depends(get_db), current_user: UserEntity = Depends(get_current_user_entity)):
    repo = PostgresUserRepository(db)
    use_case = ListUsersUseCase(repo)
    users = use_case.execute()
    return users

@app.get("/")
async def root():
    return {"message": "FIAP X Video Processor API Gateway is running with Clean Architecture"}

