
from uuid import uuid4

class Trader:
    def __init__(self, name: str) -> None:
        self.id = uuid4()
        self.name = name
