package com.long_bus_distance.tickets.repository;

import com.long_bus_distance.tickets.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> { //Su dung interface

}
