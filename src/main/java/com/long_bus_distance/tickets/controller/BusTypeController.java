package com.long_bus_distance.tickets.controller;

import com.long_bus_distance.tickets.dto.CreateBusTypeRequestDto;
import com.long_bus_distance.tickets.dto.UpdateBusTypeRequestDto;
import com.long_bus_distance.tickets.entity.BusType;
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

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<BusType> createBusType(@Valid @RequestBody CreateBusTypeRequestDto dto) {
        log.info("Nhận request tạo BusType: {}", dto.getName());
        BusType busType = busTypeService.createBusType(dto);
        return ResponseEntity.ok(busType);
    }

    @GetMapping
    public ResponseEntity<List<BusType>> listBusTypes() {
        log.info("Liệt kê BusTypes");
        List<BusType> busTypes = busTypeService.listBusTypes();
        return ResponseEntity.ok(busTypes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusType> getBusType(@PathVariable UUID id) {
        log.info("Lấy BusType ID: {}", id);
        return busTypeService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<BusType> updateBusType(@PathVariable UUID id, @Valid @RequestBody UpdateBusTypeRequestDto dto) {
        log.info("Nhận request cập nhật BusType ID: {}", id);
        BusType busType = busTypeService.updateBusType(id, dto);
        return ResponseEntity.ok(busType);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Void> deleteBusType(@PathVariable UUID id) {
        log.info("Xóa BusType ID: {}", id);
        busTypeService.deleteBusType(id);
        return ResponseEntity.noContent().build();
    }
}