package com.long_bus_distance.tickets.dto;

import com.long_bus_distance.tickets.entity.TripStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTripResponseDto {
    private String id;
    private String routeName;
    private LocalDateTime departureTime;
    private String departurePoint;
    private LocalDateTime arrivalTime;
    private String destination;
    private Integer durationMinutes;
    private String busType;
    private List<String> tripSchedule;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private TripStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CreateTicketTypeResponseDto> ticketTypes;
}
