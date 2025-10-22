# enum OrderSide
- BUY
- SELL

# public abstract sealed Order permits MarketOrder, LimitOrder, FOCOrder, IOCOrder 
Properties
- side: OrderSide
- traderId: UUID
- volume: int
- timestamp: LocalDateTime
- orderId: : UUID
Methods
+ canRestInBook(): boolean [abstract]
+ isPriceInLimit(Order): boolean [abstract]
+ inverseSide(): OrderSide

# public final MarketOrder extends Order
- PriceAcceptanceStrategy pa_strat = MarketOrderPriceAcceptanceStrategy()
Methods

# public final LimitOrder extends Order
Properties
- price: double
- PriceAcceptanceStrategy pa_strat = LimitedOrderPriceAcceptanceStrategy()
Methods

# public final FOCOrder extends Order
Properties
- price: double
- PriceAcceptanceStrategy pa_strat = LimitedOrderPriceAcceptanceStrategy()
Methods

# public final IOCOrder extends Order
Properties
- price: double
- PriceAcceptanceStrategy pa_strat = LimitedOrderPriceAcceptanceStrategy()
Methods

# public final class OrderValidator
Methods
- OrderValidator (Should not be called)
+ validateVolume
+ validatePrice

# interface PriceAcceptanceStrategy
Methods
+ inLimit(): boolean

# public class MarketOrderPriceAcceptanceStrategy implements PriceAcceptanceStrategy
Methods
+ inLimit { return true } (always true for this)

# public class LimitedOrderPriceAcceptanceStrategy implements PriceAcceptanceStrategy
Properties
- orderSide: OrderSide
- limit: int

# MarketService
Property
- book: OrderBook
- eng: MatchingEngine
- traders: List<Trader>
Method
+ MarketService() (Instantiate the market and put the book inside the engine)

# enum OrderStatus
- PENDING
- ALL_REJECTED
- ALL_RESTING
- PARTIAL_REJECTION
- PARTIAL_RESTING
- FILLED

# public class MatchResult
Properties
    - orderId: UUID
    - side: OrderSide
    - note: String
    - filledVolume: int
    - remainingVolume: int
    - avgMatchPrice: double
    - timestamp: LocalDateTime
    - status: OrderStatus
    - trades: List<Trade>
Methods
    +setOrderId(UUID): void
    +setSide(OrderSide): void
    +setNote(String): void
    +setFilledVolume(int): void
    +setRemainingVolume(int): void
    +setAvgMatchPrice(double): void
    +setTimestamp(LocalDateTime): void
    +setStatus(OrderStatus): void
    +setTrades(List<Trade>): void
    +getOrderId(): UUID
    +getSide(): OrderSide
    +getNote(): String
    +getFilledVolume(): int
    +getRemainingVolume(): int
    +getAvgMatchPrice(): double
    +getTimestamp(): LocalDateTime
    +getStatus(): OrderStatus
    +getTrades(): List<Trade>


# public class MatchResultBuilder
Methods
+MatchResultBuilder(orderId: UUID, side: orderSide, note: String, filledVolume: int, remainingVolume: int, avgMatchPrice: double, timestamp: LocalDateTime, status: OrderStatus)
+finalse(incoming: Order, trades: List<Trade>)
+getResult()
-orderStatus(Order, List<Trade>): OrderStatus
-avgTradePrice(trades: List<Trade>): double

# public class MatchingEngine
Properties
-book: OrderBook
Methods
+MatchingEngine(Orderbook)
+placeOrder(Order): MatchResult
+placeOrders(List<Order>): List<MatchResult>
+placeQuote(Quote)
-processMarketOrder(MarketOrder)
-processLimitOrder(LimitOrder): MatchResult
-processFOCOrder(FOCOrder): MatchResult
-processIOCOrder(IOCOrder): MatchResult
-handleMismatchedVolumes(Order, Order): Trade
-insertOrders(List<Order>): void

# public class Orderbook
Properties
-orderComparator: Comparator<Order>
-bids: PriorityQueue
-offers: PriorityQueue
Methods:
+ best_order(OrderSide): Order
+ insertRestingOrder(Order): void
+ cancelOrder(UUID, OrderSide): void
+ amendOrderVolume(UUID, OrderSide, int)
+ tradeTop(Order, int): Trade
- getOrderIdx(UUID, OrderSide): int

# public record Trade
Properties
- offererId: UUID
- bidderId: UUID
- price: float
- volume: int

# public record Trader
Properties
- id: UUID
- name: UUIDs  