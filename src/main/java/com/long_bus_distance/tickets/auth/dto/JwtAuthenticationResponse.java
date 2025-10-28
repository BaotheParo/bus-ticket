package com.long_bus_distance.tickets.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtAuthenticationResponse {
    private String accessToken;
    private String refreshToken;
}