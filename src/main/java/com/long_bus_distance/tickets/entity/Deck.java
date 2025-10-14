package com.long_bus_distance.tickets.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "decks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "label", nullable = false, length = 1)  // "A", "B",...
    private String label;

    @Column(name = "price_factor", nullable = false)
    private Double priceFactor = 1.0;  // Hệ số giá tầng

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats = 45;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_type_id")
    private BusType busType;  // Null nếu clone cho Trip

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;  // Set khi clone cho Trip cụ thể
}