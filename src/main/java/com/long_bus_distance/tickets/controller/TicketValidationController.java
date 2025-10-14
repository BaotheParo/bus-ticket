package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.TicketValidationRequestDto;
import com.long_bus_distance.tickets.dto.TicketValidationResponseDto;
import com.long_bus_distance.tickets.entity.TicketValidation;
import com.long_bus_distance.tickets.entity.TicketValidationMethodEnum;
import com.long_bus_distance.tickets.mapper.TicketValidationMapper;
import com.long_bus_distance.tickets.services.TicketValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ticket-validations")
@RequiredArgsConstructor
@Slf4j
public class TicketValidationController {
    private final TicketValidationService ticketValidationService;
    private final TicketValidationMapper ticketValidationMapper;

    @PostMapping
    @PreAuthorize("hasRole('STAFF')")  // Chỉ STAFF validate
    public ResponseEntity<TicketValidationResponseDto> validateTicket(@RequestBody TicketValidationRequestDto requestDto) {
        log.info("Nhận request validate vé ID: {} method: {}", requestDto.getId(), requestDto.getMethod());
        TicketValidation validation;
        UUID id = UUID.fromString(requestDto.getId());
        if (requestDto.getMethod() == TicketValidationMethodEnum.MANUAL) {
            validation = ticketValidationService.validateTicketManually(id);
        } else {
            validation = ticketValidationService.validateTicketByQRCode(id);
        }
        TicketValidationResponseDto responseDto = ticketValidationMapper.toDto(validation);
        return ResponseEntity.ok(responseDto);
    }
}