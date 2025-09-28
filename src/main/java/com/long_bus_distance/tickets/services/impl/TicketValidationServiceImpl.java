package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.entity.*;
import com.long_bus_distance.tickets.exception.QRCodeGenerationException;
import com.long_bus_distance.tickets.exception.TicketTypeNotFoundException;
import com.long_bus_distance.tickets.repository.QRCodeRepository;
import com.long_bus_distance.tickets.repository.TicketRepository;
import com.long_bus_distance.tickets.repository.TicketValidationRepository;
import com.long_bus_distance.tickets.services.TicketValidationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketValidationServiceImpl implements TicketValidationService {
    private final QRCodeRepository qrCodeRepository;
    private final TicketRepository ticketRepository;
    private final TicketValidationRepository ticketValidationRepository;

    @Override
    public TicketValidation validateTicketByQRCode(UUID qrCodeId) {
        log.info("Validating ticket via QR Code ID: {}", qrCodeId);
        //Tìm mã QR hoạt động bằng ID
        QRCode qrCode = qrCodeRepository.findById(qrCodeId).filter(qr ->qr.getStatus() == QRCodeStatusEnum.ACTIVE)
                .orElseThrow(()-> new QRCodeGenerationException("Active QR code not found for ID: "+qrCodeId));
        //lLấy vé để đối chiếu
        Ticket ticket = qrCode.getTicket();
        //Xác thực vé bằng quét mã QR
        return validateTicket(ticket, TicketValidationMethodEnum.QR_SCAN);
    }

    @Override
    public TicketValidation validateTicketManually(UUID ticketId) {
        log.info("Validating ticket manually with ticket ID: {}", ticketId);
        //Tìm vé bằng ID
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()->new TicketTypeNotFoundException("Ticket not found for ID: "+ticketId));
        //Xác thực vé bằng nhập ID vé
        return validateTicket(ticket,TicketValidationMethodEnum.MANUAL);
    }

    //Logic xử lí xác thực vé
    private TicketValidation validateTicket(Ticket ticket, TicketValidationMethodEnum method){
        log.info("Processing validation for ticket ID: {} with method: {}", ticket.getId(), method);
        //Tạo ra đối tượng TicketValdation mới
        TicketValidation validation = new TicketValidation();
        validation.setTicket(ticket);
        validation.setValidationMethod(method);
        validation.setValidationTime(java.time.LocalDateTime.now());
        //kiểm tra vé đã được xác thực bao giờ chưa
        boolean hasValidValidation = ticket.getValidations().stream()
                .anyMatch(v -> v.getStatus() == TicketStatusEnum.PURCHASED);
        //Đặt vé là PURCHASED cho lần đầu xác thực và CANCELLED khi đã xác thực rồi
        validation.setStatus(hasValidValidation ? TicketStatusEnum.CANCELLED : TicketStatusEnum.PURCHASED);
        //Lưu xác thực đó vào DB
        return ticketValidationRepository.save(validation);

    }
}
