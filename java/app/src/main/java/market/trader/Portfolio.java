package market.trader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import market.Trade;
import market.matching.MatchResult;
import market.matching.MatchingEngine;
import market.orders.OrderSummary;

public class Portfolio implements MatchSubscriber{
    public final TreeSet<OrderSummary> activeOrders = new TreeSet<>(Comparator.comparing(OrderSummary::id));
    public final List<Trade> tradeHistory = new ArrayList<>();

    public Portfolio(MatchingEngine eng) {
        eng.addSubscriber(this);
    }

    @Override
    public void notifyOfMatch(MatchResult matchRes) {
        List<Trade> trades = matchRes.getTrades();
        tradeHistory.addAll(trades);
        
        for (Trade trade : trades) {
            if (activeOrders.stream().anyMatch(o -> linked(o, trade))) {
                updateOrder(trade);
            }
        }
    }

    /**
     * Update an order in the active order set based on a trade that occurred in relation too it
     * @param trade
     */
    private void updateOrder(Trade trade) {
        
    }

    /**
     * Determine if an order was involved in a trade
     * @param order
     * @param trade
     */
    private boolean linked(OrderSummary order, Trade trade) {
        return order.id().equals(trade.bidId()) || order.id().equals(trade.offerId());
    }
}
