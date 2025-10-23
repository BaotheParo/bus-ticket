# --- Stage 1: Build (Xây dựng) ---
# SỬA LỖI: Nâng cấp lên JDK 21 để khớp với cấu hình project của bạn
FROM openjdk:21-jdk-slim AS builder

# Đặt thư mục làm việc bên trong container
WORKDIR /app

# Copy file pom.xml và source code (nếu dùng Maven Wrapper)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Tải dependencies (giúp tận dụng cache của Docker)
RUN ./mvnw dependency:go-offline

# Copy toàn bộ source code
COPY src ./src

# Build ứng dụng và tạo file .jar
RUN ./mvnw package -DskipTests

# --- Stage 2: Run (Chạy) ---
# SỬA LỖI: Nâng cấp JRE lên 21 để chạy code đã build
FROM openjdk:21-slim

# Đặt thư mục làm việc
WORKDIR /app

# Copy file .jar đã được build từ stage 'builder'
COPY --from=builder /app/target/*.jar app.jar

# Expose cổng mà ứng dụng Spring Boot của bạn đang chạy (mặc định là 8080)
EXPOSE 8080

# Lệnh để chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]