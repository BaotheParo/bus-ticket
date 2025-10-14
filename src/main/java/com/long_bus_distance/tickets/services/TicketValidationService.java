package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.TicketValidation;

import java.util.UUID;

public interface TicketValidationService {
    TicketValidation validateTicketByQRCode(UUID qrId);  // Quét QR, parse content to ticketId
    TicketValidation validateTicketManually(UUID ticketId);  // Nhập ticketId
}