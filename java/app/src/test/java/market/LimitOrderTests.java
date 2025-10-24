package market;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import market.matching.MatchResult;
import market.matching.MatchingEngine;
import market.matching.OrderStatus;
import market.orders.LimitOrder;
import market.orders.Order;
import market.orders.OrderSide;
import market.orders.PricedOrder;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Timeout(5)
class LimitOrderTests {
    private OrderBook book;
    private MatchingEngine eng;
    private List<Trader> traders;

    @BeforeEach
    public void setupMarket() {
        book = new OrderBook();
        eng = new MatchingEngine(book);

        List<String> names = List.of("John", "Jane", "Jack", "Dave", "Mike", "Sally");
        traders = new ArrayList<>(names.stream().map(n -> new Trader(n)).toList());
    }

    @Disabled
    @Test void testInsertionSpeed() {
        Trader john = traders.get(0);

        Random random = new Random();
        int volume = random.nextInt(50) + 50;
        double basePrice = 100.00 + random.nextDouble(10);

        List<Order> orders = new ArrayList<>();

        for (int i = 0; i < 3000000; i++) {
            Order order;
            if (random.nextInt() == 1) {
                order = new LimitOrder(OrderSide.BUY, john, volume, basePrice - 2);
            } else {
                order = new LimitOrder(OrderSide.SELL, john, volume, basePrice + 2);
            }
            orders.add(order);
        }

        System.out.println("Starting insertion at time " + LocalDateTime.now().toString());
        
        for (Order order : orders) {
            eng.placeOrder(order);
        }
        
        System.out.println("Finished insertion process at time " + LocalDateTime.now().toString());   
    }

    /**
     * Test simple order match where bid and offer are the same price and volume
     */
    @Test
    public void simpleLimitOrderPair() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        Order bid1 = new LimitOrder(OrderSide.BUY, john, 100, 10.00);
        eng.placeOrder(bid1);

        assertEquals(book.getBestBid(), bid1);
        
        Order offer1 = new LimitOrder(OrderSide.SELL, jane, 100, 10.00);
        MatchResult offer1Res = eng.placeOrder(offer1);

        assertTrue(book.isEmpty());

        assertEquals(offer1.getOrderId(), offer1Res.getOrderId());
        assertEquals("", offer1Res.getNote());
        assertEquals(OrderSide.SELL, offer1Res.getSide());
        assertEquals(OrderStatus.FILLED, offer1Res.getStatus());
        assertEquals(100, offer1Res.getFilledVolume());
        assertEquals(0, offer1Res.getRemainingVolume());
        assertEquals(10.00, offer1Res.getAvgMatchPrice());
        assertEquals(1, offer1Res.getTrades().size());
    }

    /**
     * Test that match candidates outside of the limit range are not matched
     */
    @Test
    public void orderOutsideOfLimitNotMatched() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        Order bid1 = new LimitOrder(OrderSide.BUY, john, 100, 10.00);
        eng.placeOrder(bid1);

        // Limit should be too high for previous offer to match
        Order offer1 = new LimitOrder(OrderSide.SELL, jane, 100, 15.00);
        MatchResult offer1Res = eng.placeOrder(offer1);

        assertEquals(bid1, book.getBestBid());
        assertEquals(offer1, book.getBestOffer());

        assertEquals("", offer1Res.getNote());
        assertEquals(0, offer1Res.getFilledVolume());
        assertEquals(100, offer1Res.getRemainingVolume());
        assertEquals(MatchResult.getNoMatches(), offer1Res.getAvgMatchPrice());
        assertEquals(0, offer1Res.getTrades().size());

        // Next order of $11.99 also should not match (It will be at the top as well
        // since 11.99 > 10)
        Order bid2 = new LimitOrder(OrderSide.BUY, john, 100, 11.99);
        eng.placeOrder(bid2);

        assertEquals(bid2, book.getBestBid());
    }

    /**
     * Test the result of an incoming order having a volume less than the order
     * that it matches with.
     */
    @Test
    public void matchVolumeGtOrder() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        Order offer = new LimitOrder(OrderSide.SELL, john, 75, 10.0);
        eng.placeOrder(offer);

        Order bid = new LimitOrder(OrderSide.BUY, jane, 50, 10.0);
        eng.placeOrder(bid);
        
        PricedOrder bestOffer = book.getBestOffer();
        assertNotNull(bestOffer);
        assertEquals(25, bestOffer.getVolume());
        assertNull(book.getBestBid());
    }

    /**
     * Test the result of an incoming order having a higher volume than the order
     * it matches with (when there is only one order to match with)
     */
    @Test
    public void matchVolumeLtTotalVolume() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        Order offer = new LimitOrder(OrderSide.SELL, john, 50, 10.0);
        eng.placeOrder(offer);

        Order bid = new LimitOrder(OrderSide.BUY, jane, 75, 10.0);
        eng.placeOrder(bid);

        assertNull(book.getBestOffer());
        PricedOrder bestBid = book.getBestBid();
        assertNotNull(bestBid);
        assertEquals(25, bestBid.getVolume());
    }

    /**
     * Test the result of an incoming order having a higher volume than the order
     * it matches with, but not the total volume of orders that can be matched with
     */
    @Test
    public void matchVolumeLtOrder() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);
        Trader jack = traders.get(2);

        Order offer1 = new LimitOrder(OrderSide.SELL, john, 50, 10.0);
        Order offer2 = new LimitOrder(OrderSide.SELL, jane, 50, 10.0);
        eng.placeOrder(offer1);
        eng.placeOrder(offer2);

        Order bid = new LimitOrder(OrderSide.BUY, jack, 70, 10.0);
        eng.placeOrder(bid);

        PricedOrder bestOffer = book.getBestOffer();
        assertNotNull(bestOffer);
        assertEquals(30, bestOffer.getVolume());
        assertNull(book.getBestBid());
    }
}
