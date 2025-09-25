package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TicketService {
    //Liệt kê toàn bộ vé cho user cụ thể với phân trang
    Page<Ticket> listTicketForUser(UUID userId, Pageable pageable);

    //Lấy vé cho người dùng
    Optional<Ticket> getTicketForUser(UUID ticketId, UUID userId);
}
