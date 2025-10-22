package market.orders.strategies;

public class MarketOrderPriceAcceptanceStrategy implements PriceAcceptanceStrategy {
    @Override
    public boolean acceptsPrice(double price) {
        // Market orders will match all orders regardless of price
        return true;
    }
}
