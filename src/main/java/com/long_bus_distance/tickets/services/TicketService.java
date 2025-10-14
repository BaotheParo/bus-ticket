package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TicketService {
    Ticket purchaseTicket(UUID userId, UUID tripId, UUID deckId, String selectedSeatPos);  // Pos "2"
    Page<Ticket> listTicketForUser(UUID userId, Pageable pageable);
    Optional<Ticket> getTicketForUser(UUID ticketId, UUID userId);
}