package com.long_bus_distance.tickets.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// Lớp Request là một đối tượng yêu cầu (request object) được sử dụng để truyền
// dữ liệu từ tầng controller đến tầng dịch vụ khi tạo một loại vé mới.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketTypeRequest {
    private String name;
    private double price;
    private String description;
    private Integer totalAvailable;
    private String deck; //TREN, DUOI, null (GHE)
}
