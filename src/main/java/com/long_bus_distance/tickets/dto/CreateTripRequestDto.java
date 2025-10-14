package com.long_bus_distance.tickets.dto;

import com.long_bus_distance.tickets.entity.TripStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTripRequestDto {
    @NotBlank(message = "Route name is required")
    private String routeName;

    private String departureTime;  // ISO format

    @NotBlank(message = "Departure point is required")
    private String departurePoint;

    private String arrivalTime;  // ISO format

    @NotBlank(message = "Destination is required")
    private String destination;

    private Integer durationMinutes;

    @NotNull(message = "Bus type ID is required")
    private UUID busTypeId;

    private List<String> tripSchedule;  // e.g., ["08:30 Hanoi"]

    private String salesStart;  // ISO

    private String salesEnd;  // ISO

    @NotNull(message = "Status is required")
    private TripStatusEnum status;

    private Double basePrice;  // Optional, tính tự động nếu null
}