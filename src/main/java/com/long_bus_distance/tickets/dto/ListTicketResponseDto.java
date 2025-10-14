package com.long_bus_distance.tickets.dto;

import com.long_bus_distance.tickets.entity.TicketStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListTicketResponseDto {
    private String id;
    private TicketStatusEnum status;
    private Double price;
    private String selectedSeat;
    private String deckLabel;  // "B" từ Deck
    private String routeName;  // Từ Trip
}