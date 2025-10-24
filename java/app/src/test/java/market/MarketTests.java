package market;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import market.matching.MatchResult;
import market.matching.MatchingEngine;
import market.matching.OrderStatus;
import market.orders.LimitOrder;
import market.orders.Order;
import market.orders.OrderSide;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class MarketTests {
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

    
}
