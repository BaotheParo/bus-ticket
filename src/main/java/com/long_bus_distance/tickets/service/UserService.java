package com.long_bus_distance.tickets.service;

import com.long_bus_distance.tickets.dto.UserUpdateRequest;
import com.long_bus_distance.tickets.entity.User;

import java.util.UUID;

public interface UserService {
    User updateUser(UUID id, UserUpdateRequest request);
}
