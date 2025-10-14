package com.long_bus_distance.tickets.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "route_name", nullable = false)
    private String routeName;

    @Column(name = "departure_time", nullable = true)
    private LocalDateTime departureTime;

    @Column(name = "departure_point", nullable = false)
    private String departurePoint;

    @Column(name = "arrival_time", nullable = true)
    private LocalDateTime arrivalTime;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "duration_minutes", nullable = true)
    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_type_id", nullable = false)
    private BusType busType;

    @ElementCollection
    @CollectionTable(name = "trip_schedule", joinColumns = @JoinColumn(name = "trip_id"))
    @Column(name = "schedule_entry")
    private List<String> tripSchedule = new ArrayList<>();  // e.g., ["08:30 Hanoi"]

    @Column(name = "sales_start", nullable = true)
    private LocalDateTime saleStart;

    @Column(name = "sales_end", nullable = true)
    private LocalDateTime saleEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TripStatusEnum status;

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    private List<Deck> decks = new ArrayList<>();  // Cloned from BusType

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @ManyToMany(mappedBy = "bookedTrips")
    private List<User> passengers = new ArrayList<>();

    @ManyToMany(mappedBy = "staffingTrips")
    private List<User> staff = new ArrayList<>();

    // Audit
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(id, trip.id) && Objects.equals(routeName, trip.routeName) && Objects.equals(departureTime, trip.departureTime) && Objects.equals(departurePoint, trip.departurePoint) && Objects.equals(arrivalTime, trip.arrivalTime) && Objects.equals(destination, trip.destination) && Objects.equals(durationMinutes, trip.durationMinutes) && Objects.equals(busType, trip.busType) && Objects.equals(tripSchedule, trip.tripSchedule) && Objects.equals(saleStart, trip.saleStart) && Objects.equals(saleEnd, trip.saleEnd) && status == trip.status && Objects.equals(basePrice, trip.basePrice) && Objects.equals(decks, trip.decks) && Objects.equals(createdAt, trip.createdAt) && Objects.equals(updatedAt, trip.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, routeName, departureTime, departurePoint, arrivalTime, destination, durationMinutes, busType, tripSchedule, saleStart, saleEnd, status, basePrice, decks, createdAt, updatedAt);
    }
}