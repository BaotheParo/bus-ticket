package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.GetPublishedTripDetailsResponseDto;
import com.long_bus_distance.tickets.dto.ListPublishedTripResponseDto;
import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.mapper.TripMapper;
import com.long_bus_distance.tickets.services.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/published-trips")
@RequiredArgsConstructor
@Slf4j
public class PublishedTripController {
    private final TripService tripService;
    private final TripMapper tripMapper;

    @GetMapping
    public ResponseEntity<Page<ListPublishedTripResponseDto>> listPublishedTrip(
            @RequestParam(name = "departurePoint", required = false) String departurePoint,
            @RequestParam(name = "destination", required = false) String destination,
            @RequestParam(name = "departureDate", required = false) String departureDate,
            @RequestParam(name = "numTickets", defaultValue = "1") int numTickets,
            @RequestParam(name = "timeSlot", required = false) String timeSlot,
            @RequestParam(name = "busType", required = false) String busType,
            @RequestParam(name = "deckLabel", required = false) String deckLabel,
            @RequestParam(name = "q", required = false) String query,  // Keep old query if needed
            Pageable pageable) {

        log.info("Search published trips: departurePoint={}, destination={}, date={}, numTickets={}, filters={}",
                departurePoint, destination, departureDate, numTickets,
                "timeSlot=" + timeSlot + ",busType=" + busType + ",deck=" + deckLabel);

        // Use new search method (primary + filters); old 'q' fallback to simple search if no primary
        Page<ListPublishedTripResponseDto> response;
        if (departurePoint != null || destination != null || departureDate != null) {
            // Primary search mode
            response = tripService.searchPublishedTrips(departurePoint, destination, departureDate, numTickets,
                    timeSlot, busType, deckLabel, pageable);
        } else if (query != null && !query.trim().isEmpty()) {
            // Backward compat: Old simple search
            Page<Trip> trips = tripService.searchPublishedTrips(query, pageable);  // Existing
            response = trips.map(tripMapper::toListPublishedTripResponseDto);
        } else {
            // Default: List all published (no filter)
            Page<Trip> trips = tripService.listPublishedTrips(pageable);
            response = trips.map(tripMapper::toListPublishedTripResponseDto);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetPublishedTripDetailsResponseDto> getPublishedTripDetails(@PathVariable UUID id) {
        log.info("Nhận yêu cầu GET để lấy chi tiết chuyến xe đã xuất bản với ID: {}", id);
        Optional<Trip> optionalTrip = tripService.getPublishedTrip(id);
        return optionalTrip.map(tripMapper::toGetPublishedTripDetailsResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}