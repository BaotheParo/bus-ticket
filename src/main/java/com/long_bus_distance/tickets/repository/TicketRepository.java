package com.long_bus_distance.tickets.repository;

import com.long_bus_distance.tickets.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    // Count sold cho remaining
    long countByDeckId(UUID deckId);

    // Check seat sold cụ thể (FIX: Sửa t.trip.id → t.deck.trip.id)
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deck.id = :deckId AND t.deck.trip.id = :tripId AND t.selectedSeat = :selectedSeat")
    long countByTripIdAndDeckIdAndSelectedSeat(@Param("tripId") UUID tripId, @Param("deckId") UUID deckId, @Param("selectedSeat") String selectedSeat);

    // List vé cho user
    Page<Ticket> findByPurchaserId(UUID purchaserId, Pageable pageable);

    // Get vé cụ thể cho user
    Optional<Ticket> findByIdAndPurchaserId(UUID ticketId, UUID purchaserId);
}