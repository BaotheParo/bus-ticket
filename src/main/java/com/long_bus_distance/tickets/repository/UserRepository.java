package com.long_bus_distance.tickets.repository;

import com.long_bus_distance.tickets.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findAllByManagedByOperatorId(UUID operatorId);
    @Query("SELECT COUNT(u) FROM User u WHERE u.roles LIKE '%ROLE_PASSENGER%' " +
            "AND u.createdAt BETWEEN :start AND :end")
    Long countNewPassengersBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // (ADMIN) Đếm tổng số nhà xe
    @Query("SELECT COUNT(u) FROM User u WHERE u.roles LIKE '%ROLE_OPERATOR%'")
    Long countTotalOperators();
}