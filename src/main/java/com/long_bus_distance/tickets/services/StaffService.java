package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.dto.CreateStaffRequestDto;
import com.long_bus_distance.tickets.dto.UpdateStaffRequestDto;
import com.long_bus_distance.tickets.entity.User;

import java.util.List;
import java.util.UUID;

public interface StaffService {
    User createStaff(CreateStaffRequestDto request, UUID operatorId);
    List<User> getStaffForOperator(UUID operatorId);
    User getStaffDetailsForOperator(UUID staffId, UUID operatorId);
    User updateStaff(UUID staffId, UpdateStaffRequestDto request, UUID operatorId);
    void toggleStaffStatus(UUID staffId, boolean status, UUID operatorId);
}