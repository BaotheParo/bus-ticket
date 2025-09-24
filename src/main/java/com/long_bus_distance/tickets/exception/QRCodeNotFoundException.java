package com.long_bus_distance.tickets.exception;

public class QRCodeNotFoundException extends RuntimeException { //auto gen func khi import RunTimeException
    public QRCodeNotFoundException() {
    }

    public QRCodeNotFoundException(String message) {
        super(message);
    }

    public QRCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public QRCodeNotFoundException(Throwable cause) {
        super(cause);
    }

    public QRCodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
