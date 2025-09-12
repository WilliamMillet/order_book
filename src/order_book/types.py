"""Types, dataclasses, enums etc that do not belong to a specific module"""

from dataclasses import dataclass
from uuid import UUID
from order_book.order_book import OrderBook
from order_book.trader import Trader
from order_book.matching_engine import MatchingEngine

@dataclass
class Market:
    book: OrderBook
    eng: MatchingEngine
    traders: list[Trader]