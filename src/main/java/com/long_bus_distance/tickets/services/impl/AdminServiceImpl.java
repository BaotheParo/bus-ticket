package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.dto.UserResponseDto;
import com.long_bus_distance.tickets.entity.User;
import com.long_bus_distance.tickets.exception.BusTicketException;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.services.AdminService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    // CẬP NHẬT: Thêm tham số và logic Specification
    public Page<UserResponseDto> listUsers(
            Optional<String> role,
            Optional<Boolean> isActive,
            Optional<String> search,
            Pageable pageable) {

        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo Vai trò
            role.ifPresent(r -> predicates.add(
                    criteriaBuilder.like(root.get("roles"), "%" + r.toUpperCase() + "%"))
            );

            // 2. Lọc theo Trạng thái (isActive)
            isActive.ifPresent(status -> predicates.add(
                    criteriaBuilder.equal(root.get("isActive"), status))
            );

            // 3. Lọc Tìm kiếm (search)
            search.ifPresent(searchTerm -> {
                String likePattern = "%" + searchTerm.toLowerCase() + "%";
                Predicate searchOr = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstname")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastname")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern)
                );
                predicates.add(searchOr);
            });

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);

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