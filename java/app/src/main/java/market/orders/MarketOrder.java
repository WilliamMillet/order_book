package market.orders;

import market.orders.strategies.MarketOrderPriceAcceptanceStrategy;
import market.orders.strategies.PriceAcceptanceStrategy;
import market.trader.Trader;

public final class MarketOrder extends Order {
    private final PriceAcceptanceStrategy paStrategy = new MarketOrderPriceAcceptanceStrategy();

    public MarketOrder(OrderSide side, Trader trader, int volume) {
        super(side, trader, volume);
    }

    @Override
    public boolean isInPriceLimit(double price) {
        return paStrategy.acceptsPrice(price);
    }

    @Override
    public boolean canRestInBook() {
        return false;
    }
}
