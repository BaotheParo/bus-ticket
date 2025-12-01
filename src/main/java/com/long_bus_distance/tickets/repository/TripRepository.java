package com.long_bus_distance.tickets.repository;

import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.entity.TripStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

        Page<Trip> findByOperatorId(UUID organizerId, Pageable pageable);

        Optional<Trip> findByIdAndOperatorId(UUID tripId, UUID operatorId);

        Page<Trip> findByStatus(TripStatusEnum status, Pageable pageable);

        @Query(value = "SELECT * FROM trips t WHERE t.status = 'PUBLISHED' " +
                        "AND to_tsvector('simple', t.route_name || ' ' || t.departure_point || ' ' || t.destination) @@ to_tsquery('simple', :searchTerm)", nativeQuery = true)
        Page<Trip> searchTrips(@Param("searchTerm") String searchTerm, Pageable pageable);

        Optional<Trip> findByIdAndStatus(UUID id, TripStatusEnum status);

        @Modifying
        @Query("UPDATE Trip t SET t.busType.id = :undefinedId WHERE t.busType.id = :oldId")
        void updateBusTypeToUndefined(@Param("oldId") UUID oldId, @Param("undefinedId") UUID undefinedId);

        // Base query with routeName and operatorId
        @Query(value = """
                        SELECT t.id, t.route_name, t.departure_time, t.departure_point, t.arrival_time, t.destination,
                               COALESCE(SUM(d.total_seats - COALESCE(bc.booked_count, 0)), 0) as total_available,
                               u.id as operator_id,
                               (u.firstname || ' ' || u.lastname) as operator_name
                        FROM trips t
                        JOIN bus_types bt ON t.bus_type_id = bt.id
                        JOIN decks d ON d.trip_id = t.id
                        JOIN users u ON t.operator_id = u.id
                        LEFT JOIN (
                            SELECT deck_id, COUNT(*) as booked_count
                            FROM tickets ti
                            WHERE ti.status = 'PURCHASED' AND ti.selected_seat IS NOT NULL
                            GROUP BY deck_id
                        ) bc ON bc.deck_id = d.id
                        WHERE t.status = 'PUBLISHED'
                          AND (:departurePoint IS NULL OR t.departure_point = :departurePoint)
                          AND (:destination IS NULL OR t.destination = :destination)
                          AND (cast(:departureDate as date) IS NULL OR DATE(t.departure_time) = :departureDate)
                          AND (:routeName IS NULL OR t.route_name ILIKE '%' || :routeName || '%')
                          AND (cast(:operatorId as uuid) IS NULL OR t.operator_id = :operatorId)
                        GROUP BY t.id, t.route_name, t.departure_time, t.departure_point, t.arrival_time, t.destination, u.id, u.firstname, u.lastname
                        HAVING COALESCE(SUM(d.total_seats - COALESCE(bc.booked_count, 0)), 0) >= :numTickets
                        """, nativeQuery = true)
        Page<Object[]> searchPublishedTripsBase(
                        @Param("departurePoint") String departurePoint,
                        @Param("destination") String destination,
                        @Param("departureDate") LocalDate departureDate,
                        @Param("numTickets") int numTickets,
                        @Param("routeName") String routeName,
                        @Param("operatorId") UUID operatorId,
                        Pageable pageable);

        // Filtered query with routeName and operatorId
        @Query(value = """
                        SELECT t.id, t.route_name, t.departure_time, t.departure_point, t.arrival_time, t.destination,
                               COALESCE(SUM(d.total_seats - COALESCE(bc.booked_count, 0)), 0) as total_available,
                               u.id as operator_id,
                               (u.firstname || ' ' || u.lastname) as operator_name
                        FROM trips t
                        JOIN bus_types bt ON t.bus_type_id = bt.id
                        JOIN decks d ON d.trip_id = t.id
                        JOIN users u ON t.operator_id = u.id
                        LEFT JOIN (
                            SELECT deck_id, COUNT(*) as booked_count
                            FROM tickets ti
                            WHERE ti.status = 'PURCHASED' AND ti.selected_seat IS NOT NULL
                            GROUP BY deck_id
                        ) bc ON bc.deck_id = d.id
                        WHERE t.status = 'PUBLISHED'
                          AND (:departurePoint IS NULL OR t.departure_point = :departurePoint)
                          AND (:destination IS NULL OR t.destination = :destination)
                          AND (cast(:departureDate as date) IS NULL OR DATE(t.departure_time) = :departureDate)
                          AND (:routeName IS NULL OR t.route_name ILIKE '%' || :routeName || '%')
                          AND (cast(:operatorId as uuid) IS NULL OR t.operator_id = :operatorId)
                          AND (:timeSlotStartHour IS NULL OR EXTRACT(HOUR FROM t.departure_time) BETWEEN :timeSlotStartHour AND :timeSlotEndHour)
                          AND (:busTypes IS NULL OR bt.name = ANY(string_to_array(:busTypes, ',')))
                          AND EXISTS (
                              SELECT 1 FROM decks df
                              LEFT JOIN (
                                  SELECT deck_id, COUNT(*) as booked_count_df
                                  FROM tickets tif
                                  WHERE tif.status = 'PURCHASED' AND tif.selected_seat IS NOT NULL
                                  GROUP BY deck_id
                              ) bcf ON bcf.deck_id = df.id
                              WHERE df.trip_id = t.id
                                AND df.label = ANY(string_to_array(:deckLabels, ','))
                                AND (df.total_seats - COALESCE(bcf.booked_count_df, 0)) >= :numTickets
                          )
                        GROUP BY t.id, t.route_name, t.departure_time, t.departure_point, t.arrival_time, t.destination, u.id, u.firstname, u.lastname
                        HAVING COALESCE(SUM(d.total_seats - COALESCE(bc.booked_count, 0)), 0) >= :numTickets
                        """, nativeQuery = true)
        Page<Object[]> searchPublishedTripsFiltered(
                        @Param("departurePoint") String departurePoint,
                        @Param("destination") String destination,
                        @Param("departureDate") LocalDate departureDate,
                        @Param("numTickets") int numTickets,
                        @Param("routeName") String routeName,
                        @Param("operatorId") UUID operatorId,
                        @Param("timeSlotStartHour") Integer timeSlotStartHour,
                        @Param("timeSlotEndHour") Integer timeSlotEndHour,
                        @Param("busTypes") String busTypes,
                        @Param("deckLabels") String deckLabels,
                        Pageable pageable);

        @Query("SELECT COUNT(t) FROM Trip t " +
                        "WHERE t.operator.id = :operatorId AND t.departureTime BETWEEN :start AND :end")
        Long countTripsForOperatorBetweenDates(@Param("operatorId") UUID operatorId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("SELECT COALESCE(SUM(d.totalSeats), 0) FROM Deck d JOIN d.trip t " +
                        "WHERE t.operator.id = :operatorId AND t.departureTime BETWEEN :start AND :end")
        Long getTotalSeatsForOperatorTrips(@Param("operatorId") UUID operatorId, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);
}