package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.Ticket;

import java.util.UUID;

public interface TicketTypeService {
    Ticket purchaseTicket(UUID userId, UUID ticketTypeId);
}
