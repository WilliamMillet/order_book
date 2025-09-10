import pytest
from dataclasses import dataclass

from order_book.matching_engine import MatchingEngine
from order_book.order import Order, OrderSide, OrderType
from order_book.order_book import OrderBook
from order_book.trader import Trader

@dataclass
class Market:
    book: OrderBook
    eng: MatchingEngine
    traders: list[Trader]


@pytest.fixture
def mkt() -> Market:
    """A simple market do conduct tests with"""
    book = OrderBook()
    eng = MatchingEngine(book)

    names = ["John", "Jane", "Jack", "Dave", "Mike", "Sally"]
    traders = [Trader(name) for name in names]

    return Market(book, eng, traders)


def test_perfect_match(mkt: Market):
    """
    Test a bid and order of the same quantity and price match correctly
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]

    initial_offer = Order(10.00, 150, OrderSide.SELL, OrderType.MARKET, john)
    eng.place_order(initial_offer)

    assert book.best_offer() == initial_offer

    initial_bid = Order(10.00, 150, OrderSide.BUY, OrderType.MARKET, jane)
    eng.place_order(initial_bid)

    # Since bid and offer match they should no longer be on the order book
    assert book.best_offer() is None
    assert book.best_bid() is None

    # Check the inverse way (Order made before bid)

    offer_2 = Order(None, 75, OrderSide.BUY, OrderType.MARKET, john)
    eng.place_order(offer_2)
    assert book.best_bid() == offer_2

    bid_2 = Order(None, 75, OrderSide.SELL, OrderType.MARKET, jane)
    eng.place_order(bid_2)

    assert book.best_offer() is None
    assert book.best_bid() is None


def test_extreme_price_difference_allowed(mkt: Market):
    """
    Market orders should execute at the best price, even if this
    price is a terrible price
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]

    assert book.best_offer() is None
    assert book.best_bid() is None

    # Extremely high price
    offer1 = Order(1000000000, 150, OrderSide.SELL, OrderType.MARKET, john)
    eng.place_order(offer1)

    # No price specified
    bid1 = Order(None, 150, OrderSide.BUY, OrderType.MARKET, jane)
    eng.place_order(bid1)

    assert book.best_offer() is None
    assert book.best_bid() is None

def test_match_volume_gt_order(mkt: Market):
    """
    Test the result of an incoming order having a volume less than the order
    that it matches with.
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]

    offer = Order(None, 75, OrderSide.SELL, OrderType.MARKET, john)
    eng.place_order(offer)

    bid = Order(None, 50, OrderSide.BUY, OrderType.MARKET, jane)
    eng.place_order(bid)
    
    assert book.best_offer().volume == 25 
    assert book.best_bid() is None 

def test_match_volume_lt_total_volume(mkt: Market):
    """
    Test the result of an incoming order having a higher volume than the order
    it matches with (when there is only one order to match with)
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]

    offer = Order(10, 50, OrderSide.SELL, OrderType.MARKET, john)
    eng.place_order(offer)

    bid = Order(10, 75, OrderSide.BUY, OrderType.MARKET, jane)
    eng.place_order(bid)

    assert book.best_offer() is None
    assert book.best_bid().volume == 25

def test_match_volume_lt_order(mkt: Market):
    """
    Test the result of an incoming order having a higher volume than the order
    it matches with, but not the total volume of orders that can be matched with
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane, jack = traders[0:3]

    offer1 = Order(None, 50, OrderSide.SELL, OrderType.MARKET, john)
    offer2 = Order(None, 50, OrderSide.SELL, OrderType.MARKET, jane)
    eng.place_order(offer1)
    eng.place_order(offer2)

    bid = Order(None, 70, OrderSide.BUY, OrderType.MARKET, jack)
    eng.place_order(bid)

    assert book.best_offer().volume == 30
    assert book.best_bid() is None





# Test best order is chosen (for BUY and SELL), check time is used as tie breaker