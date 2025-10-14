package com.long_bus_distance.tickets.mapper;

import com.long_bus_distance.tickets.dto.CreateBusTypeRequestDto;
import com.long_bus_distance.tickets.dto.GetBusTypeResponseDto;
import com.long_bus_distance.tickets.entity.BusType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusTypeMapper {
    BusTypeMapper INSTANCE = Mappers.getMapper(BusTypeMapper.class);

    BusType fromDto(CreateBusTypeRequestDto dto);

    @Mapping(target = "defaultDecks", source = "defaultDecks")
    GetBusTypeResponseDto toDto(BusType busType);

    default List<GetBusTypeResponseDto> toDtoList(List<BusType> busTypes) {
        return busTypes.stream().map(this::toDto).collect(java.util.stream.Collectors.toList());
    }
}