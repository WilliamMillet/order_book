package market.matching;

import java.time.LocalDateTime;
import java.util.List;

import market.Trade;
import market.orders.FOKOrder;
import market.orders.MarketOrder;
import market.orders.Order;

public class MatchResultBuilder {
    private final MatchResult res;
    private boolean isBuilt = false;

    /**
     * Start building a match result with information we can determine before matching is performed
     * @param order the order insertion of which the match result is relevant to
     */
    public MatchResultBuilder(Order order) {
        MatchResult res = new MatchResult();

        res.setOrderId(order.getOrderId());
        res.setSide(order.getSide());
        res.setFilledVolume(0);
        res.setRemainingVolume(order.getVolume());
        res.setAvgMatchPrice(MatchResult.getNoMatches());
        res.setTimestamp(LocalDateTime.now());
        res.setNote("");

        this.res = res;
    }

    /**
     * Fill in all the unset match result fields after matching has been performed
     * @param incoming the order attempting to enter the order book
     * @param trades the trades that were made in the matching process
     */
    public void finalise(Order incoming, List<Trade> trades) {
        res.setStatus(getOrderStatus(incoming, trades));
        res.setFilledVolume(res.getRemainingVolume() - incoming.getVolume());
        res.setRemainingVolume(incoming.getVolume());
        res.setAvgMatchPrice(getAverageTradePrice(trades));
        res.setTrades(trades);
        
        isBuilt = true;
    }

    /**
     * Attach a note to the match result
     * @param note the string note to add
     */
    public void attachNote(String note) {
        res.setNote(note);
    }

    /**
     * Get the complete match result
     * @return the constructed MatchResult object
     */
    public MatchResult getResult() {
        if (!isBuilt) {
            throw new IllegalStateException("Cannot get match result before finalising the result");
        }

        return res;
    }
    /**
     * Determine the status of an order from an order's type, volume and the list of trades.
     * @param incoming the order attempting to enter the order book
     * @param trades the trades that were made in the matching process
     * @return the status of the order after matching
     */
    private OrderStatus getOrderStatus(Order incoming, List<Trade> trades) {
        if (incoming.getVolume() == 0) {
            return OrderStatus.FILLED;
        }

        if (MarketOrder.class.isInstance(incoming) || MarketOrder.class.isInstance(incoming)) {
            if (trades.size() > 0) {
                return OrderStatus.PARTIAL_REJECTION;
            } else {
                return OrderStatus.ALL_REJECTED;
            }
        } else if (FOKOrder.class.isInstance(incoming)) {
            return OrderStatus.ALL_REJECTED;
        } else {
            if (trades.size() > 0) {
                return OrderStatus.PARTIAL_RESTING;
            } else {
                return OrderStatus.ALL_RESTING;
            }
        }
    }

    /**
     * Get the average trade price amongst a list of trades
     * @param trades the trades that occurred in the matching process
     * @return the average price of the trades or NO_MATCHES if there were no matches
     */
    private static double getAverageTradePrice(List<Trade> trades) {
        if (trades.size() > 0) {
            return trades
                .stream()
                .mapToDouble(t -> t.price() * t.volume())
                .sum() / trades.stream().mapToInt(Trade::volume).sum();
        } else {
            return MatchResult.getNoMatches();
        }
    }

}
