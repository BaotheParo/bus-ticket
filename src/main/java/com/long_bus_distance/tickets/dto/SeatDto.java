package com.long_bus_distance.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatDto {
    private String position;  // "1", "2", ..., "17" (số vị trí)
    private String status;  // "AVAILABLE" or "BOOKED"
    private Double price;  // Tính từ deck.priceFactor * trip.basePrice / totalSeats (giá mỗi ghế)
}