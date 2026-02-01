from typing import List
from src.domain.entities import User
from src.ports.interfaces import UserRepository

class ListUsersUseCase:
    def __init__(self, user_repository: UserRepository):
        self.user_repository = user_repository

    def execute(self) -> List[User]:
        return self.user_repository.list_all()
