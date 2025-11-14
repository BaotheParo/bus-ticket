package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.dto.UserResponseDto; // --- CẬP NHẬT ---
import com.long_bus_distance.tickets.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AdminService {
    Page<UserResponseDto> listUsers(
            Optional<String> role,
            Optional<Boolean> isActive,
            Optional<String> search,
            Pageable pageable);
    UserResponseDto getUserDetails(UUID userId);
    void toggleUserStatus(UUID userId, boolean status);
}