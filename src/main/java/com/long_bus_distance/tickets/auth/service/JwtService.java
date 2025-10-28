package com.long_bus_distance.tickets.auth.service;

import com.long_bus_distance.tickets.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUsername(String token);
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    boolean isTokenValid(String token, UserDetails userDetails);
}