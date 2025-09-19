package com.long_bus_distance.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketTypeResponseDto {
    private String id;
    private String name;
    private Double price;
    private String description;
    private Integer totalAvailable;
    private String deck;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
