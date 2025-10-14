package com.long_bus_distance.tickets.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateBusTypeRequestDto {
    @NotBlank(message = "Tên BusType là bắt buộc")
    private String name;

    private String description;

    @NotNull(message = "Số tầng là bắt buộc")
    @Min(value = 1, message = "Số tầng tối thiểu 1")
    private Integer numDecks;

    @NotNull(message = "Số chỗ/tầng là bắt buộc")
    @Min(value = 0, message = "Số chỗ không âm")
    private Integer seatsPerDeck;

    private Double priceFactor;

    private Boolean isDefault = false;  // Chỉ dùng cho seed
}