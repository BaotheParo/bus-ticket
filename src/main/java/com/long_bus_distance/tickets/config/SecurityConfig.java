package com.long_bus_distance.tickets.config;

import com.long_bus_distance.tickets.repository.UserProvisioningFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserProvisioningFilter userProvisioningFilter, JWTAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Cho phép truy cập công khai vào điểm cuối GET /api/v1/published-trips
                        .requestMatchers("GET", "/api/v1/published-trips/**").permitAll()
                        // Chỉ cho phép STAFF mới xác thực vé
                        .requestMatchers("/api/v1/ticket-validations/**").hasRole("STAFF")
                        // Chỉ cho phép OPERATOR mới truy cập quản lý chuyến xe
                        .requestMatchers("/api/v1/trips/**").hasRole("OPERATOR")
                        .anyRequest().authenticated() // Đảm bảo tất cả yêu cầu HTTP đều phải xác thực
                )
                // Vô hiệu hóa cơ chế Cross-Site Request Forgery cho triển khai REST API
                .csrf(csrf -> csrf.disable())
                // Vì thiết kế REST API nên STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Xác thực bằng OAuth2 và JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        //chuyển đổi JWT Auth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                // Đảm bảo bộ lọc thực thi sau Bearer, đối tượng xác thực đã có mặt và người dùng được xác thực
                // => Cho phép bộ lọc truy cập JWT
                .addFilterAfter(userProvisioningFilter, BasicAuthenticationFilter.class);

        return http.build();
    }
}