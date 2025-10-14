package com.long_bus_distance.tickets.repository;

import com.long_bus_distance.tickets.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeckRepository extends JpaRepository<Deck, UUID> {
    List<Deck> findByTripId(UUID tripId);  // Để lấy decks của Trip sau
}