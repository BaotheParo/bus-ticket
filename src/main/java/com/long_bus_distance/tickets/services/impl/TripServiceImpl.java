package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.dto.DeckWithSeatsDto;
import com.long_bus_distance.tickets.dto.ListPublishedTripResponseDto;
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
import java.sql.Timestamp;

// Và inject private final DeckRepository deckRepository; trong constructor (RequiredArgsConstructor sẽ auto)
import java.time.LocalDate;
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
        log.info("Cập nhật Trip -> Xóa toàn bộ cache tìm kiếm");
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

    @Override
    public Page<ListPublishedTripResponseDto> searchPublishedTrips(
            String departurePoint, String destination, String departureDateStr, int numTickets,
            String timeSlot, String busType, String deckLabel, Pageable pageable) {
        log.info("CACHE MISS - Đang truy vấn Database để tìm chuyến xe...");
        // Validate primary params (bắt buộc)
        if (departurePoint == null || departurePoint.trim().isEmpty() ||
                destination == null || destination.trim().isEmpty() ||
                departureDateStr == null || departureDateStr.trim().isEmpty() ||
                numTickets < 1) {
            // Return empty page nếu thiếu
            return Page.empty(pageable);
        }

        LocalDate departureDate;
        try {
            departureDate = LocalDate.parse(departureDateStr);  // ISO format (e.g., "2025-10-21")
        } catch (Exception e) {
            return Page.empty(pageable);  // Invalid date → empty
        }

        // Parse filters (comma-separated)
        Integer startHour = null, endHour = null;
        if (timeSlot != null && !timeSlot.trim().isEmpty()) {
            String[] slots = timeSlot.split(",");
            // Handle single/multi: Lấy first cho simple; nếu multi, adjust query để OR (e.g., multiple conditions)
            // Giả sử single select như yêu cầu (chọn 1 slot), dùng first
            try {
                TimeSlotEnum slotEnum = TimeSlotEnum.valueOf(slots[0].trim().toUpperCase());
                startHour = slotEnum.getStartTime().getHour();
                endHour = slotEnum.getEndTime().getHour();
                if (endHour == 23) endHour = 24;  // Edge for EVENING (PostgreSQL EXTRACT handles 24 as 00 next day if needed)
            } catch (IllegalArgumentException e) {
                // Invalid slot → ignore filter
            }
        }

        // BusType: Trim và pass nếu có
        String busTypesStr = (busType != null && !busType.trim().isEmpty()) ? busType.trim() : null;

        // DeckLabel: Trim và pass nếu có
        String deckLabelsStr = (deckLabel != null && !deckLabel.trim().isEmpty()) ? deckLabel.trim() : null;

        // Call repo: Dùng filtered nếu có bất kỳ filter nào, else base
        Page<Object[]> results;
        if (startHour != null || busTypesStr != null || deckLabelsStr != null) {
            results = tripRepository.searchPublishedTripsFiltered(
                    departurePoint.trim(), destination.trim(), departureDate, numTickets,
                    startHour, endHour, busTypesStr, deckLabelsStr, pageable);
        } else {
            results = tripRepository.searchPublishedTripsBase(
                    departurePoint.trim(), destination.trim(), departureDate, numTickets, pageable);
        }

        return results.map(row -> {
            ListPublishedTripResponseDto dto = new ListPublishedTripResponseDto();
            dto.setId((UUID) row[0]);
            dto.setRouteName((String) row[1]);
            if (row[2] != null) {
                dto.setDepartureTime(((Timestamp) row[2]).toLocalDateTime());
            }
            dto.setDeparturePoint((String) row[3]);
            if (row[4] != null) {
                dto.setArrivalTime(((Timestamp) row[4]).toLocalDateTime());
            }
            dto.setDestination((String) row[5]);
            dto.setTotalAvailableSeats(row[6] != null ? ((Number) row[6]).intValue() : 0);

            return dto;
        });
    }

    @Override
    public Page<Trip> listAllTripsForAdmin(Pageable pageable) {
        log.info("Admin listing all trips");
        // Đơn giản là gọi findAll, không lọc theo operator
        return tripRepository.findAll(pageable);
    }

    @Override
    public Optional<Trip> getTripForAdmin(UUID id) {
        log.info("Admin getting trip ID: {}", id);
        // Đơn giản là gọi findById, không lọc theo operator
        return tripRepository.findById(id);
    }
}