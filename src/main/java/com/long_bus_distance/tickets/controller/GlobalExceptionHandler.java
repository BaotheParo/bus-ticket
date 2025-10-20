package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.ErrorDto;
import com.long_bus_distance.tickets.exception.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Xử lý mọi ngoại lệ không xác định
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ErrorDto errorDto = new ErrorDto("An unknown error occurred");
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Xử lý vi phạm ràng buộc validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Constraint violation occurred", ex);
        String errorMessage = ex.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .orElse("A constraint violation occurred");
        ErrorDto errorDto = new ErrorDto(errorMessage);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // Xử lý lỗi validation argument
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Validation error occurred", ex);
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .orElse("A validation error occurred");
        ErrorDto errorDto = new ErrorDto(errorMessage);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // Xử lý không tìm thấy User
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("User not found", ex);
        ErrorDto errorDto = new ErrorDto("User not found");
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // Xử lý không tìm thấy Trip
    @ExceptionHandler(TripNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTripNotFoundException(TripNotFoundException ex) {
        log.error("Trip not found", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // Xử lý lỗi cập nhật Trip
    @ExceptionHandler(TripUpdateException.class)
    public ResponseEntity<ErrorDto> handleTripUpdateException(TripUpdateException ex) {
        log.error("Trip update error", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // Xử lý vé hết chỗ
    @ExceptionHandler(TicketSoldOutException.class)
    public ResponseEntity<ErrorDto> handleTicketSoldOutException(TicketSoldOutException ex) {
        log.error("Ticket sold out", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // Xử lý lỗi tạo QR Code
    @ExceptionHandler(QRCodeGenerationException.class)
    public ResponseEntity<ErrorDto> handleQRCodeGenerationException(QRCodeGenerationException ex) {
        log.error("QR code generation error", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // Xử lý không tìm thấy QR Code
    @ExceptionHandler(QRCodeNotFoundException.class)
    public ResponseEntity<ErrorDto> handleQRCodeNotFoundException(QRCodeNotFoundException ex) {
        log.error("QR code not found", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
    }

    // Xử lý BusTicketException chung (nếu có)
    @ExceptionHandler(BusTicketException.class)
    public ResponseEntity<ErrorDto> handleBusTicketException(BusTicketException ex) {
        log.error("Bus ticket error", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTicketNotFoundException(TicketNotFoundException ex) {
        log.error("Ticket not found", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<ErrorDto> handleInvalidDataAccess(InvalidDataAccessResourceUsageException ex) {
        log.warn("Invalid SQL query or data access: {}", ex.getMessage());
        // Return empty-like response thay vì error (UX-friendly)
        ErrorDto errorDto = new ErrorDto("No trips found matching criteria");
        return new ResponseEntity<>(errorDto, HttpStatus.OK);  // 200 thay vì 500
    }
}