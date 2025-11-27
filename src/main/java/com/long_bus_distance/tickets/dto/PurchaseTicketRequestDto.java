package com.long_bus_distance.tickets.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseTicketRequestDto {
    @NotEmpty(message = "Danh sách ghế không được để trống")
    @Valid
    private List<BookingSeatRequest> bookingSeats;
}
