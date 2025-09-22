package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.request.CreateTripRequest;
import com.long_bus_distance.tickets.request.UpdateTripRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TripService {
    //Tao chuyen xe moi
    Trip createTrip(CreateTripRequest tripRequest, UUID operatorId);

    //Liet ke cac chuyen xe cua mot NHA DIEU HANH
    Page<Trip> listTripsForOperator (UUID operatorId, Pageable pageable);

    //Lay chi tiet chuyen xe
    Optional<Trip> getTripForOperator(UUID operatorId, UUID id);

    //Cap nhat chuyen xe
    Trip updateTripForOperator(UUID operatorId, UUID id, UpdateTripRequest tripRequest);

    //Xoa chuyen xe
    void deleteTripForOperator (UUID operatorId, UUID id);

    //Liet ke cac chuyen xe da PUBLISHED
    Page<Trip> listPublishedTrips (Pageable pageable);

    //Tim kiem chuyen xe PUBLISHED dua tren QUERY
    Page<Trip> searchPublishedTrips(String query, Pageable pageable);

    //Goi chuyen di cu the theo ID
    Optional<Trip> getPublishedTrip(UUID id);
}
