package com.long_bus_distance.tickets.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data // Bao gồm @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "token_storage")
public class TokenStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true) // Mỗi user chỉ có 1 refresh token tại 1 thời điểm (có thể thay đổi logic này)
    private UUID userId;

    @Column(nullable = false, length = 1000) // Đủ dài để chứa refresh token
    private String refreshToken;

    @Column(nullable = false)
    private boolean revoked = false; // Token đã bị thu hồi (ví dụ: khi logout)

    @Column(nullable = false)
    private LocalDateTime expiresAt; // Thời gian hết hạn của refresh token

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
