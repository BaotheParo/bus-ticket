package com.long_bus_distance.tickets.exception;

public class TripUpdateException extends BusTicketException{ //tuong tu khi import BusTicketException
    public TripUpdateException() {
    }

    public TripUpdateException(String message) {
        super(message);
    }

    public TripUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public TripUpdateException(Throwable cause) {
        super(cause);
    }

    public TripUpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}