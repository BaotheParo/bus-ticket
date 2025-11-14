package com.long_bus_distance.tickets.services.impl;

// ... (các imports hiện có)
import com.long_bus_distance.tickets.dto.statistics.PerformanceData;
import com.long_bus_distance.tickets.dto.statistics.RevenueOverTimeData;
import com.long_bus_distance.tickets.dto.statistics.StatisticsResponseDto;
import com.long_bus_distance.tickets.repository.TicketRepository;
import com.long_bus_distance.tickets.repository.TripRepository;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
// THÊM IMPORT NÀY
import java.sql.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    @Override
    public StatisticsResponseDto getAdminStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        PageRequest top5 = PageRequest.of(0, 5);

        // 1. Thẻ tổng quan (Giữ nguyên)
        Double totalRevenue = ticketRepository.getPlatformRevenueBetweenDates(startDateTime, endDateTime);
        Long totalTicketsSold = ticketRepository.getPlatformTicketsSoldBetweenDates(startDateTime, endDateTime);
        Long totalNewCustomers = userRepository.countNewPassengersBetweenDates(startDateTime, endDateTime);
        Long totalOperators = userRepository.countTotalOperators();

        // 2. Biểu đồ (SỬA LỖI Ở ĐÂY)
        List<Object[]> adminRevenueResults = ticketRepository.getAdminRevenueOverTime(startDateTime, endDateTime);

        List<RevenueOverTimeData> revenueOverTime = adminRevenueResults.stream()
                .map(row -> new RevenueOverTimeData(
                        // SỬA LỖI: Chuyển java.sql.Date sang java.time.LocalDate
                        ((Date) row[0]).toLocalDate(),
                        ((Number) row[1]).doubleValue())
                )
                .collect(Collectors.toList());

        // Map thủ công từ List<Object[]> sang List<PerformanceData> (Giữ nguyên)
        List<Object[]> topOperatorsResults = ticketRepository.getTopOperators(top5, startDateTime, endDateTime);
        List<PerformanceData> topPerformers = topOperatorsResults.stream()
                .map(row -> new PerformanceData(
                        (String) row[0],
                        ((Number) row[1]).doubleValue())
                )
                .collect(Collectors.toList());

        return StatisticsResponseDto.builder()
                .totalRevenue(totalRevenue)
                .totalTicketsSold(totalTicketsSold)
                .totalNewCustomers(totalNewCustomers)
                .totalOperators(totalOperators)
                .revenueOverTime(revenueOverTime)
                .topPerformers(topPerformers)
                .build();
    }

    @Override
    public StatisticsResponseDto getOperatorStatistics(UUID operatorId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        PageRequest top5 = PageRequest.of(0, 5);

        // 1. Thẻ tổng quan (Giữ nguyên)
        Double totalRevenue = ticketRepository.getOperatorRevenueBetweenDates(operatorId, startDateTime, endDateTime);
        Long totalTicketsSold = ticketRepository.getOperatorTicketsSoldBetweenDates(operatorId, startDateTime, endDateTime);
        Long totalTrips = tripRepository.countTripsForOperatorBetweenDates(operatorId, startDateTime, endDateTime);
        Long totalSeats = tripRepository.getTotalSeatsForOperatorTrips(operatorId, startDateTime, endDateTime);
        Double averageOccupancy = 0.0;
        if (totalSeats > 0) {
            Long ticketsSoldForTrips = ticketRepository.getOperatorTicketsSoldBetweenDates(operatorId, startDateTime, endDateTime);
            averageOccupancy = (double) ticketsSoldForTrips / totalSeats * 100;
        }

        // 2. Biểu đồ (SỬA LỖI Ở ĐÂY)
        List<Object[]> operatorRevenueResults = ticketRepository.getOperatorRevenueOverTime(operatorId, startDateTime, endDateTime);

        List<RevenueOverTimeData> revenueOverTime = operatorRevenueResults.stream()
                .map(row -> new RevenueOverTimeData(
                        // SỬA LỖI: Chuyển java.sql.Date sang java.time.LocalDate
                        // Mặc dù Operator hoạt động, nhưng thống nhất cách làm cho an toàn
                        (row[0] instanceof Date) ? ((Date) row[0]).toLocalDate() : (LocalDate) row[0],
                        ((Number) row[1]).doubleValue())
                )
                .collect(Collectors.toList());

        // Map thủ công (Giữ nguyên)
        List<Object[]> topTripsResults = ticketRepository.getTopTripsForOperator(top5, operatorId, startDateTime, endDateTime);
        List<PerformanceData> topPerformers = topTripsResults.stream()
                .map(row -> new PerformanceData(
                        (String) row[0],
                        ((Number) row[1]).doubleValue())
                )
                .collect(Collectors.toList());


        return StatisticsResponseDto.builder()
                .totalRevenue(totalRevenue)
                .totalTicketsSold(totalTicketsSold)
                .totalTrips(totalTrips)
                .averageOccupancy(averageOccupancy)
                .revenueOverTime(revenueOverTime)
                .topPerformers(topPerformers)
                .build();
    }
}