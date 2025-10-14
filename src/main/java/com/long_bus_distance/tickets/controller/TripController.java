package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.*;
import com.long_bus_distance.tickets.entity.Trip;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

    @PostMapping
    public ResponseEntity<CreateTripResponseDto> createTrip(
            @Valid @RequestBody CreateTripRequestDto createTripRequestDto,
            JwtAuthenticationToken authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID operatorId = UUID.fromString(jwt.getSubject());
        var createdTripRequest = tripMapper.fromDto(createTripRequestDto);
        var createdTrip = tripService.createTrip(createdTripRequest, operatorId);
        var createTripResponseDto = tripMapper.toCreateTripResponseDto(createdTrip);
        return new ResponseEntity<>(createTripResponseDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateTripResponseDto> updateTrip(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTripRequestDto updateTripRequestDto,
            JwtAuthenticationToken authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID operatorId = UUID.fromString(jwt.getSubject());
        UpdateTripRequest updateTripRequest = tripMapper.fromUpdateTripRequestDto(updateTripRequestDto);
        Trip updatedTrip = tripService.updateTripForOperator(operatorId, id, updateTripRequest);
        UpdateTripResponseDto responseDto = tripMapper.toUpdateTripResponseDto(updatedTrip);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<Page<ListTripResponseDto>> listTripForOperator(JwtAuthenticationToken authentication, Pageable pageable) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID operatorId = UUID.fromString(jwt.getSubject());
        Page<Trip> trips = tripService.listTripsForOperator(operatorId, pageable);
        Page<ListTripResponseDto> responseDtos = trips.map(tripMapper::toListTripResponseDto);
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetTripDetailsResponseDto> getTrip(@PathVariable UUID id, JwtAuthenticationToken authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID operatorId = UUID.fromString(jwt.getSubject());
        Optional<Trip> optionalTrip = tripService.getTripForOperator(operatorId, id);
        return optionalTrip.map(tripMapper::toGetTripDetailsResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable UUID id, JwtAuthenticationToken authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID operatorId = UUID.fromString(jwt.getSubject());
        tripService.deleteTripForOperator(operatorId, id);
        return ResponseEntity.noContent().build();
    }

    // Trong TripController, thay method getSeats
    @GetMapping("/{id}/seats")
    public ResponseEntity<TripSeatsResponseDto> getSeats(@PathVariable UUID id) {  // Fix: Return TripSeatsResponseDto
        log.info("Lấy seat map cho Trip ID: {}", id);
        TripSeatsResponseDto response = tripService.getSeatsForTrip(id);  // Match interface
        return ResponseEntity.ok(response);
    }
}