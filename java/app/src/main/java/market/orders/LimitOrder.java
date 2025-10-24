package market.orders;

import market.Trader;
public final class LimitOrder extends PricedOrder {
    public LimitOrder(OrderSide side, Trader trader, int volume, double price) {
        super(side, trader, volume, price);
    }

    @Override
    public boolean canRestInBook() {
        return true;
    }
}
