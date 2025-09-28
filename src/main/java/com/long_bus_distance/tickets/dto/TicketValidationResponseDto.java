package com.long_bus_distance.tickets.dto;

import com.long_bus_distance.tickets.entity.TicketStatusEnum;
import com.long_bus_distance.tickets.entity.TicketValidation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketValidationResponseDto {
    private String ticketId;
    private TicketStatusEnum status;
}
