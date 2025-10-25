package market.orders;

import market.trader.Trader;

public final class FOKOrder extends PricedOrder {
    public FOKOrder(OrderSide side, Trader trader, int volume, double price) {
        super(side, trader, volume, price);
    }

    @Override
    public boolean canRestInBook() {
        return true;
    }
}
