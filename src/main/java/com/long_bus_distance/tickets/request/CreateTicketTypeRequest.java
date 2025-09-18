package com.long_bus_distance.tickets.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
