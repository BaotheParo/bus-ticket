package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.dto.statistics.StatisticsResponseDto;
import java.time.LocalDate;
import java.util.UUID;

public interface StatisticsService {
    StatisticsResponseDto getAdminStatistics(LocalDate startDate, LocalDate endDate);
    StatisticsResponseDto getOperatorStatistics(UUID operatorId, LocalDate startDate, LocalDate endDate);
}