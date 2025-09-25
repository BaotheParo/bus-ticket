package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.entity.Ticket;
import com.long_bus_distance.tickets.repository.TicketRepository;
import com.long_bus_distance.tickets.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;

    @Override
    public Page<Ticket> listTicketForUser(UUID userId, Pageable pageable) {
        log.info("Listing request for user ID: {} with pagination: {}",userId,pageable );
        //Gọi repo để lấy vé theo ID user
        return ticketRepository.findByPurchaserId(userId,pageable);
    }

    @Override
    public Optional<Ticket> getTicketForUser(UUID ticketId, UUID userId) {
        log.info("Retrieving ticket ID: {} for user ID: {}", ticketId,userId);
        //Gọi repo để lấy vé theo ID và ID user
        return ticketRepository.findByIdAndPurchaserId(ticketId,userId);
    }
}
