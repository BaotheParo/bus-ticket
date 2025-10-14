package com.long_bus_distance.tickets.exception;

public class TripNotFoundException extends BusTicketException{ //tuong tu khi import BusTicketException
    public TripNotFoundException() {
    }

    public TripNotFoundException(String message) {
        super(message);
    }

    public TripNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TripNotFoundException(Throwable cause) {
        super(cause);
    }

    public TripNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}