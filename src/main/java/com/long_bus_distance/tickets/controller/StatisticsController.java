package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.statistics.StatisticsResponseDto;
import com.long_bus_distance.tickets.entity.User;
import com.long_bus_distance.tickets.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    // Helper method to get the authenticated user
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<?> getStatistics(
            @RequestParam(name = "startDate") String startDateStr,
            @RequestParam(name = "endDate") String endDateStr) {
        
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(startDateStr); // Định dạng ISO: YYYY-MM-DD
            endDate = LocalDate.parse(endDateStr);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Định dạng ngày không hợp lệ. Vui lòng sử dụng YYYY-MM-DD.");
        }

        User currentUser = getAuthenticatedUser();
        StatisticsResponseDto responseDto;

        if (currentUser.getRoles().contains("ROLE_ADMIN")) {
            responseDto = statisticsService.getAdminStatistics(startDate, endDate);
        } else {
            // Đã được PreAuthorize, nên nếu không phải Admin thì là Operator
            responseDto = statisticsService.getOperatorStatistics(currentUser.getId(), startDate, endDate);
        }
        
        return ResponseEntity.ok(responseDto);
    }
}