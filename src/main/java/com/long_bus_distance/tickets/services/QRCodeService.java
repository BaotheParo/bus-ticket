package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.QRCode;
import com.long_bus_distance.tickets.entity.Ticket;

public interface QRCodeService {
    QRCode generateQRCode(Ticket ticket);
}
