package com.long_bus_distance.tickets.dto;

import com.long_bus_distance.tickets.entity.TripStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTripRequestDto {
    @NotNull(message = "Trip ID is required")
    private UUID id;

    private String routeName;

    private String departureTime;

    private String departurePoint;

    private String arrivalTime;

    private String destination;

    private Integer durationMinutes;

    private UUID busTypeId;  // UUID trực tiếp

    private List<String> tripSchedule;

    private String salesStart;

    private String salesEnd;

    private TripStatusEnum status;

    private Double basePrice;
}