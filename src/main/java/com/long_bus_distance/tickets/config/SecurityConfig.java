package com.long_bus_distance.tickets.config;

import org.springframework.beans.factory.annotation.Value; // <-- THÊM IMPORT NÀY
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

    // --- BƯỚC 1: Tiêm (inject) giá trị issuer-url từ file properties/biến môi trường ---
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-url}")
    private String issuerUri;

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
                                .decoder(jwtDecoder()) // Sẽ gọi hàm jwtDecoder() bên dưới
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                )
                .addFilterAfter(userProvisioningFilter, BasicAuthenticationFilter.class);  // Provision user sau JWT validate

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // --- BƯỚC 2: Sử dụng biến 'issuerUri' đã được tiêm (inject) ---
        // Xóa dòng gõ cứng "localhost:9090"
        // String issuerUri = "http://localhost:9090/realms/trip-ticket-platform";

        // Sử dụng giá trị từ biến môi trường (sẽ là http://keycloak:8080/...)
        return NimbusJwtDecoder.withJwkSetUri(this.issuerUri + "/protocol/openid-connect/certs").build();
    }
}

