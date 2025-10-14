package com.long_bus_distance.tickets.dto;

import com.long_bus_distance.tickets.entity.TripStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListTripResponseDto {
    private UUID id;
    private String routeName;
    private LocalDateTime departureTime;
    private String departurePoint;
    private LocalDateTime arrivalTime;
    private String destination;
    private Integer durationMinutes;
    private UUID busTypeId;
    private Double basePrice;
    private List<String> tripSchedule;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private TripStatusEnum status;
    private List<DeckResponseDto> decks;  // Với remainingCount
}