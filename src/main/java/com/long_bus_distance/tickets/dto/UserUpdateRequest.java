package com.long_bus_distance.tickets.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    private String firstname;
    private String lastname;
    private LocalDate dateOfBirth;
    private Integer gender;
    private String phone;
}
