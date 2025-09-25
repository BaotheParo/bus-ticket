package com.long_bus_distance.tickets.repository;

import com.long_bus_distance.tickets.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository  extends JpaRepository<Ticket, UUID> {
    //Tính số lượng vé dựa trên ID
    long countByTicketTypeId(UUID ticketTypeId);
    //Tìm vé dựa trên ID và phân trang
    Page<Ticket> findByPurchaserId (UUID purchaserId, Pageable pageable);
    //Tìm vé dựa trên ID và người dùng
    Optional<Ticket> findByIdAndPurchaserId(UUID ticketId, UUID purchaserId);
}
