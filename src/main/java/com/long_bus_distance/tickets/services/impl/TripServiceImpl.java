package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.dto.DeckWithSeatsDto;
import com.long_bus_distance.tickets.dto.SeatDto;
import com.long_bus_distance.tickets.dto.TripSeatsResponseDto;
import com.long_bus_distance.tickets.entity.*;
import com.long_bus_distance.tickets.exception.TripNotFoundException;
import com.long_bus_distance.tickets.exception.TripUpdateException;
import com.long_bus_distance.tickets.repository.TicketRepository;
import com.long_bus_distance.tickets.repository.TripRepository;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.request.CreateTripRequest;
import com.long_bus_distance.tickets.request.UpdateTripRequest;
import com.long_bus_distance.tickets.services.BusTypeService;
import com.long_bus_distance.tickets.services.TripService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.long_bus_distance.tickets.repository.DeckRepository;  // Để deleteAll

// Và inject private final DeckRepository deckRepository; trong constructor (RequiredArgsConstructor sẽ auto)
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final BusTypeService busTypeService;  // Để lấy BusType và clone decks
    private final DeckRepository deckRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional
    public Trip createTrip(CreateTripRequest request, UUID operatorId) {
        log.info("Tạo Trip mới cho operator: {}", operatorId);
        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new TripNotFoundException("Operator không tìm thấy"));

        Trip trip = new Trip();
        trip.setRouteName(request.getRouteName());
        trip.setDepartureTime(request.getDepartureTime() != null ? LocalDateTime.parse(request.getDepartureTime()) : null);
        trip.setDeparturePoint(request.getDeparturePoint());
        trip.setArrivalTime(request.getArrivalTime() != null ? LocalDateTime.parse(request.getArrivalTime()) : null);
        trip.setDestination(request.getDestination());
        trip.setDurationMinutes(request.getDurationMinutes());
        trip.setBusType(busTypeService.getById(request.getBusTypeId()).orElseThrow(() -> new TripNotFoundException("BusType không tìm thấy")));
        trip.setTripSchedule(request.getTripSchedule() != null ? request.getTripSchedule() : new ArrayList<>());
        trip.setSaleStart(request.getSaleStart() != null ? LocalDateTime.parse(request.getSaleStart()) : null);
        trip.setSaleEnd(request.getSaleEnd() != null ? LocalDateTime.parse(request.getSaleEnd()) : null);
        trip.setStatus(request.getStatus() != null ? request.getStatus() : TripStatusEnum.DRAFT);
        trip.setOperator(operator);

        // Tính basePrice nếu null
        if (request.getBasePrice() == null) {
            BusType busType = trip.getBusType();
            trip.setBasePrice((double) (trip.getDurationMinutes() != null ? trip.getDurationMinutes() : 0) * 1000 * busType.getPriceFactor());  // Ví dụ đơn giản
        } else {
            trip.setBasePrice(request.getBasePrice());
        }

        // Clone decks từ BusType
        cloneDecksFromBusType(trip);

        Trip savedTrip = tripRepository.save(trip);
        log.info("Tạo thành công Trip ID: {}", savedTrip.getId());
        return savedTrip;
    }


    @Override
    public Page<Trip> listTripsForOperator(UUID operatorId, Pageable pageable) {
        log.info("Liệt kê Trips cho operator: {}", operatorId);
        return tripRepository.findByOperatorId(operatorId, pageable);
    }

    @Override
    public Optional<Trip> getTripForOperator(UUID operatorId, UUID id) {
        log.info("Lấy Trip ID: {} cho operator: {}", id, operatorId);
        return tripRepository.findByIdAndOperatorId(id, operatorId);
    }

    // Thay toàn bộ method updateTripForOperator
    @Override
    @Transactional
    public Trip updateTripForOperator(UUID operatorId, UUID id, UpdateTripRequest request) {
        log.info("Cập nhật Trip ID: {} cho operator: {}", id, operatorId);
        Trip trip = tripRepository.findByIdAndOperatorId(id, operatorId)
                .orElseThrow(() -> new TripNotFoundException("Trip không tìm thấy"));

        // Update fields đơn giản
        if (request.getRouteName() != null) trip.setRouteName(request.getRouteName());
        if (request.getDepartureTime() != null) trip.setDepartureTime(LocalDateTime.parse(request.getDepartureTime()));
        if (request.getDeparturePoint() != null) trip.setDeparturePoint(request.getDeparturePoint());
        if (request.getArrivalTime() != null) trip.setArrivalTime(LocalDateTime.parse(request.getArrivalTime()));
        if (request.getDestination() != null) trip.setDestination(request.getDestination());
        if (request.getDurationMinutes() != null) trip.setDurationMinutes(request.getDurationMinutes());
        if (request.getTripSchedule() != null) trip.setTripSchedule(request.getTripSchedule());
        if (request.getSalesStart() != null) trip.setSaleStart(LocalDateTime.parse(request.getSalesStart()));
        if (request.getSalesEnd() != null) trip.setSaleEnd(LocalDateTime.parse(request.getSalesEnd()));
        if (request.getStatus() != null) trip.setStatus(request.getStatus());
        if (request.getBasePrice() != null) trip.setBasePrice(request.getBasePrice());

        // Nếu thay busTypeId, re-clone decks
        if (request.getBusTypeId() != null && !request.getBusTypeId().equals(trip.getBusType().getId())) {
            // Fix: Manual delete decks cũ trước clear
            deckRepository.deleteAll(trip.getDecks());
            trip.getDecks().clear();  // Clear collection

            trip.setBusType(busTypeService.getById(request.getBusTypeId()).orElseThrow(() -> new TripUpdateException("BusType không hợp lệ")));
            cloneDecksFromBusType(trip);  // Clone mới

            // Re-tính basePrice
            trip.setBasePrice((double) (trip.getDurationMinutes() != null ? trip.getDurationMinutes() : 0) * 1000 * trip.getBusType().getPriceFactor());

            // Bỏ flush, để save tự handle
        }

        Trip updatedTrip = tripRepository.save(trip);
        log.info("Cập nhật thành công Trip ID: {}", updatedTrip.getId());
        return updatedTrip;
    }

    // Giữ nguyên cloneDecksFromBusType, nhưng đảm bảo setId(null) và busType=null
    private void cloneDecksFromBusType(Trip trip) {
        BusType busType = trip.getBusType();
        List<Deck> clonedDecks = busType.getDefaultDecks().stream().map(defaultDeck -> {
            Deck cloned = new Deck();
            cloned.setId(null);  // Auto-gen mới
            cloned.setLabel(defaultDeck.getLabel());
            cloned.setPriceFactor(defaultDeck.getPriceFactor());
            cloned.setTotalSeats(defaultDeck.getTotalSeats());
            cloned.setBusType(null);  // Không reference BusType
            cloned.setTrip(trip);
            return cloned;
        }).collect(Collectors.toList());
        trip.setDecks(clonedDecks);
    }

    @Override
    @Transactional
    public void deleteTripForOperator(UUID operatorId, UUID id) {
        log.info("Xóa Trip ID: {} cho operator: {}", id, operatorId);
        Trip trip = tripRepository.findByIdAndOperatorId(id, operatorId)
                .orElseThrow(() -> new TripNotFoundException("Trip không tìm thấy"));
        tripRepository.delete(trip);
        log.info("Xóa thành công Trip ID: {}", id);
    }

    @Override
    public Page<Trip> listPublishedTrips(Pageable pageable) {
        log.info("Liệt kê Trips PUBLISHED");
        return tripRepository.findByStatus(TripStatusEnum.PUBLISHED, pageable);
    }

    @Override
    public Page<Trip> searchPublishedTrips(String query, Pageable pageable) {
        log.info("Tìm kiếm Trips PUBLISHED với query: {}", query);
        return tripRepository.searchTrips(query, pageable);
    }

    @Override
    public Optional<Trip> getPublishedTrip(UUID id) {
        log.info("Lấy Trip PUBLISHED ID: {}", id);
        return tripRepository.findByIdAndStatus(id, TripStatusEnum.PUBLISHED);
    }

    @Override
    public TripSeatsResponseDto getSeatsForTrip(UUID tripId) {
        log.info("Lấy seats map cho Trip ID: {}", tripId);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip không tìm thấy"));

        List<DeckWithSeatsDto> deckDtos = trip.getDecks().stream().map(deck -> {
            long sold = ticketRepository.countByDeckId(deck.getId());
            int remaining = deck.getTotalSeats() - (int) sold;
            double seatPrice = trip.getBasePrice() * deck.getPriceFactor() / deck.getTotalSeats();  // Giá mỗi ghế

            List<SeatDto> seats = new ArrayList<>();
            for (int pos = 1; pos <= deck.getTotalSeats(); pos++) {
                String position = String.valueOf(pos);
                String fullSeat = deck.getLabel() + position;  // "A1", "A2"
                long seatSold = ticketRepository.countByTripIdAndDeckIdAndSelectedSeat(tripId, deck.getId(), fullSeat);
                String status = seatSold > 0 ? "BOOKED" : "AVAILABLE";
                seats.add(new SeatDto(position, status, seatPrice));
            }

            DeckWithSeatsDto dto = new DeckWithSeatsDto(deck.getId(), deck.getLabel(), deck.getPriceFactor(), deck.getTotalSeats(), remaining + "/" + deck.getTotalSeats(), seats);
            return dto;
        }).collect(Collectors.toList());

        return new TripSeatsResponseDto(trip.getId(), trip.getRouteName(), deckDtos);
    }
}