package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.*;
import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.entity.User;
import com.long_bus_distance.tickets.mapper.TripMapper;
import com.long_bus_distance.tickets.request.UpdateTripRequest;
import com.long_bus_distance.tickets.services.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {
    private final TripMapper tripMapper;
    private final TripService tripService;

    // Helper method to get the authenticated user from the Security Context
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        return (User) authentication.getPrincipal();
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<CreateTripResponseDto> createTrip(
            @Valid @RequestBody CreateTripRequestDto createTripRequestDto) {
        User operator = getAuthenticatedUser();
        var createdTripRequest = tripMapper.fromDto(createTripRequestDto);
        var createdTrip = tripService.createTrip(createdTripRequest, operator.getId());
        var createTripResponseDto = tripMapper.toCreateTripResponseDto(createdTrip);
        return new ResponseEntity<>(createTripResponseDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<UpdateTripResponseDto> updateTrip(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTripRequestDto updateTripRequestDto) {
        User operator = getAuthenticatedUser();
        UpdateTripRequest updateTripRequest = tripMapper.fromUpdateTripRequestDto(updateTripRequestDto);
        Trip updatedTrip = tripService.updateTripForOperator(operator.getId(), id, updateTripRequest);
        UpdateTripResponseDto responseDto = tripMapper.toUpdateTripResponseDto(updatedTrip);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Page<ListTripResponseDto>> listTripForOperator(Pageable pageable) {
        User operator = getAuthenticatedUser();
        Page<Trip> trips = tripService.listTripsForOperator(operator.getId(), pageable);
        Page<ListTripResponseDto> responseDtos = trips.map(tripMapper::toListTripResponseDto);
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<GetTripDetailsResponseDto> getTrip(@PathVariable UUID id) {
        User operator = getAuthenticatedUser();
        Optional<Trip> optionalTrip = tripService.getTripForOperator(operator.getId(), id);
        return optionalTrip.map(tripMapper::toGetTripDetailsResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Void> deleteTrip(@PathVariable UUID id) {
        User operator = getAuthenticatedUser();
        tripService.deleteTripForOperator(operator.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<TripSeatsResponseDto> getSeats(@PathVariable UUID id) {
        log.info("Lấy seat map cho Trip ID: {}", id);
        TripSeatsResponseDto response = tripService.getSeatsForTrip(id);
        return ResponseEntity.ok(response);
    }
}