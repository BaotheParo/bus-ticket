package com.long_bus_distance.tickets.entity;

public enum DeckEnum {
    A_DECK("A", "Tầng dưới"),
    B_DECK("B", "Tầng trên"),
    C_DECK("C", "Tầng 3"),
    D_DECK("D", "Tầng 4");

    private final String label;
    private final String description;

    DeckEnum(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}