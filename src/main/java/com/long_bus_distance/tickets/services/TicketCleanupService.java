package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.entity.Ticket;
import com.long_bus_distance.tickets.entity.TicketStatusEnum;
import com.long_bus_distance.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketCleanupService {

    private final TicketRepository ticketRepository;

    // Chạy mỗi 60 giây (60000 ms)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupPendingTickets() {
        // Thời gian hết hạn: Vé tạo cách đây quá 15 phút
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);

        List<Ticket> expiredTickets = ticketRepository.findAllByStatusAndCreatedAtBefore(
                TicketStatusEnum.PENDING_PAYMENT, 
                expirationTime
        );

        if (!expiredTickets.isEmpty()) {
            log.info("Tìm thấy {} vé quá hạn thanh toán. Đang hủy...", expiredTickets.size());
            for (Ticket ticket : expiredTickets) {
                ticket.setStatus(TicketStatusEnum.CANCELLED);
                // Có thể thêm logic gửi email báo hủy vé ở đây
            }
            ticketRepository.saveAll(expiredTickets);
        }
    }
}