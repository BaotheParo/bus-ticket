package com.long_bus_distance.tickets.auth.service.impl;

import com.long_bus_distance.tickets.auth.dto.JwtAuthenticationResponse;
import com.long_bus_distance.tickets.auth.dto.RefreshTokenRequest;
import com.long_bus_distance.tickets.auth.dto.SignInRequest;
import com.long_bus_distance.tickets.auth.dto.SignUpRequest;
import com.long_bus_distance.tickets.auth.service.AuthenticationService;
import com.long_bus_distance.tickets.auth.service.JwtService;
import com.long_bus_distance.tickets.entity.User;
import com.long_bus_distance.tickets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public User signUp(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setFirstname(signUpRequest.getFirstname());
        user.setLastname(signUpRequest.getLastname());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setDateOfBirth(signUpRequest.getDateOfBirth());
        user.setRoles("ROLE_PASSENGER"); // Default role for new users

        return userRepository.save(user);
    }

    @Override
    public JwtAuthenticationResponse signIn(SignInRequest signInRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));

        var user = userRepository.findByUsername(signInRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String username = jwtService.extractUsername(refreshTokenRequest.getRefreshToken());
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (jwtService.isTokenValid(refreshTokenRequest.getRefreshToken(), user)) {
            var accessToken = jwtService.generateAccessToken(user);
            return JwtAuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenRequest.getRefreshToken())
                    .build();
        }
        // Here you might want to throw an exception for an invalid refresh token
        return null;
    }
}