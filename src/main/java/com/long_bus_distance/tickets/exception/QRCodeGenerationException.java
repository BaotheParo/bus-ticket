package com.long_bus_distance.tickets.exception;

public class QRCodeGenerationException extends RuntimeException { //auto gen func khi import RunTimeException
    public QRCodeGenerationException() {
    }

    public QRCodeGenerationException(String message) {
        super(message);
    }

    public QRCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public QRCodeGenerationException(Throwable cause) {
        super(cause);
    }

    public QRCodeGenerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
