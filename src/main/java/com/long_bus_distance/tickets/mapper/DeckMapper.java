package com.long_bus_distance.tickets.mapper;

import com.long_bus_distance.tickets.dto.DeckResponseDto;
import com.long_bus_distance.tickets.entity.Deck;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeckMapper {
    DeckResponseDto toDto(Deck deck);
}