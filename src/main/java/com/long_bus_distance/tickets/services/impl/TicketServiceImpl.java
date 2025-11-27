package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.dto.BookingSeatRequest;
import com.long_bus_distance.tickets.dto.PurchaseTicketRequestDto;
import com.long_bus_distance.tickets.entity.*;
import com.long_bus_distance.tickets.exception.BusTicketException;
import com.long_bus_distance.tickets.exception.TicketNotFoundException;
import com.long_bus_distance.tickets.exception.TicketSoldOutException;
import com.long_bus_distance.tickets.exception.UserNotFoundException;
import com.long_bus_distance.tickets.repository.DeckRepository;
import com.long_bus_distance.tickets.repository.TicketRepository;
import com.long_bus_distance.tickets.repository.TripRepository;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.services.QRCodeService;
import com.long_bus_distance.tickets.services.TicketService;
import com.long_bus_distance.tickets.services.VNPayService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final DeckRepository deckRepository;
    private final TicketRepository ticketRepository;
    private final QRCodeService qrCodeService;

    private final RedissonClient redissonClient;

    private final VNPayService vnPayService;

    @Override
    @Transactional
    @CacheEvict(value = "trips", allEntries = true)
    public String purchaseTicket(UUID userId, PurchaseTicketRequestDto request) {
        // 1. Validate User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User không tồn tại"));

        // Tạo một mã giao dịch chung (Order Group ID)
        String orderGroupId = UUID.randomUUID().toString();
        List<RLock> locks = new ArrayList<>();
        List<Ticket> ticketsToSave = new ArrayList<>();
        double totalAmount = 0.0;

        // 2. CHUẨN BỊ KHÓA (LOCK) CHO TẤT CẢ GHẾ
        for (BookingSeatRequest seatReq : request.getBookingSeats()) {
            // Lấy thông tin Deck để check và lấy label (A/B)
            Deck deck = deckRepository.findById(seatReq.getDeckId())
                    .orElseThrow(() -> new TicketSoldOutException("Deck không tồn tại"));

            String fullSeat = deck.getLabel() + seatReq.getSelectedSeat();

            // Tạo key lock: trip + deck + seat
            String lockKey = "lock:ticket:trip:" + seatReq.getTripId() +
                    ":deck:" + seatReq.getDeckId() +
                    ":seat:" + fullSeat;

            locks.add(redissonClient.getLock(lockKey));
        }

        // Gộp tất cả lock đơn lẻ thành 1 MultiLock
        RLock multiLock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));
        boolean isLocked = false;

        try {
            // 3. THỬ LẤY KHÓA (Atomic: Được ăn cả, ngã về không)
            // Chờ 5s, giữ lock 10s
            isLocked = multiLock.tryLock(5, 10, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new TicketSoldOutException(
                        "Một trong số các ghế bạn chọn đang được người khác thao tác. Vui lòng thử lại!");
            }

            // --- VÙNG AN TOÀN (CRITICAL SECTION) ---

            for (BookingSeatRequest seatReq : request.getBookingSeats()) {
                Deck deck = deckRepository.getReferenceById(seatReq.getDeckId()); // Lấy ref cho nhanh
                Trip trip = tripRepository.findById(seatReq.getTripId()).orElseThrow();
                String fullSeat = deck.getLabel() + seatReq.getSelectedSeat();

                // 4. DOUBLE CHECK DB (Kiểm tra xem ghế đã bán chưa)
                long seatOccupied = ticketRepository.countByTripIdAndDeckIdAndSelectedSeatAndStatusIn(
                        seatReq.getTripId(), seatReq.getDeckId(), fullSeat,
                        List.of(TicketStatusEnum.PURCHASED, TicketStatusEnum.PENDING_PAYMENT));

                if (seatOccupied > 0) {
                    throw new TicketSoldOutException("Ghế " + fullSeat + " đã bị đặt trước đó.");
                }

                // 5. Tạo Entity Ticket
                Ticket ticket = new Ticket();
                ticket.setStatus(TicketStatusEnum.PENDING_PAYMENT);
                double price = trip.getBasePrice() * deck.getPriceFactor();
                ticket.setPrice(price);
                ticket.setSelectedSeat(fullSeat);
                ticket.setDeck(deck);
                ticket.setPurchaser(user);
                ticket.setOrderGroupId(orderGroupId); // <-- QUAN TRỌNG: Gán Group ID

                ticketsToSave.add(ticket);
                totalAmount += price;
            }

            // Lưu tất cả vé xuống DB cùng lúc
            ticketRepository.saveAll(ticketsToSave);

            // 6. Tạo URL thanh toán VNPay
            // Cần cập nhật VNPayService để nhận totalAmount và orderGroupId thay vì 1
            // ticket
            String paymentUrl = vnPayService.createPaymentUrl(orderGroupId, totalAmount);

            return paymentUrl;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusTicketException("Lỗi hệ thống khi xử lý lock");
        } finally {
            // 7. NHẢ KHÓA
            if (isLocked) {
                multiLock.unlock();
            }
        }
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
    public Page<Ticket> listAllTickets(Optional<UUID> tripId, Optional<String> userEmail, Optional<String> status,
            Pageable pageable) {
        log.info("Admin listing all tickets with filters: tripId={}, userEmail={}, status={}", tripId, userEmail,
                status);
        Specification<Ticket> spec = createTicketSpecification(tripId, userEmail, status, Optional.empty());
        return ticketRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Ticket> listTicketsForOperator(UUID operatorId, Optional<UUID> tripId, Optional<String> userEmail,
            Optional<String> status, Pageable pageable) {
        log.info("Operator {} listing tickets with filters: tripId={}, userEmail={}, status={}", operatorId, tripId,
                userEmail, status);
        // Tạo spec với bộ lọc
        Specification<Ticket> spec = createTicketSpecification(tripId, userEmail, status, Optional.of(operatorId));
        return ticketRepository.findAll(spec, pageable);
    }

    /**
     * Helper private để xây dựng Specification cho việc lọc vé.
     */
    private Specification<Ticket> createTicketSpecification(Optional<UUID> tripId, Optional<String> userEmail,
            Optional<String> status, Optional<UUID> operatorId) {

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
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("purchaser").get("email")),
                        "%" + email.toLowerCase() + "%"));
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
    public Ticket getTicketDetailsForAdminOrOperator(UUID ticketId, User currentUser)
            throws TicketNotFoundException, AccessDeniedException {
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
    public Ticket cancelTicketForAdminOrOperator(UUID ticketId, User currentUser)
            throws TicketNotFoundException, AccessDeniedException {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID:" + "Ex" + ticketId));

        // Nếu là Operator, phải kiểm tra quyền sở hữu
        if (currentUser.getRoles().contains("ROLE_OPERATOR")) {
            checkOperatorTicketOwnership(ticket, currentUser.getId());
        }

        log.info("Cancelling ticket {} by user {}", ticketId, currentUser.getUsername());
        ticket.setStatus(TicketStatusEnum.CANCELLED);
        // (Trong một dự án thực tế, ở đây có thể thêm logic hoàn tiền, v.v.)

        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public void processPaymentCallback(String orderGroupId, String responseCode) {
        List<Ticket> tickets = ticketRepository.findAllByOrderGroupId(orderGroupId);

        if (tickets.isEmpty()) {
            // Fallback: try to find by ticket ID if it's a UUID (old flow)
            try {
                UUID ticketId = UUID.fromString(orderGroupId);
                Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
                if (ticketOpt.isPresent()) {
                    tickets = List.of(ticketOpt.get());
                } else {
                    throw new TicketNotFoundException("Không tìm thấy giao dịch: " + orderGroupId);
                }
            } catch (IllegalArgumentException e) {
                throw new TicketNotFoundException("Không tìm thấy giao dịch: " + orderGroupId);
            }
        }

        TicketStatusEnum newStatus = "00".equals(responseCode) ? TicketStatusEnum.PURCHASED : TicketStatusEnum.FAILED;

        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == TicketStatusEnum.PENDING_PAYMENT) {
                ticket.setStatus(newStatus);
                if (newStatus == TicketStatusEnum.PURCHASED) {
                    qrCodeService.generateQRCode(ticket); // Tạo QR cho từng vé
                }
            }
        }
        ticketRepository.saveAll(tickets);
    }

    private void checkOperatorTicketOwnership(Ticket ticket, UUID operatorId) throws AccessDeniedException {
        Trip trip = ticket.getDeck().getTrip();
        if (trip == null || trip.getOperator() == null || !trip.getOperator().getId().equals(operatorId)) {
            log.warn("Operator {} attempted to access ticket {} without ownership.", operatorId, ticket.getId());
            throw new AccessDeniedException("You do not have permission to access this ticket.");
        }
    }
}