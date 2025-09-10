import pytest
from order_book.order import Order, OrderSide, OrderType
from order_book.trader import Trader

def test_inverse_side():
    """Test the inverse_side property method"""
    john = Trader("john")

    bid = Order(12, 12, OrderSide.BUY, OrderType.FILL_OR_KILL, john)
    offer = Order(12, 12, OrderSide.SELL, OrderType.FILL_OR_KILL, john)

    assert bid.inverse_side == OrderSide.SELL
    assert offer.inverse_side == OrderSide.BUY

@pytest.mark.parametrize("price", [0.01, 10, 12.3, None])
def test_validate_price_with_valid_prices(price):
    Order.validate_price(price)

@pytest.mark.parametrize("price", [0, -1, -3.8])
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

    bid1 = Order(25, 12, OrderSide.BUY, OrderType.FILL_OR_KILL, john)
    bid2 = Order(20, 5, OrderSide.BUY, OrderType.FILL_OR_KILL, john)
    bid3 = Order(20, 10, OrderSide.BUY, OrderType.FILL_OR_KILL, john)
    bid4 = Order(15, 6, OrderSide.BUY, OrderType.FILL_OR_KILL, john)
    bid5 = Order(12, 1, OrderSide.BUY, OrderType.FILL_OR_KILL, john)

    # This should be the sorted order
    bids = [bid1, bid2, bid3, bid4, bid5]

    # Sorting should not change the order if it is already sorted
    assert sorted(bids) == bids

        # Fix order price validation logic and add tests to order validation"