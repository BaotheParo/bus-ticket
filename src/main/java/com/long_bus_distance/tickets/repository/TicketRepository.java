package com.long_bus_distance.tickets.repository;

import com.long_bus_distance.tickets.entity.Ticket;
import com.long_bus_distance.tickets.entity.TicketStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

        @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deck.id = :deckId " +
                        "AND t.deck.trip.id = :tripId " +
                        "AND t.selectedSeat = :selectedSeat " +
                        "AND t.status IN :statuses")
        long countByTripIdAndDeckIdAndSelectedSeatAndStatusIn(
                        @Param("tripId") UUID tripId,
                        @Param("deckId") UUID deckId,
                        @Param("selectedSeat") String selectedSeat,
                        @Param("statuses") Collection<TicketStatusEnum> statuses);

        List<Ticket> findAllByStatusAndCreatedAtBefore(TicketStatusEnum status, LocalDateTime dateTime);

        long countByDeckId(UUID deckId);

        @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deck.id = :deckId AND t.deck.trip.id = :tripId AND t.selectedSeat = :selectedSeat")
        long countByTripIdAndDeckIdAndSelectedSeat(@Param("tripId") UUID tripId, @Param("deckId") UUID deckId,
                        @Param("selectedSeat") String selectedSeat);

        Page<Ticket> findByPurchaserId(UUID purchaserId, Pageable pageable);

        Optional<Ticket> findByIdAndPurchaserId(UUID ticketId, UUID purchaserId);

        List<Ticket> findAllByOrderGroupId(String orderGroupId);

        // Count sold cho remaining
        @Query("SELECT COUNT(t) FROM Ticket t " +
                        "WHERE t.status = 'PURCHASED' AND t.createdAt BETWEEN :start AND :end")
        Long getPlatformTicketsSoldBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        @Query("SELECT COALESCE(SUM(t.price), 0.0) FROM Ticket t " +
                        "WHERE t.status = 'PURCHASED' AND t.createdAt BETWEEN :start AND :end")
        Double getPlatformRevenueBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        // (ADMIN) Doanh thu theo ngày
        @Query("SELECT DATE(t.createdAt), SUM(t.price * 1.0) " +
                        "FROM Ticket t WHERE t.status = 'PURCHASED' AND t.createdAt BETWEEN :start AND :end " +
                        "GROUP BY DATE(t.createdAt) ORDER BY DATE(t.createdAt) ASC")
        List<Object[]> getAdminRevenueOverTime(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        // (ADMIN) Top 5 Nhà xe
        @Query("SELECT tr.operator.username, SUM(t.price * 1.0) " +
                        "FROM Ticket t JOIN t.deck d JOIN d.trip tr " +
                        "WHERE t.status = 'PURCHASED' AND t.createdAt BETWEEN :start AND :end " +
                        "GROUP BY tr.operator.username ORDER BY SUM(t.price) DESC")
        List<Object[]> getTopOperators(Pageable pageable, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // (OPERATOR) Tổng doanh thu
        @Query("SELECT COALESCE(SUM(t.price), 0.0) FROM Ticket t JOIN t.deck d JOIN d.trip tr " +
                        "WHERE tr.operator.id = :operatorId AND t.status = 'PURCHASED' AND t.createdAt BETWEEN :start AND :end")
        Double getOperatorRevenueBetweenDates(@Param("operatorId") UUID operatorId, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // (OPERATOR) Tổng vé bán
        @Query("SELECT COUNT(t) FROM Ticket t JOIN t.deck d JOIN d.trip tr " +
                        "WHERE tr.operator.id = :operatorId AND t.status = 'PURCHASED' AND t.createdAt BETWEEN :start AND :end")
        Long getOperatorTicketsSoldBetweenDates(@Param("operatorId") UUID operatorId,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        // (OPERATOR) Doanh thu theo ngày
        @Query("SELECT DATE(t.createdAt), SUM(t.price * 1.0) " +
                        "FROM Ticket t JOIN t.deck d JOIN d.trip tr " +
                        "WHERE tr.operator.id = :operatorId AND t.status = 'PURCHASED' AND t.createdAt BETWEEN :start AND :end "
                        +
                        "GROUP BY DATE(t.createdAt) ORDER BY DATE(t.createdAt) ASC")
        List<Object[]> getOperatorRevenueOverTime(@Param("operatorId") UUID operatorId,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        // (OPERATOR) Top 5 Tuyến
        @Query("SELECT tr.routeName, SUM(t.price * 1.0) " +
                        "FROM Ticket t JOIN t.deck d JOIN d.trip tr " +
                        "WHERE tr.operator.id = :operatorId AND t.status = 'PURCHASED' AND t.createdAt BETWEEN :start AND :end "
                        +
                        "GROUP BY tr.routeName ORDER BY SUM(t.price) DESC")
        List<Object[]> getTopTripsForOperator(Pageable pageable, @Param("operatorId") UUID operatorId,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}