package com.long_bus_distance.tickets.exception;

public class QRCodeGenerationException extends BusTicketException {
    public QRCodeGenerationException(String message) {
        super(message);
    }

    public QRCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}