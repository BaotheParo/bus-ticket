package com.long_bus_distance.tickets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserProvisioningFilter userProvisioningFilter, JWTAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Công khai: Liệt kê chuyến PUBLISHED
                        .requestMatchers("GET", "/api/v1/published-trips/**").permitAll()
                        // OPERATOR: Quản lý Trip/BusType
                        .requestMatchers("/api/v1/trips/**").hasRole("OPERATOR")
                        .requestMatchers("/api/v1/bus-types/**").hasRole("OPERATOR")
                        .requestMatchers("GET", "/api/v1/bus-types").permitAll()  // GET list công khai
                        // STAFF: Xác thực vé
                        .requestMatchers("/api/v1/ticket-validations/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/tickets/**").authenticated()  // User authenticated cho vé cá nhân
                        // USER: Vé cá nhân
                        .requestMatchers("/api/v1/tickets/**").authenticated()
                        .requestMatchers("/api/v1/ticket-validations/**").hasRole("STAFF")
                        .requestMatchers("GET", "/api/v1/trips/**").permitAll()  // Bao gồm /seats cho public
                        .requestMatchers("POST", "PUT", "DELETE", "/api/v1/trips/**").hasRole("OPERATOR")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                )
                .addFilterAfter(userProvisioningFilter, BasicAuthenticationFilter.class);  // Provision user sau JWT validate

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = "http://localhost:9090/realms/trip-ticket-platform";
        return NimbusJwtDecoder.withJwkSetUri(issuerUri + "/protocol/openid-connect/certs").build();
    }
}