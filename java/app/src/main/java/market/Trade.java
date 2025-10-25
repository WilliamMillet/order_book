package market;

import java.util.UUID;

public record Trade(UUID offerId, UUID bidId, double price, int volume) {}