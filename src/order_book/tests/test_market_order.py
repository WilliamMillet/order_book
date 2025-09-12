"""
Tests for market orders. Also use limit orders to setup the order book
"""
from datetime import datetime
from order_book.types import Market
from order_book.order import Order, OrderSide, OrderType
from order_book.matching_engine import OrderStatus
from order_book.constants import NO_MATCH

def test_regular_mkt_order(mkt: Market):
    """
    Test a bid and order of the same quantity and price match correctly
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]

    offer1 = Order( OrderSide.SELL, OrderType.LIMIT, john, 150, 10.00)
    eng.place_order(offer1)

    bid1 = Order(OrderSide.BUY, OrderType.MARKET, jane, 150)
    bid1_res = eng.place_order(bid1)

    # Since bid and offer match they should no longer be on the order book
    assert book.best_offer() is None
    assert book.best_bid() is None

    # In depth checks for MatchResult
    assert bid1_res.status == OrderStatus.FILLED
    assert bid1_res.order_id == bid1.order_id
    assert bid1_res.side == OrderSide.BUY
    assert bid1_res.note == ""
    assert bid1_res.filled_volume == 150
    assert bid1_res.remaining_volume == 0
    assert bid1_res.avg_match_price == 10.00
    assert isinstance(bid1_res.timestamp, datetime)
    assert len(bid1_res.trades) == 1

    # Check the inverse way (Order made before bid)
    offer2 = Order(OrderSide.BUY, OrderType.LIMIT, john, 75)
    eng.place_order(offer2)
    assert book.best_bid() == offer2

    bid2 = Order(OrderSide.SELL, OrderType.MARKET, jane, 75)
    bid2_res = eng.place_order(bid2)

    assert bid2_res.side == OrderSide.SELL

    assert book.best_offer() is None
    assert book.best_bid() is None

def test_mkt_order_when_low_liquidity(mkt: Market):
    """
    Check that a market order does not go through when their are no other
    trades for it to match with
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john = traders[0]
    # Purposely do NOT add any limit orders to the book
    
    bid1 = Order(OrderSide.BUY, OrderType.MARKET, john, 150)
    bid1_res = eng.place_order(bid1)

    # Only test fields that should be different from our standard test before
    assert bid1_res.status == OrderStatus.ALL_REJECTED
    # Just testing liquidity because I don't want to couple the test too much
    # with the implementation
    assert "liquidity" in bid1_res.note
    assert bid1_res.filled_volume == 0
    assert bid1_res.remaining_volume == 150
    assert bid1_res.avg_match_price == NO_MATCH
    assert len(bid1_res.trades) == 0

    assert book.best_bid() is None

def test_mkt_order_partial_fill(mkt: Market):
    """
    Test that when only some of the volume for a market order can be filled,
    this will be handled correctly
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]

    offer1 = Order(OrderSide.SELL, OrderType.LIMIT, john, 30, 20.14)
    eng.place_order(offer1)
    offer2 = Order(OrderSide.SELL, OrderType.LIMIT, john, 70, 15.12)
    eng.place_order(offer2)

    exp_weighted_avg = (30 * 20.14 + 70 * 15.12) / (30 + 70)

    bid1 = Order(OrderSide.BUY, OrderType.MARKET, jane, 120)
    bid1_res = eng.place_order(bid1)
    
    assert bid1_res.status == OrderStatus.PARTIAL_REJECTION
    assert bid1_res.note == "Insufficient liquidity to match order fully"
    assert bid1_res.filled_volume == 100
    assert bid1_res.remaining_volume == 20
    assert bid1_res.avg_match_price == exp_weighted_avg
    assert len(bid1_res.trades) == 2


def test_extreme_price_difference_allowed(mkt: Market):
    """
    Market orders should execute at the best price, even if this
    price is a terrible price
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]

    # Extremely high price
    offer1 = Order(OrderSide.SELL, OrderType.LIMIT, john, 150, 999999999)
    eng.place_order(offer1)

    # No price specified
    bid1 = Order(OrderSide.BUY, OrderType.MARKET, jane, 150)
    eng.place_order(bid1)

    assert book.best_offer() is None
    assert book.best_bid() is None





# Test best order is chosen (for BUY and SELL), check time is used as tie breaker