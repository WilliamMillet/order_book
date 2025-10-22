import pytest
from order_book.order_book import OrderBook
from order_book.trader import Trader
from order_book.matching_engine import MatchingEngine
from order_book.types import Market

@pytest.fixture
def mkt() -> Market:
    """A simple market do conduct tests with"""
    book = OrderBook()
    eng = MatchingEngine(book)

    names = ["John", "Jane", "Jack", "Dave", "Mike", "Sally"]
    traders = [Trader(name) for name in names]

    return Market(book, eng, traders)

