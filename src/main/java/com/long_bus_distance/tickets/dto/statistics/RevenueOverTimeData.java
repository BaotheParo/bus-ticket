package com.long_bus_distance.tickets.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueOverTimeData {
    private LocalDate date; // Ngày (trục X)
    private Double revenue; // Doanh thu (trục Y)
}