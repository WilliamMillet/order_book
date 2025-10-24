package market;

import java.util.Objects;
import java.util.UUID;

public class Trader {
    private final String name;
    private final UUID id;

    public Trader(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (other == null || !Trader.class.isInstance(other)) {
            return false;
        }

        Trader otherTrader = (Trader) other;
        return (name.equals(otherTrader.getName()) && id.equals(otherTrader.getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }
 }