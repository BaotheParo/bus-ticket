// TripRepository.java
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

// Giao diện TripRepository định nghĩa các phương thức truy vấn cơ sở dữ liệu cho entity Trip.
// Sử dụng @Repository để Spring quản lý như một bean.
// Kế thừa JpaRepository để cung cấp các phương thức CRUD cơ bản cho Trip với khóa chính là UUID.
@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    // Tìm các chuyến xe theo ID nhà điều hành với hỗ trợ phân trang.
    // @param organizerId ID của nhà điều hành (operator) để lọc các chuyến xe.
    // @param pageable Đối tượng phân trang (page, size, sort).
    // @return Trang (Page) chứa danh sách các chuyến xe thuộc nhà điều hành.
    Page<Trip> findByOperatorId(UUID organizerId, Pageable pageable);

    // Tìm một chuyến xe cụ thể theo ID chuyến xe và ID nhà điều hành.
    // @param tripId ID của chuyến xe cần tìm.
    // @param operatorId ID của nhà điều hành để đảm bảo chỉ lấy chuyến xe của nhà điều hành đó.
    // @return Optional chứa Trip nếu tìm thấy, hoặc rỗng nếu không tìm thấy.
    Optional<Trip> findByIdAndOperatorId(UUID tripId, UUID operatorId);

    // Tìm các chuyến xe theo trạng thái (status) với hỗ trợ phân trang.
    // @param status Trạng thái của chuyến xe (ví dụ: PUBLISHED, DRAFT).
    // @param pageable Đối tượng phân trang.
    // @return Trang (Page) chứa danh sách các chuyến xe có trạng thái tương ứng.
    Page<Trip> findByStatus(TripStatusEnum status, Pageable pageable);

    // Tìm kiếm các chuyến xe đã xuất bản (PUBLISHED) bằng tìm kiếm full-text của PostgreSQL.
    // @param searchTerm Chuỗi tìm kiếm (ví dụ: "Hanoi").
    // @param pageable Đối tượng phân trang.
    // @return Trang (Page) chứa danh sách các chuyến xe khớp với truy vấn tìm kiếm.
    // Truy vấn sử dụng to_tsvector và to_tsquery để tìm kiếm trên routeName, departurePoint, destination.
    @Query(value = "SELECT * FROM trips t WHERE t.status = 'PUBLISHED' " +
            "AND to_tsvector('simple', t.route_name || ' ' || t.departure_point || ' ' || t.destination) @@ to_tsquery('simple', :searchTerm)",
            nativeQuery = true)
    Page<Trip> searchTrips(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Tìm một chuyến xe theo ID và trạng thái.
    // @param id ID của chuyến xe cần tìm.
    // @param status Trạng thái của chuyến xe (ví dụ: PUBLISHED).
    // @return Optional chứa Trip nếu tìm thấy, hoặc rỗng nếu không tìm thấy.
    Optional<Trip> findByIdAndStatus(UUID id, TripStatusEnum status);

    @Modifying
    @Query("UPDATE Trip t SET t.busType.id = :undefinedId WHERE t.busType.id = :oldId")
    void updateBusTypeToUndefined(@Param("oldId") UUID oldId, @Param("undefinedId") UUID undefinedId);

    // Base query:
    @Query(value = """
        SELECT t.id, t.route_name, t.departure_time, t.departure_point, t.arrival_time, t.destination,
               COALESCE(SUM(d.total_seats - COALESCE(bc.booked_count, 0)), 0) as total_available
        FROM trips t
        JOIN bus_types bt ON t.bus_type_id = bt.id
        JOIN decks d ON d.trip_id = t.id
        LEFT JOIN (
            SELECT deck_id, COUNT(*) as booked_count
            FROM tickets ti
            WHERE ti.status = 'PURCHASED' AND ti.selected_seat IS NOT NULL
            GROUP BY deck_id
        ) bc ON bc.deck_id = d.id
        WHERE t.status = 'PUBLISHED'
          AND t.departure_point = :departurePoint
          AND t.destination = :destination
          AND DATE(t.departure_time) = :departureDate
        GROUP BY t.id, t.route_name, t.departure_time, t.departure_point, t.arrival_time, t.destination
        HAVING COALESCE(SUM(d.total_seats - COALESCE(bc.booked_count, 0)), 0) >= :numTickets
        """, nativeQuery = true)
    Page<Object[]> searchPublishedTripsBase(
            @Param("departurePoint") String departurePoint,
            @Param("destination") String destination,
            @Param("departureDate") LocalDate departureDate,
            @Param("numTickets") int numTickets,
            Pageable pageable);

    // Thêm filters vào base query (native SQL append conditions)
    @Query(value = """
        SELECT t.id, t.route_name, t.departure_time, t.departure_point, t.arrival_time, t.destination,
               COALESCE(SUM(d.total_seats - COALESCE(bc.booked_count, 0)), 0) as total_available
        FROM trips t
        JOIN bus_types bt ON t.bus_type_id = bt.id
        JOIN decks d ON d.trip_id = t.id
        LEFT JOIN (
            SELECT deck_id, COUNT(*) as booked_count
            FROM tickets ti
            WHERE ti.status = 'PURCHASED' AND ti.selected_seat IS NOT NULL
            GROUP BY deck_id
        ) bc ON bc.deck_id = d.id
        WHERE t.status = 'PUBLISHED'
          AND t.departure_point = :departurePoint
          AND t.destination = :destination
          AND DATE(t.departure_time) = :departureDate
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
        GROUP BY t.id, t.route_name, t.departure_time, t.departure_point, t.arrival_time, t.destination
        HAVING COALESCE(SUM(d.total_seats - COALESCE(bc.booked_count, 0)), 0) >= :numTickets
        """, nativeQuery = true)
    Page<Object[]> searchPublishedTripsFiltered(
            @Param("departurePoint") String departurePoint,
            @Param("destination") String destination,
            @Param("departureDate") LocalDate departureDate,
            @Param("numTickets") int numTickets,
            @Param("timeSlotStartHour") Integer timeSlotStartHour,
            @Param("timeSlotEndHour") Integer timeSlotEndHour,
            @Param("busTypes") String busTypes,
            @Param("deckLabels") String deckLabels,
            Pageable pageable);

    // (OPERATOR) Đếm số chuyến xe (trong khoảng thời gian KHỞI HÀNH)
    @Query("SELECT COUNT(t) FROM Trip t " +
            "WHERE t.operator.id = :operatorId AND t.departureTime BETWEEN :start AND :end")
    Long countTripsForOperatorBetweenDates(@Param("operatorId") UUID operatorId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // (OPERATOR) Lấy tổng số ghế (để tính tỷ lệ lấp đầy)
    @Query("SELECT COALESCE(SUM(d.totalSeats), 0) FROM Deck d JOIN d.trip t " +
            "WHERE t.operator.id = :operatorId AND t.departureTime BETWEEN :start AND :end")
    Long getTotalSeatsForOperatorTrips(@Param("operatorId") UUID operatorId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}