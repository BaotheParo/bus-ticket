package com.long_bus_distance.tickets.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity //Đánh dấu lớp User là một thực thể JPA
@Table(name = "users") //Chỉ định tên bảng trong cơ sở dữ liệu sẽ là "users"
@Getter //Có thể sử dụng @Data ở đây để thay thế nếu muốn nhưng
@Setter //khuyến khích không nên sử dụng khi có quan hệ 2 chiều (bi-directional relationships)
@NoArgsConstructor //Tạo constructor không đối số
@AllArgsConstructor //Tạo constructor với tất cả các đối số
public class User {
    @Id
    @Column(name = "id", updatable = false,nullable = false)
    //Ánh xạ tới cột id trong bảng users.
    // Cột này không thể cập nhật
    //và không được phép null
    private UUID id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "date_of_birth",nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "email", unique = true, nullable = false)
    private String email;


    //Moi quan he
    @OneToMany(mappedBy = "operator", cascade = CascadeType.ALL)
    private List<Trip> organizedTrips; //Chuyen xe duoc quan li boi USER NG DIEU HANH

    @ManyToMany
    @JoinTable(
            name = "user_booked_trips",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "trip_id")
    )
    private List<Trip> bookedTrips; //Chuyen xe duoc mua boi USER khach hang

    @ManyToMany
    @JoinTable(
            name = "user_staffing_trips",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "trip_id")
    )
    private List<Trip> staffingTrips;

    //Audit Field
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username) && Objects.equals(password, user.password) && Objects.equals(lastname, user.lastname) && Objects.equals(dateOfBirth, user.dateOfBirth) && Objects.equals(email, user.email) && Objects.equals(created_at, user.created_at) && Objects.equals(updated_at, user.updated_at);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, lastname, dateOfBirth, email, created_at, updated_at);
    }
}
