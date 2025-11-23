
# Nền Tảng Đặt Vé Xe Khách Đường Dài

## Giới thiệu

**Nền Tảng Đặt Vé Xe Khách Đường Dài**, được xây dựng bằng **Spring Boot**. Hệ thống không chỉ quản lý vé xe khách cơ bản mà còn tích hợp các công nghệ nâng cao để xử lý bài toán thực tế như **Thanh toán Online (VNPay)** và **Xử lý chịu tải cao (High Concurrency)** với **Redis**.

Dự án hỗ trợ các vai trò: Nhà điều hành (Operator), Hành khách (Passenger), và Nhân viên (Staff).

## Tính năng Nổi bật (Mới cập nhật) 🔥

1.  **Tích hợp Thanh toán VNPay**:
    * Quy trình chuẩn: Giữ chỗ (Pending) -\> Thanh toán Sandbox -\> Xuất vé (Purchased).
    * Tự động hủy vé nếu không thanh toán sau 15 phút.
2.  **Xử lý Tranh chấp (High Concurrency)**:
    * Sử dụng **Redis Distributed Lock (Redisson)** để ngăn chặn việc 2 người cùng mua 1 ghế tại một thời điểm.
3.  **Tối ưu Hiệu năng**:
    * Sử dụng **Redis Cache** để tăng tốc độ tìm kiếm chuyến xe, giảm tải cho Database.

-----

## Yêu cầu cài đặt

Để chạy dự án, đảm bảo máy bạn đã cài:

1.  **Java JDK 21** (hoặc 17+).
2.  **Docker & Docker Desktop**: Để chạy PostgreSQL và Redis.
3.  **Maven**: Để build dự án.
4.  **Postman**: Để test API và luồng thanh toán.

-----

## Cấu hình môi trường

### 1\. Cấu hình Docker (Thêm Redis)

Chúng ta sử dụng Docker để chạy **PostgreSQL** và **Redis**.

**File:** `docker-compose.yml`

```yaml
version: '3.8'
services:
  # 1. Database PostgreSQL
  postgres:
    image: postgres:16
    container_name: busplatform_postgres
    environment:
      POSTGRES_DB: busplatform_db
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - busplatform_network

  # 2. Redis (Dùng cho Caching & Locking)
  redis:
    image: redis:7.2-alpine
    container_name: busplatform_redis
    ports:
      - "6379:6379"
    networks:
      - busplatform_network

  # 3. Keycloak (Tùy chọn nếu dùng Identity Provider)
  keycloak:
    image: quay.io/keycloak/keycloak:24.0.5
    container_name: busplatform_keycloak
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    command: start-dev
    ports:
      - "9090:8080"
    networks:
      - busplatform_network

volumes:
  postgres_data:

networks:
  busplatform_network:
    driver: bridge
```

#### Cách chạy:

1.  Mở terminal tại thư mục dự án.
2.  Chạy lệnh: `docker-compose up -d`
3.  Kiểm tra: `docker ps` (Phải thấy cả `busplatform_postgres` và `busplatform_redis` đang chạy).

-----

### 2\. Cập nhật Database (Quan trọng\!)

Do có sự thay đổi về trạng thái vé (thêm `PENDING_PAYMENT`, `FAILED`), bạn cần cập nhật lại cấu trúc bảng trong Database.

**Cách làm nhanh nhất (Reset DB):**

1.  Trong file `application.properties`, sửa dòng `ddl-auto` thành:
    ```properties
    spring.jpa.hibernate.ddl-auto=create
    ```
2.  Chạy ứng dụng Spring Boot **1 lần**. Hệ thống sẽ xóa bảng cũ, tạo bảng mới với ràng buộc đúng, và nạp lại dữ liệu từ `data.sql`.
3.  Sau khi chạy xong, đổi lại thành:
    ```properties
    spring.jpa.hibernate.ddl-auto=update
    ```

-----

### 3\. Cấu hình `application.properties`

Cập nhật file `src/main/resources/application.properties` với thông tin kết nối Docker và VNPay.

```properties
spring.application.name=tickets

# --- JPA & Database ---
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Kết nối Docker Postgres
spring.datasource.url=jdbc:postgresql://localhost:5432/busplatform_db
spring.datasource.username=admin
spring.datasource.password=admin

# --- Redis Configuration (Mới) ---
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis

# --- JWT Configuration ---
jwt.secret.access=RGF5TGFLaG9hQmlNYXRLaG9uZ0RlQ2hvQWlCaWV0UGhhaURvaU5vU2F1TmF5S2hpRGVwbG95
jwt.secret.refresh=Q29uS2hvYSBCaSBNYXQgTmF5IER1bmcgQ2hvIFJlZnJlc2hUb2tlbiBWYSBUaG9pSGFuTm8gTGFVLWhvblZpZUR1
jwt.expiration.access-ms=900000
jwt.expiration.refresh-ms=604800000

# --- VNPay Sandbox Config (Thay bằng Key của bạn) ---
# Đăng ký tại: https://sandbox.vnpayment.vn/dev
# Các biến này được sử dụng trong VNPayService.java (hoặc bạn có thể đưa vào đây để load động)
# vnpay.tmn-code=YOUR_TMN_CODE
# vnpay.hash-secret=YOUR_HASH_SECRET
# vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
# vnpay.return-url=http://localhost:8080/api/v1/payment/vnpay-return
```

-----

## Hướng dẫn Test Luồng Nghiệp vụ Mới

### 1\. Kịch bản Mua vé & Thanh toán

1.  **Login**: Đăng nhập user (Passenger).
2.  **Đặt vé**: Gọi API `POST /api/v1/tickets`.
    * Hệ thống kiểm tra ghế trống (Redis Lock).
    * Tạo vé trạng thái `PENDING_PAYMENT`.
    * Trả về **URL thanh toán VNPay**.
3.  **Thanh toán**:
    * Mở URL trả về trên trình duyệt.
    * Nhập thông tin thẻ test (Ngân hàng: `NCB`, Số thẻ: `9704198526191432198`, OTP: `123456`).
4.  **Hoàn tất**:
    * VNPay redirect về trang kết quả.
    * Hệ thống update vé thành `PURCHASED` và tạo QR Code.

### 2\. Kịch bản "Khoe" Hiệu năng (Redis)

* **Test Cache**: Tìm kiếm chuyến xe lần đầu sẽ query DB (chậm). Các lần sau sẽ lấy từ Redis (rất nhanh).
* **Test Race Condition**: Dùng Postman gửi 2 request mua cùng 1 ghế đồng thời. Chỉ 1 request thành công, request còn lại sẽ báo lỗi ngay lập tức nhờ Redis Lock.

### 3\. Tác vụ ngầm (Scheduled Task)

* Hệ thống tự động chạy mỗi phút để quét các vé `PENDING_PAYMENT` quá hạn (15 phút) và chuyển sang `CANCELLED` để nhả ghế.

-----

## Thông tin Thẻ Test VNPay (Sandbox)

* **Ngân hàng:** NCB
* **Số thẻ:** 9704198526191432198
* **Tên chủ thẻ:** NGUYEN VAN A
* **Ngày phát hành:** 07/15
* **Mật khẩu OTP:** 123456

Chúc các bạn code vui vẻ\! 🚀