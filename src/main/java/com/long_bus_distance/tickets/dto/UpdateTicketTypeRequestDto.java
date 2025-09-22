package com.long_bus_distance.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTicketTypeRequestDto {
    private UUID id;
    @NotBlank(message = "Ticket type name is required")
    private String name;
    @NotNull(message = "Price is required")
    private Double price;
    private String description;
    private Integer totalAvailable;
    private String deck;
}
