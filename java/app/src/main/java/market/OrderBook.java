package market;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.UUID;

import market.orders.Order;
import market.orders.OrderSide;
import market.orders.OrderValidator;
import market.orders.PricedOrder;

public class OrderBook {
    private final Comparator<PricedOrder> bidComparator = Comparator.comparing(PricedOrder::getPrice).reversed().thenComparing(PricedOrder::getTimestamp);
    private final Comparator<PricedOrder> offerComparator = Comparator.comparing(PricedOrder::getPrice).thenComparing(PricedOrder::getTimestamp);

    private final PriorityQueue<PricedOrder> bids = new PriorityQueue<>(bidComparator);
    private final PriorityQueue<PricedOrder> offers = new PriorityQueue<>(offerComparator);

    public PricedOrder getBestBid() {
        return bids.peek();
    }

    public PricedOrder getBestOffer() {
        return offers.peek();
    }

    public boolean isEmpty() {
        return (bids.size() == 0 && offers.size() == 0);
    }

    public int getNumOffers() {
        return offers.size();
    }
    
    public int getNumBids() {
        return bids.size();
    }
    
    /**
     * Wrapper for getBestBid and getBestOffer allowing for dynamic routing between these based on an argument
     * @param side the side of the order (bid or ask)
     * @return the best order on that side
     */
    public PricedOrder getBestOrder(OrderSide side) {
        return switch (side) {
            case BUY -> getBestBid();
            case SELL -> getBestOffer();
            default -> throw new IllegalArgumentException("Order side '" + side.toString() + "' is not known");
        };
    }

    /**
     * Insert an order into the order book in O(log(n)) time 
     * @param order the order to insert
     */
    public void insertRestingOrder(PricedOrder order) {
        getHeapOfSide(order.getSide()).add(order);
    }


    /**
     * Remove an order from the book with a specific 
     * @param orderId the id of the order to remove
     * @param side the side which the order is on
     * @return true if the order existed and could be cancelled, else false
     */
    public boolean cancelOrder(UUID orderId, OrderSide side) {
        PriorityQueue<PricedOrder> sideHeap = getHeapOfSide(side);
        return sideHeap.removeIf(o -> o.getOrderId().equals(orderId));
    }

    /**
     * Update the volume of an order. This operation is slow for non-head elements so use it sparingly
     * @param orderId the id of the order to update the volume of
     * @param side the side of the order
     * @param newVolume the new volume
     * @return true if the order volume was updated, false otherwise
     */
    public boolean amendOrderVolume(UUID orderId, OrderSide side, int newVolume) {
        // Call validation here so we can fail fast (even though we check during the update process)
        OrderValidator.validateVolume(newVolume);
        
        PriorityQueue<PricedOrder> relevantHeap = getHeapOfSide(side);
        for (PricedOrder o : relevantHeap) {
            if (o.getOrderId().equals(orderId)) {
                // We have to remove from the queue for re-heapification to occur
                relevantHeap.remove(o);
                o.setVolume(newVolume);
                relevantHeap.add(o);

                return true;
            }
        }

        return false;
    }
    
    public Trade tradeTop(Order order, int volumeToTrade) {
        PricedOrder best = getBestOrder(order.getSide());
        if (best == null) {
            throw new OrderNotFoundException("No orders to trade with found");
        }

        if (best.getVolume() == volumeToTrade) {
            cancelOrder(best.getOrderId(), best.getSide());
        } else {
            amendOrderVolume(best.getOrderId(), order.getInverseSide(), best.getVolume() - volumeToTrade);
        }

        UUID bidderId;
        UUID offererId;
        switch (order.getSide()) {
            case BUY:
                bidderId = order.getOrderId();
                offererId = best.getOrderId();
                break;
            case SELL:
                offererId = order.getOrderId();
                bidderId = order.getOrderId();
                break;
            default:
                throw new IllegalArgumentException("Order side '" + order.getSide().toString() + "' is not known");
        }

        return new Trade(offererId, bidderId, best.getPrice(), volumeToTrade);
    }

    /**
     * Get the relevant heap in the order book for an order side. Throws an error if the side is unknown
     * @param side the side to get the heap for
     * @return the heap for the relevant side
     */
    private PriorityQueue<PricedOrder> getHeapOfSide(OrderSide side) {
        return switch (side) {
            case BUY -> bids;
            case SELL -> offers;
            default -> throw new IllegalArgumentException("Order side '" + side.toString() + "' is not known");
        };
    }




    

    


}
