package com.long_bus_distance.tickets.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JWTAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {
    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        // Trích role từ JWT
        List<SimpleGrantedAuthority> authorities = extractAuthorities(jwt);
        // Tạo và trả về hàm dưới với quyền được trích xuất
        return new JwtAuthenticationToken(jwt, authorities);
    }

    // Phương thức dùng để trích xuất role từ realm_access (dãy ký tự mã hóa) của JWT
    private List<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        // Lấy realm_access và xác nhận là Map (getClaimAsMap)
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        // Kiểm tra realm_access có null không hoặc có chưa role không
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        // Trích xuất role dạng List<STRING> và lọc vai trò bắt đầu bằng ROLE_
        return ((List<?>) realmAccess.get("roles")).stream()
                .filter(role -> role instanceof String && ((String) role).startsWith("ROLE_"))
                .map(role -> new SimpleGrantedAuthority((String) role))
                .collect(Collectors.toList());
    }
}