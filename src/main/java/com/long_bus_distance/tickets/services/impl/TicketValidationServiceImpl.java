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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException; // Optional for better error handling
import org.springframework.http.HttpStatus; // Optional for better error handling


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
        QRCode qrCode = qrCodeRepository.findById(qrId)
                .filter(qr -> qr.getStatus() == QRCodeStatusEnum.ACTIVE)
                .orElseThrow(() -> new TicketNotFoundException("QR Code không hợp lệ hoặc hết hạn: " + qrId));

        String content = qrCode.getValue();
        // Giả định content chứa UUID vé trước dấu '-'
        UUID ticketId;
        try {
            ticketId = UUID.fromString(content.split("-")[0]);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            log.error("Không thể parse ticketId từ QR value: {}", content);
            throw new TicketNotFoundException("QR Code content không hợp lệ.");
        }


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

    // Private method chung - Đã cập nhật cách lấy staffId
    private TicketValidation validateTicket(Ticket ticket, TicketValidationMethodEnum method) {
        log.info("Xử lý validation cho Ticket ID: {} với method: {}", ticket.getId(), method);

        // --- START: Cập nhật Logic kiểm tra quyền của nhân viên ---
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Nhân viên chưa được xác thực.");
        }

        Object principal = authentication.getPrincipal();
        UUID staffId;

        if (principal instanceof User) {
            staffId = ((User) principal).getId();
        } else if (principal instanceof UserDetails) {
            // Trường hợp principal là UserDetails nhưng không phải User entity
            // Cần có cách lấy ID (ví dụ: query lại DB bằng username) - Tạm thời báo lỗi
            log.warn("Principal is UserDetails but not the expected User entity type.");
            throw new AccessDeniedException("Không thể xác định ID nhân viên từ thông tin xác thực.");
        }
        else {
            throw new AccessDeniedException("Loại thông tin xác thực không mong đợi.");
        }

        Trip trip = ticket.getDeck().getTrip();
        if (trip == null) {
            log.error("Không thể lấy thông tin Trip từ Ticket ID: {}", ticket.getId());
            throw new TicketNotFoundException("Không tìm thấy thông tin chuyến đi liên kết với vé.");
        }


        // Kiểm tra xem staffId có nằm trong danh sách nhân viên được gán cho chuyến đi không
        boolean isAssigned = trip.getStaff().stream()
                .anyMatch(staff -> staff.getId().equals(staffId));

        if (!isAssigned) {
            log.warn("Nhân viên {} cố gắng validate vé cho chuyến đi {} mà không được phân công.", staffId, trip.getId());
            throw new AccessDeniedException("Bạn không được phân công để xác thực vé cho chuyến đi này.");
        }
        // --- END: Cập nhật Logic kiểm tra quyền của nhân viên ---

        // Logic kiểm tra vé đã validate chưa và tạo bản ghi validation (giữ nguyên)
        boolean isFirstValidation = ticket.getValidations().stream()
                .noneMatch(v -> v.getStatus() == TicketStatusEnum.PURCHASED);

        TicketStatusEnum newStatus = isFirstValidation ? TicketStatusEnum.PURCHASED : TicketStatusEnum.CANCELLED;
        if (!isFirstValidation) {
            log.warn("Vé {} đã được xác thực trước đó. Validation lần này sẽ ghi nhận là CANCELLED.", ticket.getId());
        }


        TicketValidation validation = TicketValidation.builder()
                .status(newStatus)
                .validationMethod(method)
                .ticket(ticket)
                // validationTime sẽ được tự động gán bởi @CreatedDate
                .build();

        TicketValidation saved = ticketValidationRepository.save(validation);
        log.info("Validation thành công ID: {} cho Ticket: {} với trạng thái {}", saved.getId(), ticket.getId(), newStatus);
        return saved;
    }
}
