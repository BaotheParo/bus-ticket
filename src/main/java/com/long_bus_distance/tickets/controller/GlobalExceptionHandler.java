package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.ErrorDto;
import com.long_bus_distance.tickets.exception.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// GlobalExceptionHandler là lớp xử lý các ngoại lệ toàn cục trong ứng dụng.
// Nó sẽ bắt các lỗi xảy ra trong các Controller và trả về phản hồi thích hợp.
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

//     Xử lý mọi loại ngoại lệ không xác định.
//     @param ex ngoại lệ xảy ra
//     @return phản hồi với mã trạng thái 500 (Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException (Exception ex){
        log.error("Unexpected error occured", ex);
        ErrorDto errorDto = new ErrorDto("An unknow error occured");
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //Xử lý ngoại lệ khi có vi phạm ràng buộc.
    //@param ex ngoại lệ ConstraintViolationException
    //@return phản hồi với mã trạng thái 400 (Bad Request)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstaintViolation(ConstraintViolationException ex){
        log.error("Constaint violation occurred",ex);
        String errorMessage = ex.getConstraintViolations().stream().findFirst().map(violation -> violation.getPropertyPath() + " " + violation.getMessage()).orElse("A constraint violation occurred");
        ErrorDto errorDto = new ErrorDto(errorMessage);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    //Xử lý ngoại lệ khi có lỗi xác thực phương thức.
    //@param ex ngoại lệ MethodArgumentNotValidException
    //@return phản hồi với mã trạng thái 400 (Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArguementNotValidException
            (MethodArgumentNotValidException ex){
        log.error("Validation error occured",ex);
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField()+ " " +
                        fieldError.getDefaultMessage())
                .orElse("A validation error occured");
        ErrorDto errorDto = new ErrorDto((errorMessage));
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    //Xử lý ngoại lệ khi không tìm thấy người dùng.
    //@param ex ngoại lệ UserNotFoundException
    //@return phản hồi với mã trạng thái 400 (Bad Request)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFoundException(UserNotFoundException ex){
        log.error("User not found", ex);
        ErrorDto errorDto = new ErrorDto("User not found");
        return new ResponseEntity<>(errorDto,HttpStatus.BAD_REQUEST);
    }

    // Xử lý ngoại lệ khi không tìm thấy người dùng
    // @param ex Ngoại lệ UserNotFoundException
    // @return phản hồi với mã trạng thái 400 (Bad Request)
    @ExceptionHandler(TripNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTripNotFoundException(TripNotFoundException ex) {
        log.error("Trip not found", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // Xử lý ngoại lệ khi không tìm thấy chuyến xe
    // @param ex ngoại lệ TripNotFoundException
    // @return phản hồi với mã trạng thái 400 (Bad Request)
    @ExceptionHandler(TicketTypeNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTicketTypeNotFoundException(TicketTypeNotFoundException ex) {
        log.error("Ticket type not found", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

//  Xử lí ngoại lệ liên quan đến UPDATE chuyến xe
//  @param ex ngoại lệ TripUpdateException
//  @return phản hồi với mã trạng thái 400 (Bad Request)
    @ExceptionHandler(TripUpdateException.class)
    public ResponseEntity<ErrorDto> handleTripUpdateException(TripUpdateException ex) {
        log.error("Trip update error", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    //  Xử lí ngoại lệ liên quan đến tao QRCode
    //  @param ex ngoại lệ QRCodeGenerationException
    //  @return phản hồi với mã trạng thái 400 (Bad Request)
    @ExceptionHandler(QRCodeGenerationException.class)
    public ResponseEntity<ErrorDto> handleQRCodeGenerationException(QRCodeGenerationException ex) {
        log.error("QR code generation error", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    //  Xử lí ngoại lệ liên quan đến QRCode khong tim thay
    //  @param ex ngoại lệ QRCodeNotFoundException
    //  @return phản hồi với mã trạng thái 400 (Bad Request)
    @ExceptionHandler(QRCodeNotFoundException.class)
    public ResponseEntity<ErrorDto> handleQRCodeNotFoundException(QRCodeNotFoundException ex) {
        log.error("QR code not found", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    //  Xử lí ngoại lệ liên quan đến QRCode khong tim thay
    //  @param ex ngoại lệ QRCodeNotFoundException
    //  @return phản hồi với mã trạng thái 400 (Bad Request)
    @ExceptionHandler(TicketSoldOutException.class)
    public ResponseEntity<ErrorDto> handleTicketSoldOutException(TicketSoldOutException ex) {
        log.error("Ticket sold out", ex);
        ErrorDto errorDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }
}
