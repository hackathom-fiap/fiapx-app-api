import shutil
import os
import boto3
from boto3.session import Config # Add this import
from src.ports.interfaces import FileStorage

class LocalFileStorage(FileStorage):
    def __init__(self, base_path: str):
        self.base_path = base_path
        os.makedirs(os.path.join(self.base_path, "uploads"), exist_ok=True)

    def save(self, file_obj, filename: str) -> str:
        file_path = os.path.join(self.base_path, "uploads", filename)
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file_obj, buffer)
        return file_path

class S3FileStorage(FileStorage):
    def __init__(self, bucket_name: str):
        self.s3_client = boto3.client('s3', config=Config(use_ssl=True))
        self.bucket_name = bucket_name

    def save(self, file_obj, filename: str) -> str:
        s3_key = f"uploads/{filename}"
        print(f"Uploading file to S3: {self.bucket_name}/{s3_key}")
        # Use upload_fileobj for efficient memory usage with large files
        self.s3_client.upload_fileobj(file_obj, self.bucket_name, s3_key)
        return f"s3://{self.bucket_name}/{s3_key}"
