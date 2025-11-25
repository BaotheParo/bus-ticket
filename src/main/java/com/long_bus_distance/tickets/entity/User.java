package com.long_bus_distance.tickets.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "roles", nullable = false)
    private String roles;

    @Column(name = "gender")
    private Integer gender; // 1: Male, 0: Female

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // Mặc định là true khi tạo mới

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "managed_by_operator_id")
    @JsonIgnore // Tránh vòng lặp JSON khi trả về
    private User managedByOperator; // Chỉ có giá trị khi user là STAFF

    // Relations
    @OneToMany(mappedBy = "operator", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Trip> organizedTrips;

    @ManyToMany
    @JoinTable(
            name = "user_booked_trips",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "trip_id")
    )
    @JsonIgnore
    private List<Trip> bookedTrips;

    @ManyToMany
    @JoinTable(
            name = "user_staffing_trips",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "trip_id")
    )
    @JsonIgnore
    private List<Trip> staffingTrips;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // --- CẬP NHẬT MỚI ---
    // Phương thức này giờ sẽ kiểm tra trạng thái active của tài khoản
    @Override
    public boolean isAccountNonLocked() {
        return this.isActive;
    }
    // --- KẾT THÚC CẬP NHẬT ---

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}