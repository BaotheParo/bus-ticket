package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.dto.CreateStaffRequestDto;
import com.long_bus_distance.tickets.dto.UpdateStaffRequestDto;
import com.long_bus_distance.tickets.entity.User;
import com.long_bus_distance.tickets.exception.BusTicketException;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.services.StaffService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createStaff(CreateStaffRequestDto request, UUID operatorId) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusTicketException("Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusTicketException("Email '" + request.getEmail() + "' is already in use.");
        }

        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new BusTicketException("Operator not found."));

        User staff = new User();
        staff.setUsername(request.getUsername());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setFirstname(request.getFirstname());
        staff.setLastname(request.getLastname());
        staff.setEmail(request.getEmail());
        staff.setDateOfBirth(request.getDateOfBirth());
        staff.setRoles("ROLE_STAFF");
        staff.setActive(true); // Mặc định active khi tạo
        staff.setManagedByOperator(operator); // Gán staff cho operator

        return userRepository.save(staff);
    }

    @Override
    // CẬP NHẬT: Triển khai logic lọc và phân trang
    public Page<User> getStaffForOperator(
            UUID operatorId,
            Optional<Boolean> isActive,
            Optional<String> search,
            Pageable pageable) {

        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. (QUAN TRỌNG) Lọc theo Operator ID
            predicates.add(criteriaBuilder.equal(
                    root.get("managedByOperator").get("id"), operatorId)
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

        return userRepository.findAll(spec, pageable);
    }

    @Override
    public User getStaffDetailsForOperator(UUID staffId, UUID operatorId) {
        User staff = findStaffAndVerifyOwnership(staffId, operatorId);
        return staff;
    }

    @Override
    @Transactional
    public User updateStaff(UUID staffId, UpdateStaffRequestDto request, UUID operatorId) {
        User staff = findStaffAndVerifyOwnership(staffId, operatorId);

        if (request.getEmail() != null && !request.getEmail().equals(staff.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusTicketException("Email '" + request.getEmail() + "' is already in use.");
            }
            staff.setEmail(request.getEmail());
        }

        if (request.getFirstname() != null) staff.setFirstname(request.getFirstname());
        if (request.getLastname() != null) staff.setLastname(request.getLastname());
        if (request.getDateOfBirth() != null) staff.setDateOfBirth(request.getDateOfBirth());

        return userRepository.save(staff);
    }

    @Override
    @Transactional
    public void toggleStaffStatus(UUID staffId, boolean status, UUID operatorId) {
        User staff = findStaffAndVerifyOwnership(staffId, operatorId);
        staff.setActive(status);
        userRepository.save(staff);
    }

    // Helper method để tái sử dụng logic tìm staff và kiểm tra quyền
    private User findStaffAndVerifyOwnership(UUID staffId, UUID operatorId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new BusTicketException("Staff not found with ID: " + staffId));

        if (staff.getManagedByOperator() == null || !staff.getManagedByOperator().getId().equals(operatorId)) {
            throw new AccessDeniedException("You do not have permission to manage this staff member.");
        }

        return staff;
    }
}