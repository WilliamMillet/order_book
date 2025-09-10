"""
Tests for market orders. Also use limit orders to setup the order book
"""
from order_book.types import Market
from order_book.order import Order, OrderSide, OrderType

def regular_market_order(mkt: Market):
    """
    Test a bid and order of the same quantity and price match correctly
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]

    initial_offer = Order( OrderSide.SELL, OrderType.LIMIT, john, 150, 10.00)
    eng.place_order(initial_offer)

    assert book.best_offer() == initial_offer

    initial_bid = Order(OrderSide.BUY, OrderType.MARKET, jane, 150, 10.00)
    eng.place_order(initial_bid)

    # Since bid and offer match they should no longer be on the order book
    assert book.best_offer() is None
    assert book.best_bid() is None

    # Check the inverse way (Order made before bid)
    offer_2 = Order(OrderSide.BUY, OrderType.LIMIT, john, 75)
    eng.place_order(offer_2)
    assert book.best_bid() == offer_2

    bid_2 = Order(OrderSide.SELL, OrderType.MARKET, jane, 75)
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
    offer1 = Order(OrderSide.SELL, OrderType.LIMIT, john, 150, 999999999)
    eng.place_order(offer1)

    # No price specified
    bid1 = Order(OrderSide.BUY, OrderType.MARKET, jane, 150)
    eng.place_order(bid1)

    assert book.best_offer() is None
    assert book.best_bid() is None





# Test best order is chosen (for BUY and SELL), check time is used as tie breaker