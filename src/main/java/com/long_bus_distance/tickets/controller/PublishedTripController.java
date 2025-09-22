package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.GetPublishedTripDetailsResponseDto;
import com.long_bus_distance.tickets.dto.ListPublishedTripResponseDto;
import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.mapper.TripMapper;
import com.long_bus_distance.tickets.services.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

// Lớp PublishedTripController xử lý các yêu cầu HTTP liên quan đến các chuyến xe đã xuất bản (public).
// Sử dụng @RestController để đánh dấu đây là một REST controller trả về dữ liệu JSON.
// @RequestMapping("/api/v1/published-trips") đặt đường dẫn cơ sở cho tất cả các endpoint trong controller.
// Các endpoint này không yêu cầu xác thực, cho phép người dùng truy cập công khai.
@RestController
@RequestMapping("/api/v1/published-trips")
@RequiredArgsConstructor
@Slf4j
public class PublishedTripController {
    private final TripService tripService; // Dịch vụ để xử lý logic nghiệp vụ liên quan đến chuyến xe
    private final TripMapper tripMapper; // Mapper để chuyển đổi giữa entity và DTO

    // Liệt kê hoặc tìm kiếm các chuyến xe đã xuất bản với phân trang.
    // @param query Tham số truy vấn tìm kiếm (tùy chọn), ví dụ: "Hanoi".
    // @param pageable Đối tượng phân trang (page, size, sort).
    // @return Phản hồi HTTP 200 (OK) với danh sách các DTO chuyến xe đã xuất bản.
    @GetMapping
    public ResponseEntity<Page<ListPublishedTripResponseDto>> listPublishedTrip(
            @RequestParam(name = "q", required = false) String query, Pageable pageable) {
        log.info("Nhận yêu cầu GET để liệt kê hoặc tìm kiếm các chuyến xe đã xuất bản với truy vấn: {} và phân trang: {}", query, pageable);
        // Khai báo biến để lưu danh sách chuyến xe
        Page<Trip> trips;
        // Kiểm tra xem tham số truy vấn có được cung cấp và không rỗng
        if (query != null && !query.trim().isEmpty()) {
            // Gọi dịch vụ để tìm kiếm các chuyến xe đã xuất bản theo truy vấn
            trips = tripService.searchPublishedTrips(query, pageable);
        } else {
            // Gọi dịch vụ để liệt kê tất cả các chuyến xe đã xuất bản
            trips = tripService.listPublishedTrips(pageable);
        }
        // Chuyển danh sách entity Trip thành danh sách DTO
        Page<ListPublishedTripResponseDto> responseDtos = trips.map(tripMapper::toListPublishedTripResponseDto);
        // Trả về phản hồi HTTP 200 (OK) với danh sách DTO
        return ResponseEntity.ok(responseDtos);
    }

    // Lấy chi tiết một chuyến xe đã xuất bản dựa trên ID.
    // @param id ID của chuyến xe cần lấy, lấy từ đường dẫn.
    // @return Phản hồi HTTP 200 (OK) với DTO chi tiết chuyến xe hoặc 404 (Not Found) nếu không tìm thấy.
    @GetMapping("/{id}")
    public ResponseEntity<GetPublishedTripDetailsResponseDto> getPublishedTripDetails(@PathVariable UUID id) {
        log.info("Nhận yêu cầu GET để lấy chi tiết chuyến xe đã xuất bản với ID: {}", id);
        // Gọi dịch vụ để lấy chuyến xe có trạng thái PUBLISHED
        Optional<Trip> optionalTrip = tripService.getPublishedTrip(id);
        // Chuyển entity Trip thành DTO và trả về phản hồi
        return optionalTrip
                .map(tripMapper::toGetPublishedTripDetailsResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}