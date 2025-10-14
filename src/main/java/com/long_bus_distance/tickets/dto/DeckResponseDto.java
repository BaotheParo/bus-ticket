package com.long_bus_distance.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeckResponseDto {
    private UUID id;
    private String label;  // "A", "B"
    private Double priceFactor;  // Hệ số giá tầng
    private Integer totalSeats;  // Số chỗ
    private String remainingCount;  // Tính động sau (e.g., "16/17")
}