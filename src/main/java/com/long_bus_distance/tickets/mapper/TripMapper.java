package com.long_bus_distance.tickets.mapper;

import com.long_bus_distance.tickets.dto.CreateTicketTypeRequestDto;
import com.long_bus_distance.tickets.dto.CreateTripRequestDto;
import com.long_bus_distance.tickets.dto.CreateTripResponseDto;
import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.request.CreateTicketTypeRequest;
import com.long_bus_distance.tickets.request.CreateTripRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TripMapper {
    CreateTicketTypeRequest fromDto(CreateTicketTypeRequestDto dto);

    CreateTripRequest fromDto(CreateTripRequestDto dto);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "busType", target = "busType", defaultValue = "STANDARD")
    CreateTripResponseDto toDto(Trip trip);
}
