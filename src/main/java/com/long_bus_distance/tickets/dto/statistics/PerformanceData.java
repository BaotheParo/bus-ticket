package com.long_bus_distance.tickets.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceData {
    private String label; // Tên (Nhà xe hoặc Tuyến đường)
    private Double value; // Doanh thu hoặc số vé
}