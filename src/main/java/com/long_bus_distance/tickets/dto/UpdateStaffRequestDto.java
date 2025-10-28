package com.long_bus_distance.tickets.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateStaffRequestDto {
    // Không cho phép cập nhật username
    private String firstname;
    private String lastname;
    @Email(message = "Invalid email format")
    private String email;
    private LocalDate dateOfBirth;
}