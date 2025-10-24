package market.matching;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import market.Trade;
import market.orders.OrderSide;

/**
 * The immediate result of attempting to insert into the order book
 */
public class MatchResult {
    private UUID orderId;
    private OrderSide side;
    private String note;
    private int filledVolume;
    private int remainingVolume;
    private double avgMatchPrice;
    private LocalDateTime timestamp;
    private OrderStatus status;
    private List<Trade> trades;
    private final static int NO_MATCHES = -1;

    public MatchResult() {
        this.trades = new ArrayList<>();
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public void setNote(String note) {
        if (note.length() > 250) {
            throw new IllegalArgumentException("Match result note cannot be greater then 250 characters");
        }
        this.note = note;
    }

    public void setFilledVolume(int filledVolume) {
        this.filledVolume = filledVolume;
    }

    public void setRemainingVolume(int remainingVolume) {
        this.remainingVolume = remainingVolume;
    }

    public void setAvgMatchPrice(double avgMatchPrice) {
        this.avgMatchPrice = avgMatchPrice;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setTrades(List<Trade> trades) {
        this.trades = (trades == null) ? new ArrayList<>() : new ArrayList<>(trades);
    }

    public UUID getOrderId() {
        return orderId;
    }

    public OrderSide getSide() {
        return side;
    }

    public String getNote() {
        return note;
    }

    public int getFilledVolume() {
        return filledVolume;
    }

    public int getRemainingVolume() {
        return remainingVolume;
    }

    public double getAvgMatchPrice() {
        return avgMatchPrice;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<Trade> getTrades() {
        return List.copyOf(trades);
    }

    public static int getNoMatches() {
        return NO_MATCHES;
    }
}
