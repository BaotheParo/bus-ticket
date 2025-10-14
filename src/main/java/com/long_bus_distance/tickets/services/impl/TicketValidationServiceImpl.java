package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.entity.*;
import com.long_bus_distance.tickets.exception.TicketNotFoundException;
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
public class TicketValidationServiceImpl implements TicketValidationService {
    private final QRCodeRepository qrCodeRepository;
    private final TicketRepository ticketRepository;
    private final TicketValidationRepository ticketValidationRepository;

    @Override
    @Transactional
    public TicketValidation validateTicketByQRCode(UUID qrId) {
        log.info("Xác thực vé qua QR ID: {}", qrId);
        // Tìm QR ACTIVE
        QRCode qrCode = qrCodeRepository.findById(qrId)
                .filter(qr -> qr.getStatus() == QRCodeStatusEnum.ACTIVE)
                .orElseThrow(() -> new TicketNotFoundException("QR Code không hợp lệ hoặc hết hạn: " + qrId));

        // Parse content to ticketId (giả sử content = ticketId-selectedSeat)
        String content = qrCode.getValue();  // Thực tế decode QR image, nhưng vì lưu content, dùng trực tiếp
        UUID ticketId = UUID.fromString(content.split("-")[0]);  // Extract ticketId từ content

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket không tìm thấy từ QR: " + ticketId));

        return validateTicket(ticket, TicketValidationMethodEnum.QR_SCAN);
    }

    @Override
    @Transactional
    public TicketValidation validateTicketManually(UUID ticketId) {
        log.info("Xác thực vé thủ công ID: {}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket không tìm thấy: " + ticketId));
        return validateTicket(ticket, TicketValidationMethodEnum.MANUAL);
    }

    // Private method chung
    private TicketValidation validateTicket(Ticket ticket, TicketValidationMethodEnum method) {
        log.info("Xử lý validation cho Ticket ID: {} với method: {}", ticket.getId(), method);

        // Check nếu đã validate (first = PURCHASED, duplicate = CANCELLED)
        boolean isFirstValidation = ticket.getValidations().stream()
                .noneMatch(v -> v.getStatus() == TicketStatusEnum.PURCHASED);

        TicketStatusEnum newStatus = isFirstValidation ? TicketStatusEnum.PURCHASED : TicketStatusEnum.CANCELLED;

        // Tạo validation mới
        TicketValidation validation = TicketValidation.builder()
                .status(newStatus)
                .validationMethod(method)
                .ticket(ticket)
                .build();

        TicketValidation saved = ticketValidationRepository.save(validation);
        log.info("Validation thành công ID: {} cho Ticket: {}", saved.getId(), ticket.getId());
        return saved;
    }
}