package market.matching;

import java.util.ArrayList;
import java.util.List;

import market.OrderBook;
import market.Trade;
import market.orders.FOKOrder;
import market.orders.IOCOrder;
import market.orders.LimitOrder;
import market.orders.MarketOrder;
import market.orders.Order;

public class MatchingEngine {
    private OrderBook book;

    public MatchingEngine(OrderBook book) {
        this.book = book;
    }

    /**
     * Process an order by matching it with other orders in the order book and or inserting some of the volume int othe order
     * book when applicable
     * @param order The order to insert
     * @return The immediate result of the matching process
     */
    public MatchResult placeOrder(Order order) {
        if (MarketOrder.class.isInstance(order)) {
            return processMarketOrder(order);
        } else if (LimitOrder.class.isInstance(order)) {
            return processLimitOrder(order);
        } else if (FOKOrder.class.isInstance(order)) {
            return processFOKOrder(order); 
        } else if (IOCOrder.class.isInstance(order)) {
            return processIOCOrder(order);
        } else {
            throw new IllegalArgumentException("Matching engine does not support order of type '" + order.getClass().toString()
             + "'");
        }
    }

    /**
     * Process a list of orders via repeated calls on the regular placeOrder method. Insertion
     * occurs in the same order as the iterable is ordered.
     * @param orders the list of orders to insert
     * @return a list of the immediate results of the matching process
     */
    public List<MatchResult> placeOrders(List<Order> orders) {
        return orders.stream().map(o -> placeOrder(o)).toList();
    }

    /**
     * Immediately match an order to the best bid/offer available. If there are no orders to match with raise a liquidity error
     * @param order
     * @return
     */
    public MatchResult processMarketOrder(Order order) {
        MatchResultBuilder mrBuilder = new MatchResultBuilder(order);
        List<Trade> trades = new ArrayList<>();

        
    }
    public MatchResult processLimitOrder(Order order) { return null; }
    public MatchResult processFOKOrder(Order order) { return null; }
    public MatchResult processIOCOrder(Order order) { return null; }
}
