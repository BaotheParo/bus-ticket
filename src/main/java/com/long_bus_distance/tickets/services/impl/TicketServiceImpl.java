package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.entity.*;
import com.long_bus_distance.tickets.exception.TicketSoldOutException;
import com.long_bus_distance.tickets.exception.UserNotFoundException;
import com.long_bus_distance.tickets.repository.DeckRepository;
import com.long_bus_distance.tickets.repository.TicketRepository;
import com.long_bus_distance.tickets.repository.TripRepository;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.services.QRCodeService;
import com.long_bus_distance.tickets.services.TicketService;
import jakarta.transaction.Transactional;
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
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final DeckRepository deckRepository;
    private final TicketRepository ticketRepository;
    private final QRCodeService qrCodeService;

    @Override
    @Transactional
    public Ticket purchaseTicket(UUID userId, UUID tripId, UUID deckId, String selectedSeatPos) {
        log.info("Mua vé cho user {}, trip {}, deck {}, pos {}", userId, tripId, deckId, selectedSeatPos);

        // Tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User không tìm thấy: " + userId));

        // Tìm trip và deck
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TicketSoldOutException("Trip không tìm thấy"));
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new TicketSoldOutException("Deck không tìm thấy"));
        if (!deck.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Deck không thuộc Trip");
        }

        // Normalize selectedSeat
        String fullSeat = deck.getLabel() + selectedSeatPos;  // "B" + "2" = "B2"
        int pos;
        try {
            pos = Integer.parseInt(selectedSeatPos);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Selected seat phải là số (e.g., '2')");
        }
        if (pos < 1 || pos > deck.getTotalSeats()) {
            throw new IllegalArgumentException("Vị trí không hợp lệ: " + pos + " (tầng có " + deck.getTotalSeats() + " chỗ)");
        }

        // Check seat cụ thể đã sold chưa
        long seatSold = ticketRepository.countByTripIdAndDeckIdAndSelectedSeat(tripId, deckId, fullSeat);
        if (seatSold > 0) {
            throw new TicketSoldOutException("Ghế " + fullSeat + " đã được đặt");
        }

        // Check total sold out per deck
        long totalSold = ticketRepository.countByDeckId(deckId);
        if (totalSold + 1 > deck.getTotalSeats()) {
            throw new TicketSoldOutException("Tầng " + deck.getLabel() + " đã hết chỗ");
        }

        // Tính price
        double price = trip.getBasePrice() * deck.getPriceFactor();

        // Tạo Ticket
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setPrice(price);
        ticket.setSelectedSeat(fullSeat);
        ticket.setDeck(deck);
        ticket.setPurchaser(user);

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Tạo thành công Ticket ID: {} cho ghế {}", savedTicket.getId(), fullSeat);

        // Tạo QRCode (content include fullSeat)
        qrCodeService.generateQRCode(savedTicket);

        return savedTicket;
    }

    @Override
    public Page<Ticket> listTicketForUser(UUID userId, Pageable pageable) {
        log.info("Liệt kê vé cho user: {} với phân trang {}", userId, pageable);
        return ticketRepository.findByPurchaserId(userId, pageable);
    }

    @Override
    public Optional<Ticket> getTicketForUser(UUID ticketId, UUID userId) {
        log.info("Lấy vé ID: {} cho user: {}", ticketId, userId);
        return ticketRepository.findByIdAndPurchaserId(ticketId, userId);
    }
}