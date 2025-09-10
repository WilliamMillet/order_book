# Trader (dataclass)
## Properties
- `name`: str
- `id`: str (UUID)

# Order
##  Properties
- `id`: str (UUID) 
- `price`: float | None
- `volume`: int (Buy/sell quantity)
- `order_side`: OrderSide,
- `order_type`: OrderType,
- `trader_int`: str

# OrderBook
## Properties
- `bids`: list[Order] (Max heap)
- `offers`: list[Order] (Min heap)
## Methods
- `def best_bid(self) -> Order`
- `def best_offer(self) -> Offer`
- `def cancel_order(id: str) Cancel an order with a given id, throws an error

# MatchingEngine
## Properties
- `book`: Orderbook
## Methods
- `def place_order(self, Order)`
- `def _process_market_order(self, Order)
- `def _process_limit_order(self, Order)
- `def _process_FOK_order(self, Order)
- `def _process_IOC_order(self, Order)
- `def _process_quote_order(self, Order)


# Enums
- **OrderSide** [BUY, SELL]
- **OrderType** [MARKET, LIMIT_ORDER, FILL_OR_KILL, IMMEDIATE_OR_CANCEL, QUOTE]


To do 
- comment the basic business logic for each order type processor