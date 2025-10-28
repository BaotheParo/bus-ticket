package com.long_bus_distance.tickets.dto;

import com.long_bus_distance.tickets.entity.User;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponseDto {
    private UUID id;
    private String username;
    private String firstname;
    private String lastname;
    private LocalDate dateOfBirth;
    private String email;
    private String roles;
    private boolean isActive;
    private UUID managedByOperatorId; // Chỉ lấy ID của operator, không lấy cả object
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper method để chuyển đổi từ Entity sang DTO
    public static UserResponseDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Quan trọng: Kiểm tra null trước khi truy cập để tránh lỗi
        if (user.getManagedByOperator() != null) {
            dto.setManagedByOperatorId(user.getManagedByOperator().getId());
        }

        return dto;
    }
}