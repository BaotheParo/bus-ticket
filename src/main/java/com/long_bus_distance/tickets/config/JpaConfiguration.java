package com.long_bus_distance.tickets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Lớp JpaConfiguration cấu hình các tính năng JPA cho ứng dụng.
// Sử dụng @Configuration để đánh dấu đây là một lớp cấu hình của Spring.
// @EnableJpaAuditing kích hoạt tính năng auditing của Spring Data JPA để tự động quản lý các trường như createdAt, updatedAt.
@Configuration
@EnableJpaAuditing
public class JpaConfiguration {
}
