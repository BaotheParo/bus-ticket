package com.long_bus_distance.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripSeatsResponseDto {
    private UUID id;
    private String routeName;
    private List<DeckWithSeatsDto> decks;  // List tầng với seats
}