package com.long_bus_distance.tickets.repository;

import com.long_bus_distance.tickets.entity.TokenStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<TokenStorage, UUID> {

    Optional<TokenStorage> findByRefreshToken(String refreshToken);

    Optional<TokenStorage> findByUserId(UUID userId);

    // Optional: Tìm tất cả token hợp lệ của user (nếu cho phép nhiều session)
    // List<TokenStorage> findAllValidTokenByUser(UUID userId);
}
