package com.long_bus_distance.tickets.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponseDto {
    // 1. Thẻ tổng quan
    private Double totalRevenue;
    private Long totalTicketsSold;
    
    // Thẻ của Admin
    private Long totalNewCustomers;
    private Long totalOperators;

    // Thẻ của Operator
    private Long totalTrips;
    private Double averageOccupancy; // Tỷ lệ lấp đầy

    // 2. Biểu đồ
    private List<RevenueOverTimeData> revenueOverTime;
    private List<PerformanceData> topPerformers; // Top 5 nhà xe (Admin) hoặc Top 5 tuyến (Operator)
}