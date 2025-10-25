package market.orders;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderSummary(UUID id, int volume, double price, LocalDateTime timestamp, OrderSide side) {}
