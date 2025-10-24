package market.orders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import market.Trader;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTests {
    @Test
    public void testInverseSide() {
        Trader john = new Trader("John");

        Order bid = new FOKOrder(OrderSide.BUY, john, 15, 15);
        Order offer = new FOKOrder(OrderSide.SELL, john, 15, 15);

        assertEquals(OrderSide.SELL, bid.getInverseSide());
        assertEquals(OrderSide.BUY, offer.getInverseSide());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 10, 12.3})
    public void testValidPrices(double price) {
        assertDoesNotThrow(() -> {
            new LimitOrder(OrderSide.BUY, new Trader("John"), 1, price);
        });
    }

    @ParameterizedTest
    @ValueSource(doubles = {-10, 0})
    public void testInvalidPrices(double price) {
        assertThrows(IllegalArgumentException.class, () -> {
            new LimitOrder(OrderSide.BUY, new Trader("John"), 1, price);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 120, 200000})
    public void testValidVolumes(int volume) {
        assertDoesNotThrow(() -> {
            new LimitOrder(OrderSide.BUY, new Trader("John"), volume, 100.0);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, 0})
    public void testInvalidVolumes(int volume) {
        assertThrows(IllegalArgumentException.class, () -> {
            new MarketOrder(OrderSide.SELL, new Trader("John"), volume);
        });
    }
}
