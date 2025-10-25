package market.orders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.BeforeEach;

import market.OrderBook;
import market.matching.MatchResult;
import market.matching.MatchingEngine;
import market.matching.OrderStatus;
import market.trader.Trader;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

@Timeout(5)
class MarketOrderTests {
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

    /**
     * Test a bid and order of the same quantity and price match correctly
     */
    @Test
    public void regularMarketOrder() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        Order offer1 = new LimitOrder(OrderSide.SELL, john, 150, 10.00);
        eng.placeOrder(offer1);

        Order bid1 = new MarketOrder(OrderSide.BUY, jane, 150);
        MatchResult bid1Res = eng.placeOrder(bid1);

        // Since bid and offer match they should no longer be on the order book
        assertTrue(book.isEmpty());

        // In depth checks for bid MatchResult
        assertEquals(OrderStatus.FILLED, bid1Res.getStatus());
        assertEquals(bid1.getOrderId(), bid1Res.getOrderId());
        assertEquals(OrderSide.BUY, bid1Res.getSide());
        assertEquals("", bid1Res.getNote());
        assertEquals(150, bid1Res.getFilledVolume());
        assertEquals(0, bid1Res.getRemainingVolume());
        assertEquals(10.00, bid1Res.getAvgMatchPrice());
        assertNotNull(bid1Res.getTimestamp());

        assertEquals(1, bid1Res.getTrades().size());
        var trade = bid1Res.getTrades().get(0);
        assertEquals(10.00, trade.price());
        assertEquals(150, trade.volume());
    }

    /**
     * Test market order on the sell side
     */
    @Test
    public void marketOrderOffer() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        // Check the inverse way (Bid made before market offer)
        Order bid1 = new LimitOrder(OrderSide.BUY, john, 75, 10.00);
        eng.placeOrder(bid1);
        assertEquals(bid1, book.getBestBid());

        Order offer1 = new MarketOrder(OrderSide.SELL, jane, 75);
        MatchResult offer1Res = eng.placeOrder(offer1);

        assertEquals(OrderSide.SELL, offer1Res.getSide());
        assertTrue(book.isEmpty());
    }

    /**
     * Check that a market order does not go through when there are no other
     * trades for it to match with
     */
    @Test
    public void marketOrderWhenLowLiquidity() {
        Trader john = traders.get(0);

        // Purposely do NOT add any limit orders to the book
        Order bid1 = new MarketOrder(OrderSide.BUY, john, 150);
        MatchResult bid1Res = eng.placeOrder(bid1);

        // Market order should be rejected due to lack of liquidity
        assertEquals(OrderStatus.ALL_REJECTED, bid1Res.getStatus());
        assertTrue(bid1Res.getNote().toLowerCase().contains("liquidity"));
        assertEquals(0, bid1Res.getFilledVolume());
        assertEquals(150, bid1Res.getRemainingVolume());
        assertEquals(MatchResult.getNoMatches(), bid1Res.getAvgMatchPrice());
        assertEquals(0, bid1Res.getTrades().size());

        assertNull(book.getBestBid());
    }

    /**
     * Test that when only some of the volume for a market order can be filled,
     * this will be handled correctly
     */
    @Test
    public void marketOrderPartialFill() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        Order offer1 = new LimitOrder(OrderSide.SELL, john, 30, 20.14);
        eng.placeOrder(offer1);
        Order offer2 = new LimitOrder(OrderSide.SELL, john, 70, 15.12);
        eng.placeOrder(offer2);

        double expWeightedAvg = (30 * 20.14 + 70 * 15.12) / (30.0 + 70.0);

        Order bid1 = new MarketOrder(OrderSide.BUY, jane, 120);
        MatchResult bid1Res = eng.placeOrder(bid1);

        assertEquals(OrderStatus.PARTIAL_REJECTION, bid1Res.getStatus());
        assertEquals("Insufficient liquidity to match order fully", bid1Res.getNote());
        assertEquals(100, bid1Res.getFilledVolume());
        assertEquals(20, bid1Res.getRemainingVolume());
        assertEquals(expWeightedAvg, bid1Res.getAvgMatchPrice(), 0.001);
        assertEquals(2, bid1Res.getTrades().size());
    }

    /**
     * Market orders should execute at the best price, even if this
     * price is a terrible price
     */
    @Test
    public void extremePriceDifferenceAllowed() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);

        // Extremely high price
        Order offer1 = new LimitOrder(OrderSide.SELL, john, 150, 999999999.0);
        eng.placeOrder(offer1);

        // No price specified - market order
        Order bid1 = new MarketOrder(OrderSide.BUY, jane, 150);
        eng.placeOrder(bid1);

        assertTrue(book.isEmpty());
    }

    /**
     * If there are several orders of the same price, time should be used as a
     * tie breaker
     */
    @Test
    public void timeIsUsedAsTieBreaker() {
        Trader john = traders.get(0);
        Trader jane = traders.get(1);
        Trader jack = traders.get(2);

        // Offer prices need to be equal to test this
        Order firstOffer = new LimitOrder(OrderSide.SELL, jack, 150, 10.00);
        eng.placeOrder(firstOffer);

        // Add multiple orders at the same price
        for (int i = 0; i < 10; i++) {
            Order offer = new LimitOrder(OrderSide.SELL, john, 150, 10.00);
            eng.placeOrder(offer);
        }

        Order bid = new MarketOrder(OrderSide.BUY, jane, 150);
        MatchResult bidRes = eng.placeOrder(bid);

        assertNull(book.getBestBid());

        // Should match with the first offer placed (Jack's order)
        assertEquals(1, bidRes.getTrades().size());
        var trade = bidRes.getTrades().get(0);
        assertEquals(jack.getId(), trade.offererId());
    }
}