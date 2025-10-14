package com.long_bus_distance.tickets.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBusTypeRequestDto {
    private String description;

    @NotNull(message = "Số tầng là bắt buộc")
    @Min(value = 1)
    private Integer numDecks;

    @NotNull(message = "Số chỗ/tầng là bắt buộc")
    @Min(value = 0)
    private Integer seatsPerDeck;

    private Double priceFactor;
}