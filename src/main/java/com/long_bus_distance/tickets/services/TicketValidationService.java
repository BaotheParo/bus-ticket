package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.TicketValidation;

import java.util.UUID;

public interface TicketValidationService {
    //quét vé bằng mã QR
    TicketValidation validateTicketByQRCode(UUID qrCodeId);
    //nhập mã vé
    TicketValidation validateTicketManually(UUID ticketId);
}
