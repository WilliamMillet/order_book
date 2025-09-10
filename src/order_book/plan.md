# This was the original strategy bt but the plans have changed a lot over time

<!-- # Trader (dataclass)
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
 -->

To remember when I start working on this later
- The current thing I'm working on is trying to get some sort of response after a trade. Right now you do not have info on the result of your trade. This should be fixed to give detailed info on each trade made, the price, the volume, the traders you traded with etc. We can use this for pandas analysis later
- Do tests for fill or kill, immediate or cancel and quotes


Possible resources
- Technical notes on order books https://gist.github.com/halfelf/db1ae032dc34278968f8bf31ee999a25