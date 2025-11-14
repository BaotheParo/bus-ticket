package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.dto.CreateStaffRequestDto;
import com.long_bus_distance.tickets.dto.UpdateStaffRequestDto;
import com.long_bus_distance.tickets.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffService {
    User createStaff(CreateStaffRequestDto request, UUID operatorId);
    // CẬP NHẬT: Thay đổi hoàn toàn phương thức này
    Page<User> getStaffForOperator(
            UUID operatorId,
            Optional<Boolean> isActive,
            Optional<String> search,
            Pageable pageable);
    User getStaffDetailsForOperator(UUID staffId, UUID operatorId);
    User updateStaff(UUID staffId, UpdateStaffRequestDto request, UUID operatorId);
    void toggleStaffStatus(UUID staffId, boolean status, UUID operatorId);
}