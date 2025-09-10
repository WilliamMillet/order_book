from datetime import datetime
from enum import Enum
import uuid

from order_book.trader import Trader

# We use -1 as a sentinel to notate no price has been provided. This
# price cannot be set under normal terms
NO_PRICE = -1.0

class OrderValueError(ValueError):
    pass

class OrderSide(Enum):
    BUY = "BUY"
    SELL = "SELL"

class OrderType(Enum):
    MARKET = "MARKET"
    LIMIT = "LIMIT"
    FILL_OR_KILL = "FOK"
    IMMEDIATE_OR_CANCEL = "IOC"

class Order:
    def __init__(
        self,
        order_side: OrderSide,
        order_type: OrderType,
        trader: Trader,
        volume: int,
        price: float = NO_PRICE
        ) -> None:
        
        Order.validate_price(price)
        Order.validate_volume(volume)

        if not price and order_type != OrderType.MARKET:
            raise OrderValueError("Non-market order must contain a price")
        
        self.price = price
        self.volume = volume
        self.order_side = order_side
        self.order_type = order_type
        self.trader_id = trader.id
        self.timestamp = datetime.now()

        self.order_id = uuid.uuid4()
    
    @property
    def inverse_side(self) -> OrderSide:
        """Get the order side that the order should be matched with"""
        if self.order_side == OrderSide.BUY:
            return OrderSide.SELL
        elif self.order_side == OrderSide.SELL:
            return OrderSide.BUY
        else:
            raise OrderValueError("Order must be of side BUY or SELL")
    
    def is_price_in_limit(self, price: float) -> bool:
        """Determine if a given price is within the limit for an order"""
        if self.price is NO_PRICE:
            # Any price is within the limit if the price field is empty
            return True
        if self.order_side == OrderSide.BUY:
            return self.price >= price
        else:
            return self.price <= price
        
    @staticmethod
    def validate_price(price: float) -> None:
        """Raise an error if price is invalid"""
        if price != NO_PRICE and price <= 0:
            raise OrderValueError(
                f"Invalid order price ${price:.2f}. Price must be greater than 0"
            )

    @staticmethod   
    def validate_volume(volume: int) -> None:
        """Raise an error if the volume is invalid"""
        if volume <= 0:
            raise OrderValueError(
                f"Invalid order volume ${volume}. Volume must be greater than 0"
            )
        if not isinstance(volume, int):
            raise OrderValueError(
                f"Invalid order volume ${volume}. Volume must be an integer"
            )
        
    
    def __lt__(self, other) -> bool:
        """
        Less-than method designed such that higher priority orders (high bids
        and low offers) are considered less than the contrary. This is because
        orders will interacted with via heapq which only supports min heaps.
        
        Since a price time order book is being implemented time is used as a
        tie breaker (early placed is more prioritised and thus lower)
        """

        if (self.order_side != other.order_side):
            raise ValueError("Cannot compare orders of different sides")

        if self.price == other.price:
            return self.timestamp < other.timestamp
        
        if self.order_side == OrderSide.BUY:
            # Higher bid prices are 'better' so they are considered less
            # than in our min heaps
            return self.price > other.price
        else:
            return self.price < other.price 

class Quote():
    def __init__(self, bid: Order, offer: Order):
        if bid.order_side != OrderSide.BUY:
            raise OrderValueError("Quote bid must be a buy side order")
        if offer.order_side != OrderSide.SELL:
            raise OrderValueError("Quote offer must be a sell side order")

        self.bid = bid
        self.offer = offer