package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.entity.*;
import com.long_bus_distance.tickets.exception.TicketSoldOutException;
import com.long_bus_distance.tickets.exception.TicketTypeNotFoundException;
import com.long_bus_distance.tickets.exception.UserNotFoundException;
import com.long_bus_distance.tickets.repository.TicketRepository;
import com.long_bus_distance.tickets.repository.TicketTypeRepository;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.services.QRCodeService;
import com.long_bus_distance.tickets.services.TicketTypeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketTypeServiceImpl implements TicketTypeService {
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final QRCodeService qrCodeService;
    @Override
    public Ticket purchaseTicket(UUID userId, UUID ticketTypeId) {

        // Nhận giá trị user
        log.info("Processingt ticket purchase for user ID: {} and ticket type ID: {}", userId,ticketTypeId);
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User with ID "+ userId + " not found"));

        //Lấy lại loại vé có khóa pessimistic (cho trường hợp 2 người mua cùng lúc)
        Optional<TicketType> optionalTicketType = ticketTypeRepository.findByIdWithLock(ticketTypeId);
        TicketType ticketType = optionalTicketType.orElseThrow(()-> new TicketTypeNotFoundException("Ticket type with ID " + ticketTypeId + " not found"));

        //Kiểm tra vé còn số lượng hoặc còn tồn tại hay không
        long purchasedTickets = ticketRepository.countByTicketTypeId(ticketTypeId);
        int totalAvailable = ticketType.getTotalAvailable();
        if (purchasedTickets + 1 > totalAvailable){
            log.error("Ticket type ID: {} is sold out. Purchased: {}, Available: {}", ticketTypeId, purchasedTickets, totalAvailable);
            throw new TicketSoldOutException("Ticket type with ID " + ticketTypeId + " is sold out");
        }

        //Tạo và lưu vé
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setPurchaser(user);

        //Lưu vé vào DB
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Created ticket with ID: {} for user ID: {}", savedTicket.getId(), userId);

        //Tạo QR code cho vé
        QRCode qrCode = qrCodeService.generateQRCode(savedTicket);

        //Lưu vé lần nữa để đảm bảo liên kết được với QR (nếu cần)
        savedTicket= ticketRepository.save(savedTicket);
        log.info("Updated ticket with ID: {} with QR code ID: {}", savedTicket.getId(), qrCode.getId());

        // Trả về vé đã lưu
        return savedTicket;
    }
}
