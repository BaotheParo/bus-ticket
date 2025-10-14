package com.long_bus_distance.tickets.dto;

import com.long_bus_distance.tickets.entity.Deck;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetBusTypeResponseDto {
    private UUID id;
    private String name;
    private String description;
    private Integer numDecks;
    private Integer seatsPerDeck;
    private Double priceFactor;
    private Boolean isDefault;
    private List<Deck> defaultDecks;  // Bao gồm decks với priceFactor và totalSeats
}