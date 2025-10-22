package market.orders.strategies;

import market.orders.OrderSide;

public class LimitedOrderPriceAcceptanceStrategy implements PriceAcceptanceStrategy {
    private final double limit;
    private final OrderSide side;

    public LimitedOrderPriceAcceptanceStrategy(double limit, OrderSide side) {
        this.limit = limit;
        this.side = side;
    }
    
    @Override
    public boolean acceptsPrice(double price) {
        if (side == OrderSide.BUY) {
            return limit >= price;
        } else {
            return limit <= price;
        }
    }
}
