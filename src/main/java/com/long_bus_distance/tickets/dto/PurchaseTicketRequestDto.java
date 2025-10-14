package com.long_bus_distance.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseTicketRequestDto {
    @NotNull(message = "Trip ID is required")
    private UUID tripId;

    @NotNull(message = "Deck ID is required")
    private UUID deckId;

    @NotBlank(message = "Selected seat position is required (e.g., '2')")
    private String selectedSeat;  // Chỉ vị trí số, e.g., "2"
}