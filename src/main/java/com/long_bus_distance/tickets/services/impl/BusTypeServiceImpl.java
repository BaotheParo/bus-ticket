package com.long_bus_distance.tickets.services.impl;

import com.long_bus_distance.tickets.dto.CreateBusTypeRequestDto;
import com.long_bus_distance.tickets.dto.UpdateBusTypeRequestDto;
import com.long_bus_distance.tickets.entity.BusType;
import com.long_bus_distance.tickets.exception.BusTicketException;
import com.long_bus_distance.tickets.repository.BusTypeRepository;
import com.long_bus_distance.tickets.repository.TripRepository;
import com.long_bus_distance.tickets.services.BusTypeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusTypeServiceImpl implements BusTypeService {
    private final BusTypeRepository busTypeRepository;
    private final TripRepository tripRepository;

    private final UUID UNDEFINED_BUS_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");  // ID cố định cho UNDEFINED (seed sẽ tạo)

    @Override
    public BusType createBusType(CreateBusTypeRequestDto dto) {
        log.info("Tạo BusType mới: {}", dto.getName());
        if (busTypeRepository.existsByName(dto.getName())) {
            throw new BusTicketException("Tên BusType đã tồn tại: " + dto.getName());
        }
        BusType busType = new BusType();
        busType.setName(dto.getName());
        busType.setDescription(dto.getDescription());
        busType.setNumDecks(dto.getNumDecks());
        busType.setSeatsPerDeck(dto.getSeatsPerDeck());
        busType.setPriceFactor(dto.getPriceFactor() != null ? dto.getPriceFactor() : 1.0);
        busType.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        busType.generateDefaultDecks();  // Auto tạo decks
        BusType saved = busTypeRepository.save(busType);
        log.info("Tạo thành công BusType ID: {}", saved.getId());
        return saved;
    }

    @Override
    public Optional<BusType> getById(UUID id) {
        log.info("Lấy BusType ID: {}", id);
        return busTypeRepository.findById(id);
    }

    @Override
    public List<BusType> listBusTypes() {
        log.info("Liệt kê tất cả BusTypes");
        return busTypeRepository.findAll();
    }

    @Override
    public BusType updateBusType(UUID id, UpdateBusTypeRequestDto dto) {
        log.info("Cập nhật BusType ID: {}", id);
        BusType busType = busTypeRepository.findById(id)
                .orElseThrow(() -> new BusTicketException("BusType không tìm thấy: " + id));
        if (busType.getIsDefault()) {
            throw new BusTicketException("Không thể cập nhật BusType mặc định");
        }
        busType.setDescription(dto.getDescription());
        busType.setNumDecks(dto.getNumDecks());
        busType.setSeatsPerDeck(dto.getSeatsPerDeck());
        busType.setPriceFactor(dto.getPriceFactor() != null ? dto.getPriceFactor() : busType.getPriceFactor());
        busType.generateDefaultDecks();  // Re-generate decks nếu thay đổi numDecks/seatsPerDeck
        BusType updated = busTypeRepository.save(busType);
        log.info("Cập nhật thành công BusType ID: {}", updated.getId());
        return updated;
    }

    @Override
    public void deleteBusType(UUID id) {
        log.info("Xóa BusType ID: {}", id);
        BusType busType = busTypeRepository.findById(id)
                .orElseThrow(() -> new BusTicketException("BusType không tìm thấy: " + id));
        if (busType.getIsDefault()) {
            throw new BusTicketException("Không thể xóa BusType mặc định");
        }
        // Migrate Trips sang UNDEFINED_BUS
        tripRepository.updateBusTypeToUndefined(id, UNDEFINED_BUS_ID);
        log.info("Đã migrate Trips sang UNDEFINED_BUS");
        busTypeRepository.delete(busType);
        log.info("Xóa thành công BusType ID: {}", id);
    }

    @PostConstruct
    public void seedDefaults() {
        log.info("Seeding default BusTypes...");
        if (!busTypeRepository.existsByName("Xe ghế ngồi")) {
            CreateBusTypeRequestDto seatingDto = new CreateBusTypeRequestDto();
            seatingDto.setName("Xe ghế ngồi");
            seatingDto.setDescription("Xe buýt ghế tiêu chuẩn");
            seatingDto.setNumDecks(1);
            seatingDto.setSeatsPerDeck(45);
            seatingDto.setPriceFactor(1.0);
            seatingDto.setIsDefault(true);
            createBusType(seatingDto);
        }
        if (!busTypeRepository.existsByName("Xe giường nằm")) {
            CreateBusTypeRequestDto sleeperDto = new CreateBusTypeRequestDto();
            sleeperDto.setName("Xe giường nằm");
            sleeperDto.setDescription("Xe buýt giường nằm 2 tầng");
            sleeperDto.setNumDecks(2);
            sleeperDto.setSeatsPerDeck(17);
            sleeperDto.setPriceFactor(1.2);
            sleeperDto.setIsDefault(true);
            createBusType(sleeperDto);
        }
        if (!busTypeRepository.existsByName("Không xác định")) {
            CreateBusTypeRequestDto undefinedDto = new CreateBusTypeRequestDto();
            undefinedDto.setName("Không xác định");
            undefinedDto.setDescription("Fallback khi xóa BusType");
            undefinedDto.setNumDecks(1);
            undefinedDto.setSeatsPerDeck(0);
            undefinedDto.setPriceFactor(1.0);
            undefinedDto.setIsDefault(true);
            createBusType(undefinedDto);
        }
        log.info("Seeding hoàn tất");
    }
}