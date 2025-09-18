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
import java.util.Objects;
import java.util.UUID;
@Entity
@Table(name = "ticket_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketType {
    @Id
    @Column(name = "id", nullable = false,updatable = false)
    private UUID id;

    @Column(name = "name",nullable = false)
    private String name; // VD:"Ghe", "Giuong nam tang tren", "Limousin tang duoi",..

    @Column(name = "price",nullable = false)
    private double price;

    @Column(name = "total_available", nullable = true)
    private Integer totalAvailable; // VD: toi da 17 cho moi tang

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "deck", nullable = true)
    private DeckEnum deck; //Tang tren, tang duoi

    //Moi quan he voi Trip
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    //Moi quan he voi Ticket
    @OneToMany(mappedBy = "ticketType", cascade = CascadeType.ALL)
    private List<Ticket> tickets = new ArrayList<>(); //Khoi tao de tranh loi NullPointerException

    //Audit field
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TicketType that = (TicketType) o;
        return Double.compare(price, that.price) == 0 && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(totalAvailable, that.totalAvailable) && deck == that.deck && Objects.equals(created_at, that.created_at) && Objects.equals(updated_at, that.updated_at);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, totalAvailable, deck, created_at, updated_at);
    }
}
