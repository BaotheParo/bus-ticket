package com.long_bus_distance.tickets.exception;

public class QRCodeNotFoundException extends BusTicketException {
    public QRCodeNotFoundException(String message) {
        super(message);
    }
}