package com.long_bus_distance.tickets.request;

import com.long_bus_distance.tickets.entity.TripStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTripRequest {
    private UUID id;
    private String routeName;
    private String departureTime;
    private String departurePoint;
    private String arrivalTime;
    private String destination;
    private Integer durationMinutes;
    private UUID busTypeId;  // Optional, nếu có thì re-clone decks
    private List<String> tripSchedule;
    private String salesStart;
    private String salesEnd;
    private TripStatusEnum status;
    private Double basePrice;
}