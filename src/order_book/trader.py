
import uuid

class Trader():
    def __init__(self, name: str) -> None:
        self.id = uuid.uuid4
        self.name = name
