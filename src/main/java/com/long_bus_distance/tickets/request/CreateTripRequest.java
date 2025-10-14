package com.long_bus_distance.tickets.request;

import com.long_bus_distance.tickets.entity.TripStatusEnum;
import com.long_bus_distance.tickets.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTripRequest {
    private String routeName;
    private String departureTime;  // ISO string
    private String departurePoint;
    private String arrivalTime;  // ISO string
    private String destination;
    private Integer durationMinutes;
    private UUID busTypeId;
    private List<String> tripSchedule = new ArrayList<>();
    private String saleStart;  // ISO
    private String saleEnd;  // ISO
    private TripStatusEnum status;
    private Double basePrice;
    private User operator;  // Set trong service
}