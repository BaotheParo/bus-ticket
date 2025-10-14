package com.long_bus_distance.tickets.repository;

import com.long_bus_distance.tickets.entity.BusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BusTypeRepository extends JpaRepository<BusType, UUID> {
    boolean existsByName(String name);
}