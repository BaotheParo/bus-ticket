package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.dto.UserResponseDto;
import com.long_bus_distance.tickets.entity.User;
import com.long_bus_distance.tickets.exception.BusTicketException;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    // --- CẬP NHẬT: Thay đổi kiểu trả về và thêm logic map ---
    public Page<UserResponseDto> listUsers(Optional<String> role, Pageable pageable) {
        Specification<User> spec = (root, query, criteriaBuilder) ->
                role.map(r -> criteriaBuilder.like(root.get("roles"), "%" + r.toUpperCase() + "%"))
                        .orElse(criteriaBuilder.conjunction());

        Page<User> userPage = userRepository.findAll(spec, pageable);

        // Sử dụng .map() của Page để chuyển đổi từng User thành UserResponseDto
        return userPage.map(UserResponseDto::fromEntity);
    }

    @Override
    // --- CẬP NHẬT: Thay đổi kiểu trả về và thêm logic map ---
    public UserResponseDto getUserDetails(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusTicketException("User not found with ID: " + userId));
        return UserResponseDto.fromEntity(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(UUID userId, boolean status) {
        // --- CẬP NHẬT: Lấy entity từ repo thay vì từ method khác ---
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusTicketException("User not found with ID: " + userId));
        user.setActive(status);
        userRepository.save(user);
    }
}