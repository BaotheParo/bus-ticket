// TripRepository.java
package com.long_bus_distance.tickets.repository;

import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.entity.TripStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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
}