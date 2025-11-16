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


@Timeout(5)
public class FOKOrderTests {
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
    @DisplayName("Test FOK order where all volume is met immediately")
    public void testSuccessfulFok() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);
        
        Order bid = new LimitOrder(OrderSide.BUY, john, 100, 10.0);
        eng.placeOrder(bid);

        assertEquals(book.getBestBid(), bid);

        Order offer = new FOKOrder(OrderSide.SELL, jane, 100, 10.00);
        MatchResult offerRes = eng.placeOrder(offer);
        
        assertTrue(book.isEmpty());

        assertEquals(OrderStatus.FILLED, offerRes.getStatus());
        assertEquals(OrderSide.SELL, offerRes.getSide());
        assertEquals(100, offerRes.getFilledVolume());
        assertEquals(0, offerRes.getRemainingVolume());
        assertEquals(10, offerRes.getAvgMatchPrice());
        assertEquals("", offerRes.getNote());
        

        assertEquals(1, offerRes.getTrades().size());
        Trade trade = offerRes.getTrades().get(0);
        assertEquals(10, trade.price());
        assertEquals(100, trade.volume());
    }   

    @Test
    @DisplayName("Test FOK order that cannot be fully matched is rejected and book unchanged")
    public void testFailedFok() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        Order offer1 = new LimitOrder(OrderSide.SELL, john, 60, 8.00);
        Order offer2 = new LimitOrder(OrderSide.SELL, john, 40, 10.00);
        eng.placeOrder(offer1);
        eng.placeOrder(offer2);

        Order bid = new FOKOrder(OrderSide.BUY, jane, 80, 9.00);
        MatchResult bidRes = eng.placeOrder(bid);

        assertEquals(OrderStatus.ALL_REJECTED, bidRes.getStatus());
        assertEquals(0, bidRes.getFilledVolume());
        assertEquals(80, bidRes.getRemainingVolume());
        assertEquals(0, bidRes.getTrades().size());
        assertTrue(bidRes.getNote().toLowerCase().contains("liquidity"));

        assertNull(book.getBestBid());
        Order bestOffer = book.getBestOffer();
        assertNotNull(bestOffer);
        assertEquals(60, bestOffer.getVolume());
        assertInstanceOf(PricedOrder.class, bestOffer);
        assertEquals(8.00, ((PricedOrder) bestOffer).getPrice());
    }
}
