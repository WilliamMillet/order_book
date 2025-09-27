"""
Tests specific to fill or kill (FOK) orders
"""

from order_book.types import Market
from order_book.order import Order, OrderSide, OrderType
from order_book.matching_engine import OrderStatus
from order_book.constants import NO_MATCH


def test_fully_met_ioc(mkt: Market):
    """
    Test that when all volume in an IOC goes through it affects the market
    correctly and returns teh correct result
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]

    bid = Order(OrderSide.BUY, OrderType.LIMIT, john, 100, 10.00)
    eng.place_order(bid)

    offer = Order(OrderSide.SELL, OrderType.IMMEDIATE_OR_CANCEL,
                  jane, 100, 8.00)

    offer_res = eng.place_order(offer)

    assert book.is_empty()

    assert offer_res.order_type == OrderType.IMMEDIATE_OR_CANCEL
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


def test_partially_fulfilled_ioc(mkt: Market):
    """
    Test that the return value and side effects of a partially fulfilled
    IOC value are met
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]

    offer1 = Order(OrderSide.SELL, OrderType.LIMIT, john, 60, 8.00)
    offer2 = Order(OrderSide.SELL, OrderType.LIMIT, john, 40, 10.00)
    eng.place_orders([offer1, offer2])

    bid = Order(OrderSide.BUY, OrderType.IMMEDIATE_OR_CANCEL, jane, 80, 9.00)
    bid_res = eng.place_order(bid)

    assert bid_res.status == OrderStatus.PARTIAL_REJECTION
    assert bid_res.filled_volume == 60
    assert bid_res.remaining_volume == 20
    assert bid_res.avg_match_price == 8.00
    assert len(bid_res.trades) == 1

    assert book.best_bid() is None
    assert book.num_offers() == 1
    best = book.best_offer()
    assert best is not None
    assert best.volume == 40
    assert best.price == 10.00

def test_fully_rejected_ioc(mkt: Market):
    """
    Test the results of an IOC order where no volume can be fulfilled
    """
    book, eng, traders = mkt.book, mkt.eng, mkt.traders
    john, jane = traders[0:2]
    
    bid = Order(OrderSide.BUY, OrderType.LIMIT, john, 60, 1.00)
    eng.place_order(bid)
    
    offer = Order(OrderSide.SELL, OrderType.IMMEDIATE_OR_CANCEL, jane, 60, 8.00)
    offer_res = eng.place_order(offer)
    
    assert book.best_offer() is None
    assert book.num_bids() == 1
    
    assert offer_res.status == OrderStatus.ALL_REJECTED
    assert offer_res.filled_volume == 0
    assert offer_res.remaining_volume == 60
    assert offer_res.avg_match_price == NO_MATCH
    assert len(offer_res.trades) == 0