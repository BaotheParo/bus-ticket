package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.request.CreateTripRequest;

import java.util.UUID;

public interface TripService {
    Trip createTrip(CreateTripRequest tripRequest, UUID operatorId);
}
