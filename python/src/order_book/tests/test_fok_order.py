"""
Tests specific to fill or kill (FOK) orders
"""

from order_book.types import Market
from order_book.order import Order, OrderSide, OrderType
from order_book.matching_engine import OrderStatus
from order_book.constants import NO_MATCH

def test_successful_fok(mkt: Market):
    """
    Test an FOK order where all volume is met immediately returns the correct
    order and affects the order book correctly.
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]

    bid = Order(OrderSide.BUY, OrderType.LIMIT, john, 100, 10.00)
    eng.place_order(bid)
    
    offer = Order(OrderSide.SELL, OrderType.FILL_OR_KILL, jane, 100, 9.00)
    offer_res = eng.place_order(offer)
    
    assert book.is_empty()
    
    assert offer_res.order_type == OrderType.FILL_OR_KILL
    assert offer_res.status == OrderStatus.FILLED
    assert offer_res.side == OrderSide.SELL
    assert offer_res.filled_volume == 100
    assert offer_res.remaining_volume == 0
    assert offer_res.avg_match_price == 10.00
    assert offer_res.note == ""
    
    assert len(offer_res.trades) == 1
    trade = offer_res.trades[0]
    assert trade.price == 10.00
    assert trade.volume == 100
    
def test_failed_fok(mkt: Market):
    """
    Test that when a FOK order cannot be met the order book the entire order
    is cancelled and the order book is returned to it's previous state
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]
    
    offer1 = Order(OrderSide.SELL, OrderType.LIMIT, john, 60, 8.00)
    offer2 = Order(OrderSide.SELL, OrderType.LIMIT, john, 40, 10.00) 
    eng.place_orders([offer1, offer2])

    bid = Order(OrderSide.BUY, OrderType.FILL_OR_KILL, jane, 80, 9.00)
    bid_res = eng.place_order(bid)

    assert bid_res.status == OrderStatus.ALL_REJECTED
    assert bid_res.filled_volume == 0
    assert bid_res.remaining_volume == 80
    assert bid_res.avg_match_price == NO_MATCH
    assert len(bid_res.trades) == 0 
    assert "liquidity" in bid_res.note

    assert book.best_bid() is None
    assert book.num_offers() == 2
    best = book.best_offer()
    assert best is not None
    assert best.volume == 60
    assert best.price == 8.00
    