import pytest
from order_book.order import Order, OrderSide, OrderType, NO_PRICE
from order_book.trader import Trader

def test_inverse_side():
    """Test the inverse_side property method"""
    john = Trader("john")

    bid = Order(OrderSide.BUY, OrderType.FILL_OR_KILL, john, 12, 12)
    offer = Order(OrderSide.SELL, OrderType.FILL_OR_KILL, john, 12, 12)

    assert bid.inverse_side == OrderSide.SELL
    assert offer.inverse_side == OrderSide.BUY

@pytest.mark.parametrize("price", [0.01, 10, 12.3, NO_PRICE])
def test_validate_price_with_valid_prices(price):
    Order.validate_price(price)

@pytest.mark.parametrize("price", [0, -2, -3.8])
def test_validate_price_with_invalid_prices(price):
    with pytest.raises(ValueError):
        Order.validate_price(price)

@pytest.mark.parametrize("volume", [1, 2, 120, 20000000000000])
def test_validate_volume_with_valid_volume(volume):
    Order.validate_volume(volume)

@pytest.mark.parametrize("volume", [-10, 0, 1.2])
def test_validate_volume_with_invalid_volume(volume):
    with pytest.raises(ValueError):
        Order.validate_volume(volume)

def test_compare_orders():
    john = Trader("john")

    bid1 = Order(OrderSide.BUY, OrderType.FILL_OR_KILL, john, 12, 25)
    bid2 = Order(OrderSide.BUY, OrderType.FILL_OR_KILL, john, 5, 20)
    bid3 = Order(OrderSide.BUY, OrderType.FILL_OR_KILL, john, 10, 20)
    bid4 = Order(OrderSide.BUY, OrderType.FILL_OR_KILL, john, 6, 15)
    bid5 = Order(OrderSide.BUY, OrderType.FILL_OR_KILL, john, 1, 12)

    # This should be the sorted order
    bids = [bid1, bid2, bid3, bid4, bid5]

    # Sorting should not change the order if it is already sorted
    assert sorted(bids) == bids

        # Fix order price validation logic and add tests to order validation"