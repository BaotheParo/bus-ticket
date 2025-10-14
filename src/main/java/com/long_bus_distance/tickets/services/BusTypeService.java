package com.long_bus_distance.tickets.services;

import com.long_bus_distance.tickets.dto.CreateBusTypeRequestDto;
import com.long_bus_distance.tickets.dto.UpdateBusTypeRequestDto;
import com.long_bus_distance.tickets.entity.BusType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusTypeService {
    BusType createBusType(CreateBusTypeRequestDto dto);
    Optional<BusType> getById(UUID id);
    List<BusType> listBusTypes();
    BusType updateBusType(UUID id, UpdateBusTypeRequestDto dto);
    void deleteBusType(UUID id);
}