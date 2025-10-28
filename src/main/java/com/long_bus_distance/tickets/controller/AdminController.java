package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.UserResponseDto; // --- THÊM MỚI ---
import com.long_bus_distance.tickets.entity.User;
import com.long_bus_distance.tickets.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    // --- CẬP NHẬT: Thay đổi kiểu trả về ---
    public ResponseEntity<Page<UserResponseDto>> listUsers(
            @RequestParam(name = "role", required = false) Optional<String> role,
            Pageable pageable) {
        Page<UserResponseDto> users = adminService.listUsers(role, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    // --- CẬP NHẬT: Thay đổi kiểu trả về ---
    public ResponseEntity<UserResponseDto> getUserDetails(@PathVariable UUID userId) {
        UserResponseDto user = adminService.getUserDetails(userId);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        adminService.toggleUserStatus(userId, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/reactivate")
    public ResponseEntity<Void> reactivateUser(@PathVariable UUID userId) {
        adminService.toggleUserStatus(userId, true);
        return ResponseEntity.noContent().build();
    }
}