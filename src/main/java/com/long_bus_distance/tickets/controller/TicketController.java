package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.GetTicketResponseDto;
import com.long_bus_distance.tickets.dto.ListTicketResponseDto;
import com.long_bus_distance.tickets.dto.PurchaseTicketRequestDto;
import com.long_bus_distance.tickets.entity.Ticket;
import com.long_bus_distance.tickets.entity.User;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // Helper method to get the authenticated user from the Security Context
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        return (User) authentication.getPrincipal();
    }

    @PostMapping
    @PreAuthorize("hasRole('PASSENGER')")
    // Sửa return type thành String hoặc DTO chứa URL
    public ResponseEntity<String> purchaseTicket(
            @Valid @RequestBody PurchaseTicketRequestDto requestDto) {
        log.info("Nhận request mua vé...");
        User currentUser = getAuthenticatedUser();

        // Gọi service, nhận về URL
        String paymentUrl = ticketService.purchaseTicket(
                currentUser.getId(),
                requestDto.getTripId(),
                requestDto.getDeckId(),
                requestDto.getSelectedSeat()
        );

        // Trả về URL để Frontend redirect
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Page<ListTicketResponseDto>> listTickets(Pageable pageable) {
        log.info("Liệt kê vé cho user với phân trang: {}", pageable);
        User currentUser = getAuthenticatedUser();
        Page<Ticket> tickets = ticketService.listTicketForUser(currentUser.getId(), pageable);
        Page<ListTicketResponseDto> responseDtos = tickets.map(ticketMapper::toListTicketResponseDto);
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<GetTicketResponseDto> getTicket(@PathVariable UUID ticketId) {
        log.info("Lấy vé ID: {}", ticketId);
        User currentUser = getAuthenticatedUser();
        Optional<Ticket> optionalTicket = ticketService.getTicketForUser(ticketId, currentUser.getId());
        return optionalTicket.map(ticketMapper::toGetTicketResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{ticketId}/qrcodes")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<byte[]> getTicketQRCode(@PathVariable UUID ticketId) {
        log.info("Lấy QR cho vé ID: {}", ticketId);
        User currentUser = getAuthenticatedUser();
        byte[] qrCodeImage = qrCodeService.getQRCodeImageForUserAndTicket(currentUser.getId(), ticketId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCodeImage.length);
        return ResponseEntity.ok().headers(headers).body(qrCodeImage);
    }
}