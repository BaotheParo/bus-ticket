package com.long_bus_distance.tickets.mapper;

import com.long_bus_distance.tickets.dto.GetTicketResponseDto;
import com.long_bus_distance.tickets.dto.ListTicketResponseDto;
import com.long_bus_distance.tickets.dto.ListTicketTypeResponseDto;
import com.long_bus_distance.tickets.entity.Ticket;
import com.long_bus_distance.tickets.entity.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {
    //Ánh xạ TicketType vào ListTicketTypeResponseDto
    @Mapping(source = "id", target = "id")
    @Mapping(source = "deck", target = "deck")
    ListTicketTypeResponseDto toListTicketTypeResponseDto (TicketType ticketType);

    //Ánh xạ Ticket vào ListTicketResponseDto
    @Mapping(source = "id", target = "id")
    @Mapping(source = "ticketType", target = "ticketType")
    ListTicketResponseDto toListTicketResponseDto(Ticket ticket);

    //Ánh xạ Ticket vào GetTicketResponseDto những dữ liệu đã được làm "PHẲNG"
    @Mapping(source = "id", target = "id")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "ticketType.price", target = "price")
    @Mapping(source = "ticketType.description", target = "description")
    @Mapping(source = "ticketType.trip.routeName", target = "routeName")
    @Mapping(source = "ticketType.trip.departurePoint", target = "departurePoint")
    @Mapping(source = "ticketType.trip.destination", target = "destination")
    @Mapping(source = "ticketType.trip.departureTime", target = "departureTime")
    @Mapping(source = "ticketType.trip.arrivalTime", target = "arrivalTime")
    GetTicketResponseDto toGetTicketResponseDto (Ticket ticket);
}
