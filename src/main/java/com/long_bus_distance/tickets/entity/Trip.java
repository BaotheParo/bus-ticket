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

    @Column(name="departure_time",nullable = true)
    private LocalDateTime departureTime;

    @Column(name = "departure_point", nullable = false)
    private String departurePoint;

    @Column(name="arrival_time",nullable = true)
    private LocalDateTime arrivalTime;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "duration_minutes", nullable=true)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "bus_type",nullable = false)
    private BusTypeEnum busType;

    @ElementCollection
    @Column(name = "trip_schedule")
    private List<String> tripSchedule;
    // VD: ["08:30 Ha Noi", "12:00 Ca Mau"]

    @Column(name = "sales_start", nullable = true)
    private LocalDateTime saleStart;

    @Column(name = "sales_end", nullable = true)
    private LocalDateTime saleEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TripStatusEnum status;

    //Audit fields (Dung de thong ke trong tuong lai)
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updateAt;

    //Moi quan he CSDL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator; //Nha dieu hanh quan li chuyen di

    @ManyToMany(mappedBy = "bookedTrips")
    private List<User> passengers; // Khach hang dat chuyen di

    @ManyToMany(mappedBy = "staffingTrips")
    private List<User> staff; //Nhan vien xe bus

    @OneToMany(mappedBy = "trip",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketType> ticketTypes = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(id, trip.id) && Objects.equals(routeName, trip.routeName) && Objects.equals(departureTime, trip.departureTime) && Objects.equals(departurePoint, trip.departurePoint) && Objects.equals(arrivalTime, trip.arrivalTime) && Objects.equals(destination, trip.destination) && Objects.equals(durationMinutes, trip.durationMinutes) && busType == trip.busType && Objects.equals(tripSchedule, trip.tripSchedule) && Objects.equals(saleStart, trip.saleStart) && Objects.equals(saleEnd, trip.saleEnd) && status == trip.status && Objects.equals(createAt, trip.createAt) && Objects.equals(updateAt, trip.updateAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, routeName, departureTime, departurePoint, arrivalTime, destination, durationMinutes, busType, tripSchedule, saleStart, saleEnd, status, createAt, updateAt);
    }
}
