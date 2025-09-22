package com.long_bus_distance.tickets.dto;

import com.long_bus_distance.tickets.entity.TripStatusEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTripRequestDto {

    @NotNull(message = "Trip ID is required")
    private UUID id;

    @NotBlank(message = "Route name is required")
    private String routeName;

    private String departureTime;

    @NotBlank(message = "Departure point is required")
    private String departurePoint;

    private String arrivalTime;

    @NotBlank(message = "Destination is required")
    private String destination;

    private Integer durationMinutes;

    @NotBlank(message = "Bus type is required")
    private String busType;

    private List<String> tripSchedule; // e.g., ["08:30 Hanoi", "12:00 Ninh Binh"]

    private String salesStart;

    private String salesEnd;

    @NotNull(message = "Status is required")
    private TripStatusEnum status;

    @Valid
    @NotNull(message = "Ticket types are required")
    @Size(min = 1, message = "At least one ticket type is required")
    private List<UpdateTicketTypeRequestDto> ticketTypes;
}
