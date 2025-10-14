package com.long_bus_distance.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckWithSeatsDto {
    private UUID id;
    private String label;  // "A", "B"
    private Double priceFactor;
    private Integer totalSeats;
    private String remainingCount;  // "16/17"
    private List<SeatDto> seats;  // Array ghế cho map
}