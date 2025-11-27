package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.Ticket;
import com.long_bus_distance.tickets.entity.User;
import com.long_bus_distance.tickets.exception.TicketNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.UUID;

public interface TicketService {
    String purchaseTicket(UUID userId, UUID tripId, UUID deckId, String selectedSeatPos);

    String purchaseBulkTickets(UUID userId, com.long_bus_distance.tickets.dto.BulkPurchaseRequestDto request);

    Page<Ticket> listTicketForUser(UUID userId, Pageable pageable);

    Optional<Ticket> getTicketForUser(UUID ticketId, UUID userId);

    Page<Ticket> listAllTickets(Optional<UUID> tripId, Optional<String> userEmail, Optional<String> status,
            Pageable pageable);

    Page<Ticket> listTicketsForOperator(UUID operatorId, Optional<UUID> tripId, Optional<String> userEmail,
            Optional<String> status, Pageable pageable);

    Ticket getTicketDetailsForAdminOrOperator(UUID ticketId, User currentUser)
            throws TicketNotFoundException, AccessDeniedException;

    Ticket cancelTicketForAdminOrOperator(UUID ticketId, User currentUser)
            throws TicketNotFoundException, AccessDeniedException;

    void processPaymentCallback(String ticketId, String responseCode);
}