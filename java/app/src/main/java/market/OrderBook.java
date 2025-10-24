package market;

import java.util.Comparator;
import java.util.PriorityQueue;

import market.orders.Order;

public class OrderBook {
    private final Comparator<Order> orderComparator = new Comparator<>() {
        @Override
        public int compare(Order o1, Order o2) {

        }
    }
    private final PriorityQueue<Order> bids = new PriorityQueue<>((Order o1, Order o2) -> ());
}
