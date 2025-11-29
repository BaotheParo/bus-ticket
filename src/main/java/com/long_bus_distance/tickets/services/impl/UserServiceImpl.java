package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.dto.UserUpdateRequest;
import com.long_bus_distance.tickets.entity.User;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getFirstname() != null) {
            user.setFirstname(request.getFirstname());
        }
        if (request.getLastname() != null) {
            user.setLastname(request.getLastname());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getPhone() != null) {
            // Check if phone is unique if changed
            if (!request.getPhone().equals(user.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
                throw new IllegalArgumentException("Phone number is already in use.");
            }
            user.setPhone(request.getPhone());
        }

        return userRepository.save(user);
    }
}
