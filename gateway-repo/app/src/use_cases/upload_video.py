from src.domain.entities import Video, User
from src.ports.interfaces import VideoRepository, MessageBroker, FileStorage
import uuid
import os

class UploadVideoUseCase:
    def __init__(self, video_repo: VideoRepository, broker: MessageBroker, storage: FileStorage):
        self.video_repo = video_repo
        self.broker = broker
        self.storage = storage

    def execute(self, file_obj, original_filename: str, user: User) -> Video:
        if user.id is None:
            raise ValueError("O usuário deve estar logado e possuir um ID para fazer upload.")

        # 1. Save file
        ext = os.path.splitext(original_filename)[1]
        unique_filename = f"{uuid.uuid4()}{ext}"
        self.storage.save(file_obj, unique_filename)

        # 2. Create DB Record
        video = Video(
            filename=unique_filename,
            original_name=original_filename,
            user_id=user.id
        )
        saved_video = self.video_repo.create(video)

        # 3. Publish message
        if saved_video.id is None:
            raise ValueError("Falha ao salvar o vídeo no banco de dados, ID não gerado.")
            
        self.broker.publish_video_processing(saved_video.id, unique_filename)

        return saved_video
