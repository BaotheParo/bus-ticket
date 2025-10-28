package com.long_bus_distance.tickets.auth.service;

import com.long_bus_distance.tickets.auth.dto.JwtAuthenticationResponse;
import com.long_bus_distance.tickets.auth.dto.RefreshTokenRequest;
import com.long_bus_distance.tickets.auth.dto.SignInRequest;
import com.long_bus_distance.tickets.auth.dto.SignUpRequest;
import com.long_bus_distance.tickets.entity.User;

public interface AuthenticationService {
    User signUp(SignUpRequest signUpRequest);
    JwtAuthenticationResponse signIn(SignInRequest signInRequest);
    JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}