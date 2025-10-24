package market.orders;

import java.time.LocalDateTime;
import java.util.UUID;

import market.Trader;

/**
 * An order to buy or sell on the market
 */
public abstract sealed class Order permits MarketOrder, PricedOrder {
    private final UUID orderId;
    private final UUID traderId;
    private final OrderSide side;
    private final LocalDateTime timestamp; 
    private int volume;


    public Order(OrderSide side, Trader trader, int volume) {
        OrderValidator.validateVolume(volume);

        this.orderId = UUID.randomUUID();
        this.traderId = trader.getId();
        this.side = side;
        this.timestamp = LocalDateTime.now();
        this.volume = volume;   
    }

    /**
     * Get the opposite order side
     * @return SELL if the current side is BUY, else BUY
     */
    public OrderSide getInverseSide() {
        if (side == OrderSide.BUY) {
            return OrderSide.SELL;
        } else {
            return OrderSide.BUY;
        }
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getTraderId() {
        return traderId;
    }

    public OrderSide getSide() {
        return side;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        OrderValidator.validateVolume(volume);
        
        this.volume = volume;
    }

    /**
     * Determine if an order can rest in the order book
     * @return true if the order can rest in the order book, false otherwise
     */
    public abstract boolean canRestInBook();

    /**
     * Determine if the price of an order is acceptable when matching with the order this is composed within 
     * @param price the candidate order price to match with
     * @return true if the price is not an issue, false otherwise
     */
    public abstract boolean isInPriceLimit(double price);
}

