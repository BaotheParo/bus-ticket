package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.GetTicketResponseDto;
import com.long_bus_distance.tickets.dto.ListTicketResponseDto;
import com.long_bus_distance.tickets.dto.UserResponseDto; // --- THÊM MỚI ---
import com.long_bus_distance.tickets.entity.Ticket;
import com.long_bus_distance.tickets.entity.User;
import com.long_bus_distance.tickets.mapper.TicketMapper;
import com.long_bus_distance.tickets.services.AdminService;
import com.long_bus_distance.tickets.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // THÊM MỚI injections
    private final TicketService ticketService;
    private final TicketMapper ticketMapper;

    // Helper method để lấy user (THÊM MỚI)
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDto>> listUsers(
            @RequestParam(name = "role", required = false) Optional<String> role,
            // THÊM MỚI 2 tham số sau
            @RequestParam(name = "isActive", required = false) Optional<Boolean> isActive,
            @RequestParam(name = "search", required = false) Optional<String> search,
            Pageable pageable) {

        // CẬP NHẬT: Truyền các tham số mới vào service
        Page<UserResponseDto> users = adminService.listUsers(role, isActive, search, pageable);
        return ResponseEntity.ok(users);
    }

    // CẬP NHẬT: Thêm prefix /users
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> getUserDetails(@PathVariable UUID userId) {
        UserResponseDto user = adminService.getUserDetails(userId);
        return ResponseEntity.ok(user);
    }

    // CẬP NHẬT: Thêm prefix /users
    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        adminService.toggleUserStatus(userId, false);
        return ResponseEntity.noContent().build();
    }

    // CẬP NHẬT: Thêm prefix /users
    @PatchMapping("/users/{userId}/reactivate")
    public ResponseEntity<Void> reactivateUser(@PathVariable UUID userId) {
        adminService.toggleUserStatus(userId, true);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/tickets")
    public ResponseEntity<Page<ListTicketResponseDto>> getAllTickets(
            @RequestParam(name = "tripId", required = false) Optional<UUID> tripId,
            @RequestParam(name = "userEmail", required = false) Optional<String> userEmail,
            @RequestParam(name = "status", required = false) Optional<String> status,
            Pageable pageable) {

        Page<Ticket> tickets = ticketService.listAllTickets(tripId, userEmail, status, pageable);
        return ResponseEntity.ok(tickets.map(ticketMapper::toListTicketResponseDto));
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<GetTicketResponseDto> getTicketDetails(@PathVariable UUID ticketId) throws AccessDeniedException {
        User adminUser = getAuthenticatedUser();
        Ticket ticket = ticketService.getTicketDetailsForAdminOrOperator(ticketId, adminUser);
        return ResponseEntity.ok(ticketMapper.toGetTicketResponseDto(ticket));
    }

    @PostMapping("/tickets/{ticketId}/cancel")
    public ResponseEntity<GetTicketResponseDto> cancelTicket(@PathVariable UUID ticketId) throws AccessDeniedException {
        User adminUser = getAuthenticatedUser();
        Ticket ticket = ticketService.cancelTicketForAdminOrOperator(ticketId, adminUser);
        return ResponseEntity.ok(ticketMapper.toGetTicketResponseDto(ticket));
    }
}