package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.entity.*;
import com.long_bus_distance.tickets.exception.TicketTypeNotFoundException;
import com.long_bus_distance.tickets.exception.TripNotFoundException;
import com.long_bus_distance.tickets.exception.TripUpdateException;
import com.long_bus_distance.tickets.repository.TripRepository;
import com.long_bus_distance.tickets.repository.UserRepository;
import com.long_bus_distance.tickets.request.CreateTripRequest;
import com.long_bus_distance.tickets.request.UpdateTicketTypeRequest;
import com.long_bus_distance.tickets.request.UpdateTripRequest;
import com.long_bus_distance.tickets.services.TripService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// Lớp TripServiceImpl triển khai giao diện TripService, xử lý logic nghiệp vụ liên quan đến chuyến xe.
// Sử dụng @Service để đánh dấu đây là một bean dịch vụ trong Spring.
// @Slf4j cung cấp logging để ghi lại thông tin và lỗi.
// @RequiredArgsConstructor tạo constructor tự động cho các trường final (userRepository, tripRepository).
@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService{

    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    // Tạo một chuyến xe mới dựa trên yêu cầu và ID nhà điều hành.
    // @param tripRequest Đối tượng yêu cầu chứa thông tin chuyến xe.
    // @param operatorId ID của nhà điều hành tạo chuyến xe.
    // @return Entity Trip đã được tạo và lưu vào cơ sở dữ liệu.
    @Override
    public Trip createTrip(CreateTripRequest tripRequest, UUID operatorId) {
        //Tim nguoi tao
        User operator = userRepository.findById(operatorId)
                .orElseThrow(()-> new RuntimeException("User with id "+
                        operatorId + " not found"));
        //Tao CHUYEN XE
        Trip tripToCreate = new Trip();
        tripToCreate.setRouteName(tripRequest.getRoutename());
        // Chuyển đổi departureTime từ String sang LocalDateTime, hoặc null nếu không có
        tripToCreate.setDepartureTime(tripRequest.getDepartureTime() != null ?
                LocalDateTime.parse(tripRequest.getDepartureTime()): null);
        tripToCreate.setDeparturePoint(tripRequest.getDeparturePoint());
        tripToCreate.setArrivalTime(tripRequest.getArrivalTime() != null ?
                LocalDateTime.parse(tripRequest.getArrivalTime()): null);
        tripToCreate.setDestination(tripRequest.getDestination());
        tripToCreate.setDurationMinutes(tripRequest.getDurationMinutes());
        // Chuyển đổi busType từ String sang BusTypeEnum, mặc định là STANDARD
        tripToCreate.setBusType(tripRequest.getBusType() != null ?
                BusTypeEnum.valueOf(tripRequest.getBusType()):BusTypeEnum.STANDARD);
        tripToCreate.setTripSchedule(tripRequest.getTripSchedule() != null ?
                tripRequest.getTripSchedule(): new ArrayList<>());
        tripToCreate.setSaleStart(tripRequest.getSaleStart() != null ?
                LocalDateTime.parse(tripRequest.getSaleStart()): null);
        tripToCreate.setSaleEnd(tripRequest.getSaleEnd() != null ?
                LocalDateTime.parse(tripRequest.getSaleEnd()) : null);
        // Gán trạng thái, mặc định là DRAFT nếu null
        tripToCreate.setStatus(tripRequest.getStatus() !=null ?
                tripRequest.getStatus() : TripStatusEnum.DRAFT);
        tripToCreate.setOperator(operator);

        // Tạo và xử lý danh sách (TicketType) từ yêu cầu
        List<TicketType> ticketTypesToCreate = tripRequest.getTicketType() != null ?
                tripRequest.getTicketType().stream().map(ticketTypeRequest ->{
                    TicketType ticketTypeToCreate = new TicketType();
                    ticketTypeToCreate.setName(ticketTypeRequest.getName());
                    ticketTypeToCreate.setPrice(ticketTypeRequest.getPrice());
                    ticketTypeToCreate.setDescription(ticketTypeRequest.getDescription());
                    ticketTypeToCreate.setTotalAvailable(ticketTypeRequest.getTotalAvailable());
                    ticketTypeToCreate.setDeck(ticketTypeRequest.getDeck() != null ?
                            DeckEnum.valueOf(ticketTypeRequest.getDeck()) : null);
                    // Thiết lập mối quan hệ hai chiều với Trip
                    ticketTypeToCreate.setTrip(tripToCreate);
                    return ticketTypeToCreate;
                }).collect(Collectors.toList()): new ArrayList<>();

        // Gán danh sách loại vé vào chuyến xe
        tripToCreate.setTicketTypes(ticketTypesToCreate);
        // Lưu chuyến xe vào CSDL (cascade lưu cả TicketType)
        Trip createTrip = tripRepository.save(tripToCreate);

        return createTrip;
    }

    // Liệt kê các chuyến xe của một nhà điều hành với phân trang.
    // @param operatorId ID của nhà điều hành.
    // @param pageable Đối tượng phân trang (page, size, sort).
    // @return Trang (Page) chứa danh sách các chuyến xe.
    @Override
    public Page<Trip> listTripsForOperator(UUID operatorId, Pageable pageable) {
        log.info("Listing trips for operator ID: {}", operatorId);
        return tripRepository.findByOrganizerId(operatorId,pageable);
    }

    // Lấy thông tin chi tiết một chuyến xe của nhà điều hành.
    // @param operatorId ID của nhà điều hành.
    // @param id ID của chuyến xe.
    // @return Optional chứa Trip nếu tìm thấy, hoặc rỗng nếu không tìm thấy.
    @Override
    public Optional<Trip> getTripForOperator(UUID operatorId, UUID id) {
        log.info("Retrieving trip with ID: {} for operator ID: {}", id ,operatorId);
        return tripRepository.findByIdAndOperatorId(id, operatorId);
    }

    // Cập nhật thông tin một chuyến xe.
    // @param operatorId ID của nhà điều hành.
    // @param id ID của chuyến xe cần cập nhật.
    // @param tripRequest Đối tượng yêu cầu chứa thông tin cập nhật.
    // @return Entity Trip đã được cập nhật.
    // @Transactional đảm bảo tất cả thao tác cơ sở dữ liệu được thực hiện trong một giao dịch.
    @Override
    @Transactional
    public Trip updateTripForOperator(UUID operatorId, UUID id, UpdateTripRequest tripRequest) {
        log.info("Updating trip with ID:{} for operator ID: {}",id,operatorId);
        // Kiểm tra ID trong yêu cầu
        if (tripRequest.getId() == null)
            throw new TripUpdateException("Trip ID in request cannot be null");
        // Kiểm tra ID trong đường dẫn và yêu cầu có khớp không
        if (!id.equals(tripRequest.getId()))
            throw new TripNotFoundException("Cannot update the ID of a trip");

        // Tìm chuyến xe hiện có, ném ngoại lệ nếu không tìm thấy
        Trip existingTrip = tripRepository.findByIdAndOperatorId(id, operatorId)
                .orElseThrow(()->new TripNotFoundException("Trip with ID "+ id +" not found for operator"));

        // Cập nhật các trường đơn giản
        existingTrip.setRouteName(tripRequest.getRouteName());
        existingTrip.setDepartureTime(tripRequest.getDepartureTime() != null ? LocalDateTime.parse(tripRequest.getDepartureTime()):null);
        existingTrip.setDeparturePoint(tripRequest.getDeparturePoint());
        existingTrip.setArrivalTime(tripRequest.getArrivalTime() != null ? LocalDateTime.parse(tripRequest.getArrivalTime()) : null);
        existingTrip.setDestination(tripRequest.getDestination());
        existingTrip.setDurationMinutes(tripRequest.getDurationMinutes());
        existingTrip.setBusType(tripRequest.getBusType() != null ? BusTypeEnum.valueOf(tripRequest.getBusType()) : BusTypeEnum.STANDARD);
        existingTrip.setTripSchedule(tripRequest.getTripSchedule() != null ? tripRequest.getTripSchedule() : new ArrayList<>());
        existingTrip.setSaleStart(tripRequest.getSalesStart() != null ? LocalDateTime.parse(tripRequest.getSalesStart()) : null);
        existingTrip.setSaleEnd(tripRequest.getSalesEnd() != null ? LocalDateTime.parse(tripRequest.getSalesEnd()) : null);
        existingTrip.setStatus(tripRequest.getStatus() != null ? tripRequest.getStatus() : TripStatusEnum.DRAFT);

        // Xử lý danh sách loại vé
        Set<UUID> requestTicketTypeIds = tripRequest.getTicketTypes().stream().
                filter(ticketType -> ticketType.getId() != null)
                .map(UpdateTicketTypeRequest::getId)
                .collect(Collectors.toSet());

        // Xóa các loại vé không còn trong yêu cầu
        existingTrip.getTicketTypes().removeIf(ticketType -> ticketType.getId() != null && !requestTicketTypeIds.contains(ticketType.getId()));

        // Tạo chỉ mục cho các loại vé hiện có để tra cứu nhanh
        Map<UUID, TicketType> existingTicketTypesIndex = existingTrip.getTicketTypes().stream().filter(ticketType -> ticketType.getId() != null)
                .collect(Collectors.toMap(TicketType::getId, ticketType -> ticketType));

        // Xử lý tạo/cập nhật loại vé
        List<TicketType> updatedTicketTypes = new ArrayList<>();
        for (UpdateTicketTypeRequest ticketTypeRequest : tripRequest.getTicketTypes()){
            if (ticketTypeRequest.getId() == null){
                // Tạo loại vé mới
                TicketType newTicketType = new TicketType();
                newTicketType.setName(ticketTypeRequest.getName());
                newTicketType.setPrice(ticketTypeRequest.getPrice());
                newTicketType.setDescription(ticketTypeRequest.getDescription());
                newTicketType.setTotalAvailable(ticketTypeRequest.getTotalAvailable());
                newTicketType.setDeck(ticketTypeRequest.getDeck() != null ? DeckEnum.valueOf(ticketTypeRequest.getDeck()) : null);
                newTicketType.setTrip(existingTrip);
                updatedTicketTypes.add(newTicketType);
            } else {
                // Cập nhật loại vé hiện có
                TicketType existingTicketType = existingTicketTypesIndex .get(ticketTypeRequest.getId());
                if (existingTicketType == null){
                    throw new TicketTypeNotFoundException("Ticket type with ID " + ticketTypeRequest.getId() + " not found");
                }
                existingTicketType.setName(ticketTypeRequest.getName());
                existingTicketType.setPrice(ticketTypeRequest.getPrice());
                existingTicketType.setDescription(ticketTypeRequest.getDescription());
                existingTicketType.setTotalAvailable(ticketTypeRequest.getTotalAvailable());
                existingTicketType.setDeck(ticketTypeRequest.getDeck() != null ?
                        DeckEnum.valueOf(ticketTypeRequest.getDeck()) : null);
                updatedTicketTypes.add(existingTicketType);
            }
        }
        // Cập nhật danh sách loại vé
        existingTrip.setTicketTypes(updatedTicketTypes);

        // Lưu chuyến xe đã cập nhật vào cơ sở dữ liệu
        Trip updatedTrip = tripRepository.save(existingTrip);
        log.info("Updated trip with ID: {}, ticket types: {}", updatedTrip.getId(), updatedTrip.getTicketTypes().size());
        return updatedTrip;
    }

    // Xóa một chuyến xe của nhà điều hành.
    // @param operatorId ID của nhà điều hành.
    // @param id ID của chuyến xe cần xóa.
    @Override
    public void deleteTripForOperator(UUID operatorId, UUID id) {
        log.info("Deleting trip with ID: {} for operator ID: {}", id, operatorId);
        Optional<Trip> optionalTrip = getTripForOperator(operatorId, id);
        optionalTrip.ifPresent(trip -> {
            tripRepository.delete(trip);
            log.info("Deleted trip with ID: {}", id);
        });
    }

    // Liệt kê các chuyến xe đã xuất bản (PUBLISHED) với phân trang.
    // @param pageable Đối tượng phân trang.
    // @return Trang (Page) chứa danh sách các chuyến xe đã xuất bản.
    @Override
    public Page<Trip> listPublishedTrips(Pageable pageable) {
        // Gọi repository để lấy các chuyến xe có trạng thái PUBLISHED
        return tripRepository.findByStatus(TripStatusEnum.PUBLISHED, pageable);
    }

    // Tìm kiếm các chuyến xe đã xuất bản dựa trên truy vấn văn bản.
    // @param query Chuỗi tìm kiếm (ví dụ: "Hanoi").
    // @param pageable Đối tượng phân trang.
    // @return Trang (Page) chứa danh sách các chuyến xe khớp với truy vấn.
    @Override
    public Page<Trip> searchPublishedTrips(String query, Pageable pageable) {
        //Goi repository de tim kiem van ban dua tren chuyen xe PUBLISHED
        return tripRepository.searchTrips(query, pageable);
    }

    // Lấy thông tin một chuyến xe đã xuất bản theo ID.
    // @param id ID của chuyến xe.
    // @return Optional chứa Trip nếu tìm thấy, hoặc rỗng nếu không tìm thấy.
    @Override
    public Optional<Trip> getPublishedTrip(UUID id) {
        //Nhan chuyen di cu the theo ID
        return tripRepository.findByIdAndStatus(id, TripStatusEnum.PUBLISHED);
    }
}
