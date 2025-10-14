package com.long_bus_distance.tickets.mapper;

import com.long_bus_distance.tickets.dto.TicketValidationResponseDto;
import com.long_bus_distance.tickets.entity.TicketValidation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketValidationMapper {
    @Mapping(target = "ticketId", source = "ticket.id")
    TicketValidationResponseDto toDto(TicketValidation validation);
}