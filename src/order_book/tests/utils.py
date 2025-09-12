"""Utilities for making testing easier"""
from typing import TypeVar

T = TypeVar("T")

class OfType[T]:
    """
    Any instance of this will be equal to any variable of the type that is
    passed in the constructor
    """
    def __init__(self, equal_type: type[T]) -> None:
        self._equal_type = equal_type

    def __eq__(self, other) -> bool:
        return isinstance(other, self._equal_type)