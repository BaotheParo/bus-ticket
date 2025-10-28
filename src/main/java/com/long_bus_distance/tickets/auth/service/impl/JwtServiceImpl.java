package com.long_bus_distance.tickets.auth.service.impl;

import com.long_bus_distance.tickets.auth.service.JwtService;
import com.long_bus_distance.tickets.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret.access}")
    private String jwtAccessSecret;

    @Value("${jwt.secret.refresh}")
    private String jwtRefreshSecret;

    @Value("${jwt.expiration.access-ms}")
    private long jwtAccessExpiration;

    @Value("${jwt.expiration.refresh-ms}")
    private long jwtRefreshExpiration;

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String generateAccessToken(User user) {
        return generateToken(user, jwtAccessExpiration, getSignInKey(jwtAccessSecret));
    }

    @Override
    public String generateRefreshToken(User user) {
        return generateToken(user, jwtRefreshExpiration, getSignInKey(jwtRefreshSecret));
    }

    private String generateToken(User user, long expirationTime, Key key) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // Use the access secret key for parsing, as this method is used for validation
        return Jwts.parserBuilder().setSigningKey(getSignInKey(jwtAccessSecret)).build().parseClaimsJws(token).getBody();
    }

    private Key getSignInKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}