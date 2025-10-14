package com.long_bus_distance.tickets.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bus_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;  // e.g., "Xe ghế ngồi"

    @Column(name = "description")
    private String description;

    @Column(name = "num_decks", nullable = false)
    private Integer numDecks = 1;  // Số tầng

    @Column(name = "seats_per_deck", nullable = false)
    private Integer seatsPerDeck = 45;  // Chỗ/tầng mặc định

    @Column(name = "price_factor", nullable = false)
    private Double priceFactor = 1.0;  // Hệ số tổng cho loại xe

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;  // Không xóa 3 mặc định

    @OneToMany(mappedBy = "busType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deck> defaultDecks = new ArrayList<>();  // Tầng mặc định

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Method auto-generate decks khi tạo
    public void generateDefaultDecks() {
        defaultDecks.clear();
        for (int i = 1; i <= numDecks; i++) {
            Deck deck = new Deck();
            deck.setLabel(getDeckLabel(i));  // A=1, B=2,...
            deck.setPriceFactor(1.0 + (i * 0.1));  // Tầng cao hơn +10%
            deck.setTotalSeats(seatsPerDeck);
            deck.setBusType(this);
            defaultDecks.add(deck);
        }
    }

    private String getDeckLabel(int index) {
        return switch (index) {
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            case 4 -> "D";
            default -> throw new IllegalArgumentException("Chỉ hỗ trợ tối đa 4 tầng");
        };
    }
}