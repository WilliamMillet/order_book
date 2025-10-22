package market.orders;

import market.Trader;
import market.orders.strategies.PriceAcceptanceStrategy;
import market.orders.strategies.LimitedOrderPriceAcceptanceStrategy;

public final class IOCOrder extends Order {
    private double price;
    private final PriceAcceptanceStrategy paStrategy;

    public IOCOrder(OrderSide side, Trader trader, int volume, double price) {
        super(side, trader, volume);

        OrderValidator.validatePrice(price);
        this.price = price;
        paStrategy = new LimitedOrderPriceAcceptanceStrategy(price, side);
    }
    
    @Override
    public boolean isInPriceLimit(double price) {
        return paStrategy.acceptsPrice(price);
    }

    @Override
    public boolean canRestInBook() {
        return true;
    }

    public double getPrice() {
        return price;
    }
}
