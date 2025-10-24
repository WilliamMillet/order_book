package market.orders;

import market.Trader;
import market.orders.strategies.LimitedOrderPriceAcceptanceStrategy;
import market.orders.strategies.PriceAcceptanceStrategy;

public sealed abstract class PricedOrder extends Order permits LimitOrder, FOKOrder, IOCOrder {
    private double price;
    private final PriceAcceptanceStrategy paStrategy;

    public PricedOrder(OrderSide side, Trader trader, int volume, double price) {
        super(side, trader, volume);

        OrderValidator.validatePrice(price);
        this.price = price;
        paStrategy = new LimitedOrderPriceAcceptanceStrategy(price, side);
    }
    
    @Override
    public boolean isInPriceLimit(double price) {
        return paStrategy.acceptsPrice(price);
    }

    public double getPrice() {
        return price;
    }
}
