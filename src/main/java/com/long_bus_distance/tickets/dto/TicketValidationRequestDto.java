package com.long_bus_distance.tickets.dto;

import com.long_bus_distance.tickets.entity.TicketValidationMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketValidationRequestDto {
    private String id;  // ticketId (manual) or qrId (QR scan)
    private TicketValidationMethodEnum method;
}