package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.CreateBusTypeRequestDto;
import com.long_bus_distance.tickets.dto.GetBusTypeResponseDto;
import com.long_bus_distance.tickets.dto.UpdateBusTypeRequestDto;
import com.long_bus_distance.tickets.entity.BusType;
import com.long_bus_distance.tickets.mapper.BusTypeMapper;
import com.long_bus_distance.tickets.services.BusTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bus-types")
@RequiredArgsConstructor
@Slf4j
public class BusTypeController {
    private final BusTypeService busTypeService;
    private final BusTypeMapper busTypeMapper;

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    // Sửa kiểu trả về từ BusType sang DTO
    public ResponseEntity<GetBusTypeResponseDto> createBusType(@Valid @RequestBody CreateBusTypeRequestDto dto) {
        log.info("Nhận request tạo BusType: {}", dto.getName());
        BusType busType = busTypeService.createBusType(dto);
        // Chuyển đổi sang DTO trước khi trả về
        return ResponseEntity.ok(busTypeMapper.toDto(busType));
    }

    @GetMapping
    // Sửa kiểu trả về từ List<BusType> sang List<DTO>
    public ResponseEntity<List<GetBusTypeResponseDto>> listBusTypes() {
        log.info("Liệt kê BusTypes");
        List<BusType> busTypes = busTypeService.listBusTypes();
        // Chuyển đổi List sang DTO
        return ResponseEntity.ok(busTypeMapper.toDtoList(busTypes));
    }

    @GetMapping("/{id}")
    // Sửa kiểu trả về từ BusType sang DTO
    public ResponseEntity<GetBusTypeResponseDto> getBusType(@PathVariable UUID id) {
        log.info("Lấy BusType ID: {}", id);
        return busTypeService.getById(id)
                .map(busTypeMapper::toDto) // Dùng mapper để chuyển đổi
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    // Sửa kiểu trả về từ BusType sang DTO
    public ResponseEntity<GetBusTypeResponseDto> updateBusType(@PathVariable UUID id, @Valid @RequestBody UpdateBusTypeRequestDto dto) {
        log.info("Nhận request cập nhật BusType ID: {}", id);
        BusType busType = busTypeService.updateBusType(id, dto);
        // Chuyển đổi sang DTO trước khi trả về
        return ResponseEntity.ok(busTypeMapper.toDto(busType));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Void> deleteBusType(@PathVariable UUID id) {
        log.info("Xóa BusType ID: {}", id);
        busTypeService.deleteBusType(id);
        return ResponseEntity.noContent().build();
    }
}