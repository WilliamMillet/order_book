package market.orders;

/**
 * Validator for orders during creation
 */
public final class OrderValidator {
    private OrderValidator() {};

    /**
     * Throw an error if an order volume is not legal in the market
     * @param volume
     */
    public static void validateVolume(int volume) {
        if (volume <= 0) {
            throw new IllegalArgumentException("Cannot have order with volume less than or equal to zero");
        }
    }

    /**
     * Throw an error if an order price or limit is not legal in the market
     * @param price the prospective order limit
     */
    public static void validatePrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Cannot have order with price less than or equal to zero");
        }
    }
}
