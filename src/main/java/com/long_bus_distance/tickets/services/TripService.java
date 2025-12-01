package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.dto.ListPublishedTripResponseDto;
import com.long_bus_distance.tickets.dto.TripSeatsResponseDto;
import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.request.CreateTripRequest;
import com.long_bus_distance.tickets.request.UpdateTripRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TripService {
    Trip createTrip(CreateTripRequest request, UUID operatorId);

    Page<Trip> listTripsForOperator(UUID operatorId, Pageable pageable);

    Optional<Trip> getTripForOperator(UUID operatorId, UUID id);

    Trip updateTripForOperator(UUID operatorId, UUID id, UpdateTripRequest request);

    void deleteTripForOperator(UUID operatorId, UUID id);

    Page<Trip> listPublishedTrips(Pageable pageable);

    Page<Trip> searchPublishedTrips(String query, Pageable pageable);

    Optional<Trip> getPublishedTrip(UUID id);

    TripSeatsResponseDto getSeatsForTrip(UUID tripId);

    Page<ListPublishedTripResponseDto> searchPublishedTrips(
            String departurePoint, String destination, String departureDateStr, int numTickets,
            String timeSlot, String busType, String deckLabel, String routeName, String operatorId, Pageable pageable);

    Page<Trip> listAllTripsForAdmin(Pageable pageable);

    Optional<Trip> getTripForAdmin(UUID id);
}