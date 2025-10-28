package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.CreateStaffRequestDto;
import com.long_bus_distance.tickets.dto.UpdateStaffRequestDto;
import com.long_bus_distance.tickets.entity.User;
import com.long_bus_distance.tickets.services.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operator/staff")
@PreAuthorize("hasRole('OPERATOR')")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    // Helper method to get the authenticated operator
    private User getAuthenticatedOperator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @PostMapping
    public ResponseEntity<User> createStaff(@Valid @RequestBody CreateStaffRequestDto requestDto) {
        User operator = getAuthenticatedOperator();
        User newStaff = staffService.createStaff(requestDto, operator.getId());
        return new ResponseEntity<>(newStaff, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<User>> getMyStaff() {
        User operator = getAuthenticatedOperator();
        List<User> staffList = staffService.getStaffForOperator(operator.getId());
        return ResponseEntity.ok(staffList);
    }

    @GetMapping("/{staffId}")
    public ResponseEntity<User> getStaffDetails(@PathVariable UUID staffId) {
        User operator = getAuthenticatedOperator();
        User staff = staffService.getStaffDetailsForOperator(staffId, operator.getId());
        return ResponseEntity.ok(staff);
    }

    @PutMapping("/{staffId}")
    public ResponseEntity<User> updateStaff(@PathVariable UUID staffId, @Valid @RequestBody UpdateStaffRequestDto requestDto) {
        User operator = getAuthenticatedOperator();
        User updatedStaff = staffService.updateStaff(staffId, requestDto, operator.getId());
        return ResponseEntity.ok(updatedStaff);
    }

    @PatchMapping("/{staffId}/deactivate")
    public ResponseEntity<Void> deactivateStaff(@PathVariable UUID staffId) {
        User operator = getAuthenticatedOperator();
        staffService.toggleStaffStatus(staffId, false, operator.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{staffId}/reactivate")
    public ResponseEntity<Void> reactivateStaff(@PathVariable UUID staffId) {
        User operator = getAuthenticatedOperator();
        staffService.toggleStaffStatus(staffId, true, operator.getId());
        return ResponseEntity.noContent().build();
    }
}