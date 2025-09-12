"""
Module contains tests for limit order specific functionality and general order
book tests. General order book tests are done here as limit orders are a
simple type of order which allows for efficient testing.
"""

from order_book.types import Market
from order_book.order import Order, OrderSide, OrderType


# def test_simple_limit_order_pair(mkt: Market):
#     """
#     Test limit orders match and the order book is cleared when the two orders
#     of opposing types have the same volume and price.
#     """
#     book, eng, traders = mkt.book, mkt.eng, mkt.traders
#     john, jane = traders[0:2]

#     bid1 = Order(OrderSide.BUY, OrderType.LIMIT, john, 100, 10.00)
#     eng.place_order(bid1)

#     assert book.best_bid() == bid1

#     offer1 = Order(OrderSide.SELL, OrderType.LIMIT, jane, 100, 10.00)
#     eng.place_order(offer1)
    
#     assert book.best_bid() is None
#     assert book.best_offer() is None

# def test_order_outside_of_limit_not_matched(mkt: Market):
#     """
#     Test that match candidates outside of the limit range are not matched
#     """
#     book, eng, traders = mkt.book, mkt.eng, mkt.traders
#     john, jane = traders[0:2]

#     bid1 = Order(OrderSide.BUY, OrderType.LIMIT, john, 100, 10.00)
#     eng.place_order(bid1)

#     # Limit should be to high for previous offer to match
#     offer1 = Order(OrderSide.SELL, OrderType.LIMIT, jane, 100, 15.00)
#     eng.place_order(offer1)

#     print(book.best_bid())
#     print(book.best_offer())

#     assert book.best_bid() == bid1
#     assert book.best_offer() == offer1

#     # Next order of $11.99 also should not match (It will be at the top as well
#     # since 11.99 > 10
#     bid2 = Order(OrderSide.BUY, OrderType.LIMIT, john, 100, 11.99)
#     eng.place_order(bid2)

#     assert book.best_bid() == bid2


# def test_match_volume_gt_order(mkt: Market):
#     """
#     Test the result of an incoming order having a volume less than the order
#     that it matches with.
#     """
#     book, eng, traders = mkt.book, mkt.eng, mkt.traders
#     john, jane = traders[0:2]

#     offer = Order(OrderSide.SELL, OrderType.LIMIT, john, 75, 10)
#     eng.place_order(offer)

#     bid = Order(OrderSide.BUY, OrderType.LIMIT, jane, 50, 10)
#     eng.place_order(bid)
    
#     best_offer = book.best_offer()
#     assert best_offer is not None and best_offer.volume == 25
#     assert book.best_bid() is None 

# def test_match_volume_lt_total_volume(mkt: Market):
#     """
#     Test the result of an incoming order having a higher volume than the order
#     it matches with (when there is only one order to match with)
#     """
#     book, eng, traders = mkt.book, mkt.eng, mkt.traders
#     john, jane = traders[0:2]

#     offer = Order(OrderSide.SELL, OrderType.LIMIT, john, 50, 10)
#     eng.place_order(offer)

#     bid = Order(OrderSide.BUY, OrderType.LIMIT, jane, 75, 10)
#     eng.place_order(bid)

#     assert book.best_offer() is None
#     best_bid = book.best_bid()
#     assert best_bid is not None and best_bid.volume == 25

# def test_match_volume_lt_order(mkt: Market):
#     """
#     Test the result of an incoming order having a higher volume than the order
#     it matches with, but not the total volume of orders that can be matched with
#     """
#     book, eng, traders = mkt.book, mkt.eng, mkt.traders
#     john, jane, jack = traders[0:3]

#     offer1 = Order(OrderSide.SELL, OrderType.LIMIT, john, 50, 10)
#     offer2 = Order(OrderSide.SELL, OrderType.LIMIT, jane, 50, 10)
#     eng.place_order(offer1)
#     eng.place_order(offer2)

#     bid = Order(OrderSide.BUY, OrderType.LIMIT, jack, 70, 10)
#     eng.place_order(bid)

#     best_offer = book.best_offer()
#     assert best_offer is not None and best_offer.volume == 30
#     assert book.best_bid() is None
