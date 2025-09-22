package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.*;
import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.mapper.TripMapper;
import com.long_bus_distance.tickets.request.UpdateTripRequest;
import com.long_bus_distance.tickets.services.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

// Lớp TripController xử lý các yêu cầu HTTP liên quan đến quản lý chuyến xe (tạo, cập nhật, liệt kê, lấy chi tiết, xóa).
// Sử dụng @RestController để đánh dấu đây là một REST controller trả về dữ liệu JSON.
// @RequestMapping("/api/v1/trips") đặt đường dẫn cơ sở cho tất cả các endpoint trong controller.
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripMapper tripMapper; // Mapper để chuyển đổi giữa entity và DTO
    private final TripService tripService; // Service để xử lý logic nghiệp vụ liên quan đến chuyến xe

    // Tạo một chuyến xe mới.
    // @param createTripRequestDto DTO chứa thông tin để tạo chuyến xe, được xác thực bằng @Valid.
    // @param authentication Thông tin xác thực JWT để lấy ID nhà điều hành.
    // @return Phản hồi HTTP 201 (Created) với DTO chứa thông tin chuyến xe đã tạo.
    @PostMapping
    public ResponseEntity<CreateTripResponseDto> createTrip(
        @Valid @RequestBody CreateTripRequestDto createTripRequestDto
            , JwtAuthenticationToken authentication){
        // Trích xuất ID nhà điều hành từ JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID operatorId = UUID.fromString(jwt.getSubject());
        //Chuyển DTO thành Service
        var createdTripRequest = tripMapper.fromDto(createTripRequestDto);
        // Gọi Service để tạo chuyến xe
        var createdTrip = tripService.createTrip(createdTripRequest,operatorId);
        //Chuyển entity Trip thành DTO response
        var createTripResponseDto = tripMapper.toDto(createdTrip);
        //Trả về HTTP 201 với DTO
        return new ResponseEntity<>(createTripResponseDto, HttpStatus.CREATED);
    }

    // Cập nhật thông tin một chuyến xe hiện có.
    // @param id ID của chuyến xe cần cập nhật, lấy từ đường dẫn.
    // @param updateTripRequestDto DTO chứa thông tin cập nhật, được xác thực bằng @Valid.
    // @param authentication Thông tin xác thực JWT để lấy ID nhà điều hành.
    // @return Phản hồi HTTP 200 (OK) với DTO chứa thông tin chuyến xe đã cập nhật.
    @PutMapping("/{id}")
    public ResponseEntity<UpdateTripResponseDto> updateTrip(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTripRequestDto updateTripRequestDto
            , JwtAuthenticationToken authentication){
        // Trích xuất ID nhà điều hành từ JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID operatorId = UUID.fromString(jwt.getSubject());
        // Chuyển DTO thành đối tượng yêu cầu cập nhật
        UpdateTripRequest updateTripRequest = tripMapper.fromUpdateTripRequestDto(updateTripRequestDto);
        // Gọi Service để cập nhật chuyến xe
        Trip updatedTrip = tripService.updateTripForOperator(operatorId, id, updateTripRequest);
        // Chuyển entity Trip thành DTO response
        UpdateTripResponseDto responseDto =tripMapper.toUpdateTripResponseDto(updatedTrip);
        // Trả về HTTP 201 với DTO
        return ResponseEntity.ok(responseDto);
    }

    // Liệt kê các chuyến xe của một nhà điều hành với phân trang.
    // @param authentication Thông tin xác thực JWT để lấy ID nhà điều hành.
    // @param pageable Đối tượng phân trang (page, size, sort).
    // @return Phản hồi HTTP 200 (OK) với danh sách các DTO chuyến xe theo phân trang.
    @GetMapping
    public ResponseEntity<Page<ListTripResponseDto>>listTripForOperator(JwtAuthenticationToken authentication, Pageable pageable){
        // Trích xuất ID nhà điều hành từ JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID operatorId = UUID.fromString(jwt.getSubject());
        // Gọi Service để lấy danh sách chuyến xe của nhà điều hành
        Page<Trip>trips = tripService.listTripsForOperator(operatorId,pageable);
        // Chuyển entity Trip thành DTO response
        Page<ListTripResponseDto> responseDtos = trips.map(tripMapper::toListTripResponseDto);
        // Trả về HTTP 201 với DTO
        return ResponseEntity.ok(responseDtos);
    }

    // Lấy chi tiết một chuyến xe cụ thể.
    // @param id ID của chuyến xe cần lấy, lấy từ đường dẫn.
    // @param authentication Thông tin xác thực JWT để lấy ID nhà điều hành.
    // @return Phản hồi HTTP 200 (OK) với DTO chi tiết chuyến xe hoặc 404 (Not Found) nếu không tìm thấy.
    @GetMapping("/{id}")
    public ResponseEntity<GetTripDetailsResponseDto>getTrip(@PathVariable UUID id, JwtAuthenticationToken authentication){
        // Trích xuất ID nhà điều hành từ JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID operatorId = UUID.fromString(jwt.getSubject());
        // Gọi Service để lấy chi tiết chuyến xe
        Optional<Trip> optionalTrip = tripService.getTripForOperator(operatorId,id);
        // Chuyển entity Trip thành DTO và trả về phản hồi
        return optionalTrip.map(tripMapper::toGetTripDetailsResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Xóa một chuyến xe.
    // @param id ID của chuyến xe cần xóa, lấy từ đường dẫn.
    // @param authentication Thông tin xác thực JWT để lấy ID nhà điều hành.
    // @return Phản hồi HTTP 204 (No Content) sau khi xóa thành công.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable UUID id, JwtAuthenticationToken authentication){
        // Trích xuất ID nhà điều hành từ JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID operatorId = UUID.fromString(jwt.getSubject());
        // Gọi Service để xóa chuyến xe
        tripService.deleteTripForOperator(operatorId, id);
        // Trả về phản hồi HTTP 204 (No Content)
        return ResponseEntity.noContent().build();
    }

}
