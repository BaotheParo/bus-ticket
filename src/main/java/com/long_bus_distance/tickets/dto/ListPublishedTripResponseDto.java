package com.long_bus_distance.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListPublishedTripResponseDto {
    private UUID id;
    private String routeName;
    private LocalDateTime departureTime;
    private String departurePoint;
    private LocalDateTime arrivalTime;
    private String destination;
    private Integer totalAvailableSeats;
}