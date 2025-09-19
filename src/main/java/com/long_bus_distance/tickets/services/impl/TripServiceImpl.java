package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.entity.*;
import com.long_bus_distance.tickets.repository.TripRepository;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.request.CreateTripRequest;
import com.long_bus_distance.tickets.services.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService{

    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    @Override
    public Trip createTrip(CreateTripRequest tripRequest, UUID operatorId) {
        //Tim nguoi tao
        User operator = userRepository.findById(operatorId)
                .orElseThrow(()-> new RuntimeException("User with id "+
                        operatorId + " not found"));
        //Tao CHUYEN XE
        Trip tripToCreate = new Trip();
        tripToCreate.setRouteName(tripRequest.getRoutename());
        tripToCreate.setDepartureTime(tripRequest.getDepartureTime() != null ?
                LocalDateTime.parse(tripRequest.getDepartureTime()): null);
        tripToCreate.setDeparturePoint(tripRequest.getDeparturePoint());
        tripToCreate.setArrivalTime(tripRequest.getArrivalTime() != null ?
                LocalDateTime.parse(tripRequest.getArrivalTime()): null);
        tripToCreate.setDestination(tripRequest.getDestination());
        tripToCreate.setDurationMinutes(tripRequest.getDurationMinutes());
        tripToCreate.setBusType(tripRequest.getBusType() != null ?
                BusTypeEnum.valueOf(tripRequest.getBusType()):BusTypeEnum.STANDARD);
        tripToCreate.setTripSchedule(tripRequest.getTripSchedule() != null ?
                tripRequest.getTripSchedule(): new ArrayList<>());
        tripToCreate.setSaleStart(tripRequest.getSaleStart() != null ?
                LocalDateTime.parse(tripRequest.getSaleStart()): null);
        tripToCreate.setSaleEnd(tripRequest.getSaleEnd() != null ?
                LocalDateTime.parse(tripRequest.getSaleEnd()) : null);
        tripToCreate.setStatus(tripRequest.getStatus() !=null ?
                tripRequest.getStatus() : TripStatusEnum.DRAFT);
        tripToCreate.setOperator(operator);

        //Tao va ket hop TicketType (xu li ticketType nay kha phuc tap nen gop tat ca roi tao
        // TickType thanh List la on nhat)
        List<TicketType> ticketTypesToCreate = tripRequest.getTicketType() != null ?
                tripRequest.getTicketType().stream().map(ticketTypeRequest ->{
                    TicketType ticketTypeToCreate = new TicketType();
                    ticketTypeToCreate.setName(ticketTypeRequest.getName());
                    ticketTypeToCreate.setPrice(ticketTypeRequest.getPrice());
                    ticketTypeToCreate.setDescription(ticketTypeRequest.getDescription());
                    ticketTypeToCreate.setTotalAvailable(ticketTypeRequest.getTotalAvailable());
                    ticketTypeToCreate.setDeck(ticketTypeRequest.getDeck() != null ?
                            DeckEnum.valueOf(ticketTypeRequest.getDeck()) : null);
                    ticketTypeToCreate.setTrip(tripToCreate);// Thiet lap mqh 2 chieu
                    return ticketTypeToCreate;

                }).collect(Collectors.toList()): new ArrayList<>();

        //Luu chuyen di (cascade ticketType)
        tripToCreate.setTicketTypes(ticketTypesToCreate);
        Trip createTrip = tripRepository.save(tripToCreate);

        return createTrip;
    }
}
