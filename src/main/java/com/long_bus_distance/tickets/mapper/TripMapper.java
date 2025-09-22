package com.long_bus_distance.tickets.mapper;

import com.long_bus_distance.tickets.dto.*;
import com.long_bus_distance.tickets.entity.TicketType;
import com.long_bus_distance.tickets.entity.Trip;
import com.long_bus_distance.tickets.request.CreateTicketTypeRequest;
import com.long_bus_distance.tickets.request.CreateTripRequest;
import com.long_bus_distance.tickets.request.UpdateTripRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

// Giao diện TripMapper định nghĩa các phương thức ánh xạ giữa entity và DTO.
// Sử dụng MapStruct để tự động tạo mã ánh xạ.
// @Mapper(componentModel = "spring") tích hợp với Spring để tạo bean.
// unmappedTargetPolicy = ReportingPolicy.IGNORE bỏ qua các thuộc tính không được ánh xạ.
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TripMapper {

    // Ánh xạ từ CreateTicketTypeRequestDto sang CreateTicketTypeRequest.
    // @param dto DTO chứa thông tin tạo loại vé.
    // @return Đối tượng yêu cầu tạo loại vé dùng trong logic dịch vụ.
    CreateTicketTypeRequest fromDto(CreateTicketTypeRequestDto dto);

    // Ánh xạ từ CreateTripRequestDto sang CreateTripRequest.
    // @param dto DTO chứa thông tin tạo chuyến xe.
    // @return Đối tượng yêu cầu tạo chuyến xe dùng trong logic dịch vụ.
    CreateTripRequest fromDto(CreateTripRequestDto dto);

    // Ánh xạ từ entity Trip sang CreateTripResponseDto.
    // @param trip Entity chuyến xe.
    // @return DTO chứa thông tin chuyến xe sau khi tạo.
    // @Mapping đảm bảo ánh xạ id và busType, với busType mặc định là "STANDARD" nếu null.
    @Mapping(source = "id", target = "id")
    @Mapping(source = "busType", target = "busType", defaultValue = "STANDARD")
    CreateTripResponseDto toDto(Trip trip);

    // Ánh xạ từ entity TicketType sang ListTicketTypeResponseDto.
    // @param ticketType Entity loại vé.
    // @return DTO chứa thông tin loại vé để hiển thị trong danh sách.
    // Sửa lỗi typo: ListTicketTypeResponeDto -> ListTicketTypeResponseDto.
    @Mapping(source = "id", target = "id")
    @Mapping(source = "deck", target = "deck")
    ListTicketTypeResponseDto toListTicketResponseDto(TicketType ticketType);

    // Ánh xạ từ entity Trip sang ListTripResponseDto.
    // @param trip Entity chuyến xe.
    // @return DTO chứa thông tin chuyến xe để hiển thị trong danh sách.
    @Mapping(source = "id", target = "id")
    @Mapping(source = "busType", target = "busType")
    ListTripResponseDto toListTripResponseDto(Trip trip);

    // Ánh xạ từ entity TicketType sang GetTicketTypeDetailsResponseDto.
    // @param ticketType Entity loại vé.
    // @return DTO chứa chi tiết thông tin loại vé.
    // Chuyển deck từ DeckEnum sang String bằng expression.
    @Mapping(source = "id", target = "id")
    @Mapping(source = "deck", target = "deck")
    GetTicketTypeDetailsResponseDto toGetTicketTypeDetailsResponseDto(TicketType ticketType);

    // Ánh xạ từ entity Trip sang GetTripDetailsResponseDto.
    // @param trip Entity chuyến xe.
    // @return DTO chứa chi tiết thông tin chuyến xe.
    @Mapping(source = "id", target = "id")
    @Mapping(source = "busType", target = "busType")
    GetTripDetailsResponseDto toGetTripDetailsResponseDto (Trip trip);

    // Ánh xạ từ UpdateTicketTypeRequestDto sang UpdateTicketTypeRequest.
    // @param dto DTO chứa thông tin cập nhật loại vé.
    // @return Đối tượng yêu cầu cập nhật loại vé dùng trong logic dịch vụ.
    UpdateTripRequest fromUpdateTicketTypeRequestDto(UpdateTicketTypeRequestDto dto);

    // Ánh xạ từ UpdateTripRequestDto sang UpdateTripRequest.
    // @param dto DTO chứa thông tin cập nhật chuyến xe.
    // @return Đối tượng yêu cầu cập nhật chuyến xe dùng trong logic dịch vụ.
    UpdateTripRequest fromUpdateTripRequestDto (UpdateTripRequestDto dto);

    // Ánh xạ từ entity TicketType sang UpdateTicketTypeResponseDto.
    // @param ticketType Entity loại vé.
    // @return DTO chứa thông tin loại vé sau khi cập nhật.
    @Mapping(source = "id",target = "id")
    @Mapping(source = "deck", target = "deck")
    UpdateTicketTypeResponseDto toUpdateTicketTypeResponseDto(TicketType ticketType);

    // Ánh xạ từ entity Trip sang UpdateTripResponseDto.
    // @param trip Entity chuyến xe.
    // @return DTO chứa thông tin chuyến xe sau khi cập nhật.
    @Mapping(source = "id", target = "id")
    @Mapping(source = "busType", target = "busType")
    UpdateTripResponseDto toUpdateTripResponseDto(Trip trip);

    // Ánh xạ từ entity Trip sang ListPublishedTripResponseDto.
    // @param trip Entity chuyến xe.
    // @return DTO chứa thông tin chuyến xe đã xuất bản để hiển thị trong danh sách công khai.
    @Mapping(source = "id", target = "id")
    ListPublishedTripResponseDto toListPublishedTripResponseDto(Trip trip);

    // Ánh xạ từ entity Trip sang GetPublishedTripDetailsResponseDto.
    // @param trip Entity chuyến xe.
    // @return DTO chứa chi tiết thông tin chuyến xe đã xuất bản.
    @Mapping(source = "id", target = "id")
    GetPublishedTripDetailsResponseDto toGetPublishedTripDetailsResponseDto(Trip trip);

    // Ánh xạ từ entity TicketType sang GetPublishedTripDetailsTicketTypesResponseDto.
    // @param ticketType Entity loại vé.
    // @return DTO chứa thông tin loại vé của chuyến xe đã xuất bản.
    // Chuyển deck từ DeckEnum sang String bằng expression.
    @Mapping(source = "id", target = "id")
    GetPublishedTripDetailsTicketTypesResponseDto toGetPublishedTripDetailsTicketTypesResponseDto (TicketType ticketType);
}
