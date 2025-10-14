package com.long_bus_distance.tickets.exception;

public class TicketSoldOutException extends RuntimeException { //auto gen func khi import RunTimeException
    public TicketSoldOutException() {
    }

    public TicketSoldOutException(String message) {
        super(message);
    }

    public TicketSoldOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public TicketSoldOutException(Throwable cause) {
        super(cause);
    }

    public TicketSoldOutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}