package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.GetTicketResponseDto;
import com.long_bus_distance.tickets.dto.ListTicketResponseDto;
import com.long_bus_distance.tickets.dto.PurchaseTicketRequestDto;
import com.long_bus_distance.tickets.entity.Ticket;
import com.long_bus_distance.tickets.mapper.TicketMapper;
import com.long_bus_distance.tickets.services.QRCodeService;
import com.long_bus_distance.tickets.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {
    private final TicketService ticketService;
    private final TicketMapper ticketMapper;
    private final QRCodeService qrCodeService;

    @PostMapping
    public ResponseEntity<GetTicketResponseDto> purchaseTicket(
            @Valid @RequestBody PurchaseTicketRequestDto requestDto,
            JwtAuthenticationToken authentication) {
        log.info("Nhận request mua vé cho trip {}, deck {}", requestDto.getTripId(), requestDto.getDeckId());
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());
        Ticket ticket = ticketService.purchaseTicket(userId, requestDto.getTripId(), requestDto.getDeckId(), requestDto.getSelectedSeat());
        GetTicketResponseDto responseDto = ticketMapper.toGetTicketResponseDto(ticket);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<Page<ListTicketResponseDto>> listTickets(JwtAuthenticationToken authentication, Pageable pageable) {
        log.info("Liệt kê vé cho user với phân trang: {}", pageable);
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());
        Page<Ticket> tickets = ticketService.listTicketForUser(userId, pageable);
        Page<ListTicketResponseDto> responseDtos = tickets.map(ticketMapper::toListTicketResponseDto);
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<GetTicketResponseDto> getTicket(@PathVariable UUID ticketId, JwtAuthenticationToken authentication) {
        log.info("Lấy vé ID: {}", ticketId);
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());
        Optional<Ticket> optionalTicket = ticketService.getTicketForUser(ticketId, userId);
        return optionalTicket.map(ticketMapper::toGetTicketResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{ticketId}/qrcodes")
    public ResponseEntity<byte[]> getTicketQRCode(@PathVariable UUID ticketId, JwtAuthenticationToken authentication) {
        log.info("Lấy QR cho vé ID: {}", ticketId);
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());
        byte[] qrCodeImage = qrCodeService.getQRCodeImageForUserAndTicket(userId, ticketId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCodeImage.length);
        return ResponseEntity.ok().headers(headers).body(qrCodeImage);
    }
}