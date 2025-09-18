package com.long_bus_distance.tickets.request;

import com.long_bus_distance.tickets.entity.TripStatusEnum;
import com.long_bus_distance.tickets.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTripRequest {
    private String routename;
    private String departureTime;//ISO 8601 format String,vv..
    // VD: 2025-09-10T08:30:00
    private String departurePoint;
    private String arrivalTime; //ISO 8601 format String,vv..
    // VD: 2025-09-10T08:30:00
    private String destination;
    private Integer durationMinutes;
    private String busType;// GHE, GIUONG, LIMOUSINE
    private List<String> tripSchedule; //VD:["08:30 Hanoi", "12:00 Ca Mau"]
    private String saleStart;
    private String saleEnd;
    private TripStatusEnum status;
    private List<CreateTicketTypeRequest> ticketType = new ArrayList<>();

}
