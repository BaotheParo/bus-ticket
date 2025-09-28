package com.long_bus_distance.tickets.mapper;

import com.long_bus_distance.tickets.dto.TicketValidationResponseDto;
import com.long_bus_distance.tickets.entity.TicketValidation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketValidationMapper {
    @Mapping(source = "ticket.id", target = "ticketId")
    TicketValidationResponseDto toTicketValidationResponseDto (TicketValidation validation);
}

