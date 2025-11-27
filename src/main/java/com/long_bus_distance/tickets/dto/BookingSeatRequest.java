package com.long_bus_distance.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingSeatRequest {
    @NotNull(message = "Trip ID is required")
    private UUID tripId;

    @NotNull(message = "Deck ID is required")
    private UUID deckId;

    @NotBlank(message = "Selected seat position is required")
    private String selectedSeat;
}
