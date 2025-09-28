package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.QRCode;
import com.long_bus_distance.tickets.entity.Ticket;

import java.util.UUID;

public interface QRCodeService {
    QRCode generateQRCode(Ticket ticket);
    //Lấy hình QR dựa trên mã user và mã vé
    byte[] getQRCodeImageForUserAndTicket(UUID userId, UUID ticketId);
}
