package market.orders;

import market.trader.Trader;

public final class IOCOrder extends PricedOrder {
    public IOCOrder(OrderSide side, Trader trader, int volume, double price) {
        super(side, trader, volume, price);
    }

    @Override
    public boolean canRestInBook() {
        return true;
    }
}
