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
            @RequestParam(name = "q", required = false) String query, Pageable pageable) {
        log.info("Nhận yêu cầu GET để liệt kê hoặc tìm kiếm các chuyến xe đã xuất bản với truy vấn: {} và phân trang: {}", query, pageable);
        Page<Trip> trips;
        if (query != null && !query.trim().isEmpty()) {
            trips = tripService.searchPublishedTrips(query, pageable);
        } else {
            trips = tripService.listPublishedTrips(pageable);
        }
        Page<ListPublishedTripResponseDto> responseDtos = trips.map(tripMapper::toListPublishedTripResponseDto);
        return ResponseEntity.ok(responseDtos);
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