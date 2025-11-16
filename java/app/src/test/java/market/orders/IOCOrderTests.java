package market.orders;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

import market.OrderBook;
import market.Trade;
import market.matching.MatchResult;
import market.matching.MatchingEngine;
import market.matching.OrderStatus;
import market.trader.Trader;

/**
 * Tests specific to immediate-or-cancel (IOC) orders
 */
@Timeout(5)
public class IOCOrderTests {
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

    @Test
    @DisplayName("Test IOC order where all volume is met immediately")
    public void testFullyMetIoc() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        Order bid = new LimitOrder(OrderSide.BUY, john, 100, 10.00);
        eng.placeOrder(bid);

        Order offer = new IOCOrder(OrderSide.SELL, jane, 100, 8.00);
        MatchResult offerRes = eng.placeOrder(offer);

        assertTrue(book.isEmpty());

        assertEquals(OrderStatus.FILLED, offerRes.getStatus());
        assertEquals(OrderSide.SELL, offerRes.getSide());
        assertEquals(100, offerRes.getFilledVolume());
        assertEquals(0, offerRes.getRemainingVolume());
        assertEquals(10.00, offerRes.getAvgMatchPrice());
        assertEquals("", offerRes.getNote());

        assertEquals(1, offerRes.getTrades().size());
        Trade trade = offerRes.getTrades().get(0);
        assertEquals(10.00, trade.price());
        assertEquals(100, trade.volume());
    }

    @Test
    @DisplayName("Test IOC order that is partially fulfilled")
    public void testPartiallyFulfilledIoc() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        Order offer1 = new LimitOrder(OrderSide.SELL, john, 60, 8.00);
        Order offer2 = new LimitOrder(OrderSide.SELL, john, 40, 10.00);
        eng.placeOrder(offer1);
        eng.placeOrder(offer2);

        Order bid = new IOCOrder(OrderSide.BUY, jane, 80, 9.00);
        MatchResult bidRes = eng.placeOrder(bid);

        assertEquals(OrderStatus.PARTIAL_REJECTION, bidRes.getStatus());
        assertEquals(60, bidRes.getFilledVolume());
        assertEquals(20, bidRes.getRemainingVolume());
        assertEquals(8.00, bidRes.getAvgMatchPrice());
        assertEquals(1, bidRes.getTrades().size());

        assertNull(book.getBestBid());
        Order bestOffer = book.getBestOffer();
        assertNotNull(bestOffer);
        assertEquals(40, bestOffer.getVolume());
        assertInstanceOf(LimitOrder.class, bestOffer);
        assertEquals(10.00, ((LimitOrder)bestOffer).getPrice());
    }

    @Test
    @DisplayName("Test IOC order where no volume can be fulfilled")
    public void testFullyRejectedIoc() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        Order bid = new LimitOrder(OrderSide.BUY, john, 60, 1.00);
        eng.placeOrder(bid);

        Order offer = new IOCOrder(OrderSide.SELL, jane, 60, 8.00);
        MatchResult offerRes = eng.placeOrder(offer);

        assertNull(book.getBestOffer());
        assertNotNull(book.getBestBid());

        assertEquals(OrderStatus.ALL_REJECTED, offerRes.getStatus());
        assertEquals(0, offerRes.getFilledVolume());
        assertEquals(60, offerRes.getRemainingVolume());
        assertEquals(0, offerRes.getTrades().size());
    }
}
