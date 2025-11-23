package com.long_bus_distance.tickets.services.impl;

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
    @CacheEvict(value = "trips", allEntries = true) // <--- Xóa cache tìm kiếm khi có vé mới được bán
    public String purchaseTicket(UUID userId, UUID tripId, UUID deckId, String selectedSeatPos) {
        // 1. Validate cơ bản (User, Trip, Deck tồn tại?)
        // Logic này giữ nguyên để fail-fast (lỗi nhanh) trước khi tốn công lock
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User không tìm thấy: " + userId));
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new TicketSoldOutException("Deck không tìm thấy"));

        // 2. CHUẨN BỊ KHÓA (LOCK)
        // Key lock phải thật cụ thể: trip + deck + seat
        String fullSeat = deck.getLabel() + selectedSeatPos;
        String lockKey = "lock:ticket:trip:" + tripId + ":deck:" + deckId + ":seat:" + fullSeat;

        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 3. THỬ LẤY KHÓA
            // waitTime: 5s (chờ tối đa 5s để lấy khóa, nếu ko được thì thôi)
            // leaseTime: 10s (giữ khóa tối đa 10s rồi tự nhả, tránh deadlock nếu server sập)
            log.info("Đang thử lấy lock cho ghế: {}", fullSeat);
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);

            if (!isLocked) {
                // Trường hợp này xảy ra khi có 2 người cùng bấm nút "Mua" cực nhanh
                throw new TicketSoldOutException("Ghế " + fullSeat + " đang được người khác thao tác. Vui lòng thử lại!");
            }

            // --- VÙNG AN TOÀN (CRITICAL SECTION) ---
            log.info("Đã lấy được lock cho ghế: {}. Bắt đầu xử lý...", fullSeat);

            // 4. DOUBLE CHECK (Kiểm tra lại DB lần nữa cho chắc chắn)
            long seatOccupied = ticketRepository.countByTripIdAndDeckIdAndSelectedSeatAndStatusIn(
                    tripId, deckId, fullSeat,
                    List.of(TicketStatusEnum.PURCHASED, TicketStatusEnum.PENDING_PAYMENT)
            );

            if (seatOccupied > 0) {
                throw new TicketSoldOutException("Rất tiếc! Ghế " + fullSeat + " vừa có người đặt thành công.");
            }

            // 5. Logic tạo vé PENDING (Code cũ của bạn)
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new TicketSoldOutException("Trip không tìm thấy"));
            double price = trip.getBasePrice() * deck.getPriceFactor();

            Ticket ticket = new Ticket();
            ticket.setStatus(TicketStatusEnum.PENDING_PAYMENT);
            ticket.setPrice(price);
            ticket.setSelectedSeat(fullSeat);
            ticket.setDeck(deck);
            ticket.setPurchaser(user);

            Ticket savedTicket = ticketRepository.save(ticket);

            // 6. Lấy URL thanh toán
            String paymentUrl = vnPayService.createPaymentUrl(savedTicket);

            log.info("Giữ chỗ thành công ghế {}. Đang chuyển sang thanh toán.", fullSeat);
            return paymentUrl;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusTicketException("Lỗi hệ thống khi xử lý lock (Interrupted)");
        } finally {
            // 7. LUÔN LUÔN NHẢ KHÓA
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Đã nhả lock cho ghế: {}", fullSeat);
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

    @Override
    @Transactional
    public void processPaymentCallback(String ticketIdStr, String responseCode) {
        UUID ticketId = UUID.fromString(ticketIdStr);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Vé không tồn tại"));

        if ("00".equals(responseCode)) { // 00 là thành công của VNPay
            if (ticket.getStatus() == TicketStatusEnum.PENDING_PAYMENT) {
                ticket.setStatus(TicketStatusEnum.PURCHASED);
                ticketRepository.save(ticket);

                // Thanh toán xong mới tạo QR
                qrCodeService.generateQRCode(ticket);
                log.info("Thanh toán thành công vé ID: {}", ticketId);
            }
        } else {
            ticket.setStatus(TicketStatusEnum.FAILED);
            ticketRepository.save(ticket);
            log.warn("Thanh toán thất bại vé ID: {}", ticketId);
        }
    }

    private void checkOperatorTicketOwnership(Ticket ticket, UUID operatorId) throws AccessDeniedException {
        Trip trip = ticket.getDeck().getTrip();
        if (trip == null || trip.getOperator() == null || !trip.getOperator().getId().equals(operatorId)) {
            log.warn("Operator {} attempted to access ticket {} without ownership.", operatorId, ticket.getId());
            throw new AccessDeniedException("You do not have permission to access this ticket.");
        }
    }
}