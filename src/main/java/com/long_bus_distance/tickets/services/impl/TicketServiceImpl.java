package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.entity.*;
import com.long_bus_distance.tickets.exception.TicketNotFoundException;
import com.long_bus_distance.tickets.exception.TicketSoldOutException;
import com.long_bus_distance.tickets.exception.UserNotFoundException;
import com.long_bus_distance.tickets.repository.DeckRepository;
import com.long_bus_distance.tickets.repository.TicketRepository;
import com.long_bus_distance.tickets.repository.TripRepository;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.services.QRCodeService;
import com.long_bus_distance.tickets.services.TicketService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public Page<Ticket> listAllTickets(Optional<UUID> tripId, Optional<String> userEmail, Optional<String> status, Pageable pageable) {
        log.info("Admin listing all tickets with filters: tripId={}, userEmail={}, status={}", tripId, userEmail, status);
        Specification<Ticket> spec = createTicketSpecification(tripId, userEmail, status, Optional.empty());
        return ticketRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Ticket> listTicketsForOperator(UUID operatorId, Optional<UUID> tripId, Optional<String> userEmail, Optional<String> status, Pageable pageable) {
        log.info("Operator {} listing tickets with filters: tripId={}, userEmail={}, status={}", operatorId, tripId, userEmail, status);
        // Tạo spec với bộ lọc
        Specification<Ticket> spec = createTicketSpecification(tripId, userEmail, status, Optional.of(operatorId));
        return ticketRepository.findAll(spec, pageable);
    }

    /**
     * Helper private để xây dựng Specification cho việc lọc vé.
     */
    private Specification<Ticket> createTicketSpecification(Optional<UUID> tripId, Optional<String> userEmail, Optional<String> status, Optional<UUID> operatorId) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo operatorId (join qua Deck -> Trip)
            operatorId.ifPresent(opId -> {
                predicates.add(criteriaBuilder.equal(root.get("deck").get("trip").get("operator").get("id"), opId));
            });

            // Lọc theo tripId (join qua Deck)
            tripId.ifPresent(tId -> {
                predicates.add(criteriaBuilder.equal(root.get("deck").get("trip").get("id"), tId));
            });

            // Lọc theo email người mua (join qua User)
            userEmail.ifPresent(email -> {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("purchaser").get("email")), "%" + email.toLowerCase() + "%"));
            });

            // Lọc theo trạng thái vé
            status.ifPresent(s -> {
                try {
                    TicketStatusEnum statusEnum = TicketStatusEnum.valueOf(s.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                    // Bỏ qua nếu status không hợp lệ
                    log.warn("Invalid status filter ignored: {}", s);
                }
            });

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public Ticket getTicketDetailsForAdminOrOperator(UUID ticketId, User currentUser) throws TicketNotFoundException, AccessDeniedException {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));

        // Nếu là Operator, phải kiểm tra quyền sở hữu
        if (currentUser.getRoles().contains("ROLE_OPERATOR")) {
            checkOperatorTicketOwnership(ticket, currentUser.getId());
        }

        // Admin có thể xem mọi vé
        return ticket;
    }

    @Override
    @Transactional
    public Ticket cancelTicketForAdminOrOperator(UUID ticketId, User currentUser) throws TicketNotFoundException, AccessDeniedException {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID:"+ "Ex" + ticketId));

                        // Nếu là Operator, phải kiểm tra quyền sở hữu
        if (currentUser.getRoles().contains("ROLE_OPERATOR")) {
            checkOperatorTicketOwnership(ticket, currentUser.getId());
        }

        log.info("Cancelling ticket {} by user {}", ticketId, currentUser.getUsername());
        ticket.setStatus(TicketStatusEnum.CANCELLED);
        // (Trong một dự án thực tế, ở đây có thể thêm logic hoàn tiền, v.v.)

        return ticketRepository.save(ticket);
    }

    private void checkOperatorTicketOwnership(Ticket ticket, UUID operatorId) throws AccessDeniedException {
        Trip trip = ticket.getDeck().getTrip();
        if (trip == null || trip.getOperator() == null || !trip.getOperator().getId().equals(operatorId)) {
            log.warn("Operator {} attempted to access ticket {} without ownership.", operatorId, ticket.getId());
            throw new AccessDeniedException("You do not have permission to access this ticket.");
        }
    }
}