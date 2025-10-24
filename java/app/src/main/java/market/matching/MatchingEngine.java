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
import market.orders.PricedOrder;

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
            return processMarketOrder((MarketOrder) order);
        } else if (LimitOrder.class.isInstance(order)) {
            return processLimitOrder((LimitOrder) order);
        } else if (FOKOrder.class.isInstance(order)) {
            return processFOKOrder((FOKOrder) order); 
        } else if (IOCOrder.class.isInstance(order)) {
            return processIOCOrder((IOCOrder) order);
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
     * @param incoming the order to process
     * @return the immediate match result
     */
    public MatchResult processMarketOrder(MarketOrder incoming) {
        MatchResultBuilder matchResBuilder = new MatchResultBuilder(incoming);
        List<Trade> trades = new ArrayList<>();

        while (incoming.getVolume() > 0) {
            PricedOrder best = book.getBestOrder(incoming.getSide());
            if (best == null) {
                matchResBuilder.attachNote("Insufficient liquidity to match order fully");
                break;
            } else {
                Trade trade = handleMismatchedVolumes(incoming, best);
                trades.add(trade);
            }
        }

        matchResBuilder.finalise(incoming, trades);
        return matchResBuilder.getResult();
    }

    public MatchResult processLimitOrder(LimitOrder incoming) {
        MatchResultBuilder matchResBuilder = new MatchResultBuilder(incoming);
        List<Trade> trades = new ArrayList<>();

        while (incoming.getVolume() > 0) {
            PricedOrder best = book.getBestOrder(incoming.getSide());
            if (best == null || incoming.isInPriceLimit(best.getPrice())) {
                book.insertRestingOrder(incoming);
                break;
            } else {
                Trade trade = handleMismatchedVolumes(incoming, best);
                trades.add(trade);
            }
        }

        matchResBuilder.finalise(incoming, trades);
        return matchResBuilder.getResult();
    }

    public MatchResult processFOKOrder(FOKOrder incoming) {
            MatchResultBuilder matchResBuilder = new MatchResultBuilder(incoming);
        List<Trade> trades = new ArrayList<>();
    }
    public MatchResult processIOCOrder(IOCOrder incoming) { return null; }

    /**
     * Resolve a partial match between the incoming order current best order.
     * 
     * If an order gets to this function it's assumed it's price limit is compatible with the best limit.
     * @param incoming the order being processed
     * @param best the best candidate for the incoming order to be matched with
     * @return the trade that occurred
     */
    private Trade handleMismatchedVolumes(Order incoming, Order best) {
        Trade trade;
        if (best.getVolume() <= incoming.getVolume()) {
            int newVol = incoming.getVolume() - best.getVolume();
            incoming.setVolume(newVol);
            trade = book.tradeTop(incoming, best.getVolume());
            
        } else {
            int volToTrade = Math.min(best.getVolume(), incoming.getVolume());
            trade = book.tradeTop(null, volToTrade);
            int newVol = incoming.getVolume() - volToTrade;
            
            incoming.setVolume(newVol);
        }
        
        return trade;
    }

    /**
     * Take a list of orders and inserts them into the order book. Note that I tested this in reverse and normal sorted order.
     * Iterating in reverse seems to have no effect or a negative effect on time efficiency. Also, inserting into the order book
     * is a distinct action from making a trade, as we don't check if there is an order to match with. It is assumed this has
     * been done already.
     * @param orders the orders to insert
     */
    private void insertOrders(List<PricedOrder> orders) {
        for (PricedOrder order : orders) {
            book.insertRestingOrder(order);
        }
    }
}
