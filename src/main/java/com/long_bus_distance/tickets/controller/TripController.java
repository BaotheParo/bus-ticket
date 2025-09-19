package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.CreateTripRequestDto;
import com.long_bus_distance.tickets.dto.CreateTripResponseDto;
import com.long_bus_distance.tickets.mapper.TripMapper;
import com.long_bus_distance.tickets.services.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripMapper tripMapper;
    private final TripService tripService;

    @PostMapping
    public ResponseEntity<CreateTripResponseDto> createTrip(
        @Valid @RequestBody CreateTripRequestDto createTripRequestDto
            , JwtAuthenticationToken authentication){

        // Trich xuat ID tu JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID operatorId = UUID.fromString(jwt.getSubject());

        // DTO -> Service
        var createdTripRequest = tripMapper.fromDto(createTripRequestDto);

        // Goi service de tao Trip
        var createdTrip = tripService.createTrip(createdTripRequest,operatorId);

        // Entity -> DTO response
        var createTripResponseDto = tripMapper.toDto(createdTrip);

        //Return 201 (CREATED) -> THANH CONG
        return new ResponseEntity<>(createTripResponseDto, HttpStatus.CREATED);
    }
}
