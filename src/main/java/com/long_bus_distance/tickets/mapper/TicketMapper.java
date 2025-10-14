package com.long_bus_distance.tickets.mapper;

import com.long_bus_distance.tickets.dto.GetTicketResponseDto;
import com.long_bus_distance.tickets.dto.ListTicketResponseDto;
import com.long_bus_distance.tickets.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {

    @Mapping(target = "deckLabel", source = "deck.label")
    @Mapping(target = "routeName", source = "deck.trip.routeName")
    ListTicketResponseDto toListTicketResponseDto(Ticket ticket);

    default Page<ListTicketResponseDto> toListTicketResponseDtoPage(Page<Ticket> tickets) {
        return tickets.map(this::toListTicketResponseDto);
    }

    @Mapping(target = "deckLabel", source = "deck.label")
    @Mapping(target = "routeName", source = "deck.trip.routeName")
    @Mapping(target = "departurePoint", source = "deck.trip.departurePoint")
    @Mapping(target = "departureTime", source = "deck.trip.departureTime")
    @Mapping(target = "destination", source = "deck.trip.destination")
    @Mapping(target = "arrivalTime", source = "deck.trip.arrivalTime")
    GetTicketResponseDto toGetTicketResponseDto(Ticket ticket);
}