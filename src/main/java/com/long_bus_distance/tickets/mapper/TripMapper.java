package com.long_bus_distance.tickets.mapper;

import com.long_bus_distance.tickets.dto.*;
import com.long_bus_distance.tickets.entity.Deck;
import com.long_bus_distance.tickets.entity.Ticket;
import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.repository.TicketRepository;
import com.long_bus_distance.tickets.request.CreateTripRequest;
import com.long_bus_distance.tickets.request.UpdateTripRequest;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {DeckMapper.class})
public interface TripMapper {

    // From DTO to Request: Source busTypeId (UUID) → target busTypeId
    @Mapping(target = "busTypeId", source = "busTypeId")  // Fix: Trực tiếp UUID, không "busType.id"
    CreateTripRequest fromDto(CreateTripRequestDto dto);

    // To Response DTO: Source busType.id → target busTypeId
    @Mapping(target = "busTypeId", source = "busType.id")  // OK cho entity source
    @Mapping(target = "id", source = "id")
    CreateTripResponseDto toCreateTripResponseDto(Trip trip);

    // From DTO to Request: Source busTypeId → target busTypeId
    @Mapping(target = "busTypeId", source = "busTypeId")  // Fix: Trực tiếp UUID
    @Mapping(target = "id", source = "id")
    UpdateTripRequest fromUpdateTripRequestDto(UpdateTripRequestDto dto);

    // To Response DTO: Source busType.id → target busTypeId
    @Mapping(target = "busTypeId", source = "busType.id")
    @Mapping(target = "id", source = "id")
    UpdateTripResponseDto toUpdateTripResponseDto(Trip trip);

    @Mapping(target = "busTypeId", source = "busType.id")
    ListTripResponseDto toListTripResponseDto(Trip trip);

    default Page<ListTripResponseDto> toListTripResponseDtoPage(Page<Trip> trips) {
        return trips.map(this::toListTripResponseDto);
    }

    @Mapping(target = "busTypeId", source = "busType.id")
    GetTripDetailsResponseDto toGetTripDetailsResponseDto(Trip trip);

    ListPublishedTripResponseDto toListPublishedTripResponseDto(Trip trip);

    default Page<ListPublishedTripResponseDto> toListPublishedTripResponseDtoPage(Page<Trip> trips) {
        return trips.map(this::toListPublishedTripResponseDto);
    }

    // Fix: Target có busTypeId, source busType.id
    @Mapping(target = "busTypeId", source = "busType.id")
    GetPublishedTripDetailsResponseDto toGetPublishedTripDetailsResponseDto(Trip trip);

    // Giữ nguyên @AfterMapping cho decks remaining
    @AfterMapping
    default void setDecksWithRemaining(@MappingTarget ListTripResponseDto dto, Trip source, @Context TicketRepository ticketRepository) {
        List<DeckResponseDto> deckDtos = source.getDecks().stream()
                .map(deck -> {
                    long sold = ticketRepository.countByDeckId(deck.getId());
                    int remaining = deck.getTotalSeats() - (int) sold;
                    DeckResponseDto d = new DeckResponseDto(deck.getId(), deck.getLabel(), deck.getPriceFactor(), deck.getTotalSeats(), remaining + "/" + deck.getTotalSeats());
                    return d;
                })
                .collect(Collectors.toList());
        dto.setDecks(deckDtos);
    }

    @AfterMapping
    default void setDecksWithRemainingDetails(@MappingTarget GetTripDetailsResponseDto dto, Trip source, @Context TicketRepository ticketRepository) {
        List<DeckResponseDto> deckDtos = source.getDecks().stream()
                .map(deck -> {
                    long sold = ticketRepository.countByDeckId(deck.getId());
                    int remaining = deck.getTotalSeats() - (int) sold;
                    DeckResponseDto d = new DeckResponseDto(deck.getId(), deck.getLabel(), deck.getPriceFactor(), deck.getTotalSeats(), remaining + "/" + deck.getTotalSeats());
                    return d;
                })
                .collect(Collectors.toList());
        dto.setDecks(deckDtos);
    }

    @AfterMapping
    default void setDecksWithRemainingPublished(@MappingTarget GetPublishedTripDetailsResponseDto dto, Trip source, @Context TicketRepository ticketRepository) {
        List<DeckResponseDto> deckDtos = source.getDecks().stream()
                .map(deck -> {
                    long sold = ticketRepository.countByDeckId(deck.getId());
                    int remaining = deck.getTotalSeats() - (int) sold;
                    DeckResponseDto d = new DeckResponseDto(deck.getId(), deck.getLabel(), deck.getPriceFactor(), deck.getTotalSeats(), remaining + "/" + deck.getTotalSeats());
                    return d;
                })
                .collect(Collectors.toList());
        dto.setDecks(deckDtos);
    }
}