package com.long_bus_distance.tickets.config;

import com.long_bus_distance.tickets.dto.TicketValidationRequestDto;
import com.long_bus_distance.tickets.dto.TicketValidationResponseDto;
import com.long_bus_distance.tickets.entity.TicketValidation;
import com.long_bus_distance.tickets.entity.TicketValidationMethodEnum;
import com.long_bus_distance.tickets.mapper.TicketValidationMapper;
import com.long_bus_distance.tickets.services.TicketValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ticket-validations")
@RequiredArgsConstructor
@Slf4j
public class TicketValidationController {
    private final TicketValidationService ticketValidationService;
    private final TicketValidationMapper ticketValidationMapper;
    //Xác thực vé qua mã QR hoặc nhập thủ công
    @PostMapping
    public ResponseEntity<TicketValidationResponseDto>validateTicket(@RequestBody TicketValidationRequestDto requestDto){
        log.info("Received POST request to validate ticket with ID: {} and method: {}",requestDto.getId(),requestDto.getMethod());
        //Khởi tạo kết quả xác thực
        TicketValidation validation;
        //Kiểm tra phương thức xác thực và gọi service
        if(requestDto.getMethod() == TicketValidationMethodEnum.MANUAL){
            validation = ticketValidationService.validateTicketManually(UUID.fromString(requestDto.getId()));
        }else {
            validation = ticketValidationService.validateTicketByQRCode(UUID.fromString(requestDto.getId()));
        }
        // Ánh xạ entity xác thực tới DTO phản hồi
        TicketValidationResponseDto responseDto =
                ticketValidationMapper.toTicketValidationResponseDto(validation);

        return ResponseEntity.ok(responseDto);
    }
}
