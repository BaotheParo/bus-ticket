package com.long_bus_distance.tickets.exception;

public class BusTicketException extends RuntimeException { //auto gen func khi import RunTimeException
    public BusTicketException() {
    }

    public BusTicketException(String message) {
        super(message);
    }

    public BusTicketException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusTicketException(Throwable cause) {
        super(cause);
    }

    public BusTicketException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}