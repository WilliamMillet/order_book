package market;

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
}