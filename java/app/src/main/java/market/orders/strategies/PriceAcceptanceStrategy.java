package market.orders.strategies;


public interface PriceAcceptanceStrategy {
    /**
     * Determine if the price of an order is acceptable when matching with the order this is composed within 
     * @param price the candidate order price to match with
     * @return true if the price is not an issue, false otherwise
     */
    public boolean acceptsPrice(double price);
}
