package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.ErrorDto;
import com.long_bus_distance.tickets.exception.UserNotFoundException;
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
}
