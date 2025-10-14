package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.QRCode;
import com.long_bus_distance.tickets.entity.Ticket;

import java.util.UUID;

public interface QRCodeService {
    QRCode generateQRCode(Ticket ticket);  // Tạo QR cho ticket mới
    byte[] getQRCodeImageForUserAndTicket(UUID userId, UUID ticketId);  // Lấy PNG byte[] cho user
}