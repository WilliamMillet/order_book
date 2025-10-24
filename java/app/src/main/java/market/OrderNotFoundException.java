package market;

/**
 * Exception for when an order cannot be found in the order book
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
