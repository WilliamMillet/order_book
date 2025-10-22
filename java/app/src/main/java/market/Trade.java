package market;

import java.util.UUID;

public record Trade(UUID offererId, UUID bidderId, double price, int volume) {}