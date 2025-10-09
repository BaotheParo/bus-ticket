# Nền Tảng Đặt Vé Xe Khách Đường Dài

## Giới thiệu
Chào các bạn trong nhóm! Đây là dự án **Nền Tảng Đặt Vé Xe Khách Đường Dài**, được xây dựng bằng **Spring Boot** với mục tiêu cung cấp một hệ thống quản lý vé xe khách hiệu quả, hỗ trợ các vai trò như nhà điều hành xe (bus operator), hành khách (passenger), và nhân viên xe (bus staff). Dự án sử dụng **Keycloak** để quản lý danh tính và xác thực người dùng, tích hợp với **Spring Security** và **JPA** để quản lý dữ liệu và bảo mật.

Dưới đây là hướng dẫn cài đặt và tổng quan về những gì đã được triển khai để các bạn có thể bắt đầu làm việc cùng mình.

## Yêu cầu cài đặt
Để chạy dự án trên máy tính của bạn, hãy đảm bảo bạn đã cài đặt các công cụ sau:

1. **Java Development Kit (JDK)**: Phiên bản 17 hoặc cao hơn.
    - Tải và cài đặt từ [Oracle](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) hoặc [OpenJDK](https://adoptium.net/).
2. **Maven**: Công cụ quản lý phụ thuộc và build dự án.
    - Tải từ [Maven](https://maven.apache.org/download.cgi) hoặc sử dụng wrapper (`mvnw`) trong dự án.
3. **Docker**: Để chạy PostgreSQL và Keycloak trong container.
    - Cài đặt từ [Docker](https://www.docker.com/get-started).
4. **IDE**: IntelliJ IDEA, Eclipse, hoặc VS Code (khuyến nghị IntelliJ cho Spring Boot).
5. **Postman** (tùy chọn): Để test API.
    - Tải từ [Postman](https://www.postman.com/downloads/).

## Cấu hình môi trường

### 1. Chạy ứng dụng bằng Docker
Chúng ta sử dụng Docker để chạy **PostgreSQL** và **Keycloak** trong container, đảm bảo môi trường nhất quán. Dưới đây là các bước để thiết lập và chạy:

#### **Bước 1: Tạo file `docker-compose.yml`**
Tạo file `docker-compose.yml` trong thư mục gốc của dự án với nội dung sau:

```yaml
version: '3.8'
services:
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

**Giải thích**:
- **Postgres**: Chạy PostgreSQL với database `busplatform_db`, user `admin`, password `admin` trên port `5432`.
- **Keycloak**: Chạy Keycloak trên port `9090` (ánh xạ từ port 8080 trong container), với tài khoản admin mặc định (`admin/admin`).
- **Volumes**: Lưu trữ dữ liệu PostgreSQL để không mất khi container dừng.
- **Networks**: Tạo mạng `busplatform_network` để các service giao tiếp.

#### **Bước 2: Chạy Docker Compose**
1. Mở terminal trong thư mục chứa `docker-compose.yml`.
2. Chạy lệnh:
   ```bash
   docker-compose up -d
   ```
    - `-d`: Chạy ở chế độ nền (detached).
3. Kiểm tra container đang chạy:
   ```bash
   docker ps
   ```
   Bạn sẽ thấy 2 container: `busplatform_postgres` và `busplatform_keycloak`.

#### **Bước 3: Cấu hình `application.properties`**
Cập nhật file `src/main/resources/application.properties` để kết nối với PostgreSQL và Keycloak trong Docker:

```properties
# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/busplatform_db
spring.datasource.username=admin
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update

# Keycloak configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/trip-ticket-platform
```

**Lưu ý**:
- Nếu bạn đổi tên database, user, hoặc password trong `docker-compose.yml`, cập nhật tương ứng trong `application.properties`.
- Keycloak issuer URI sử dụng port `9090` vì ánh xạ từ `8080` trong container.

#### **Bước 4: Chạy ứng dụng Spring Boot**
1. Mở dự án trong IDE.
2. Chạy lệnh để tải phụ thuộc:
   ```bash
   mvn clean install
   ```
3. Chạy ứng dụng:
   ```bash
   mvn spring-boot:run
   ```
   Hoặc chạy trực tiếp từ IDE (file `TicketsApplication.java`).
4. Kiểm tra ứng dụng:
    - Truy cập `http://localhost:8080` để xác nhận API chạy.
    - Dùng Postman để test các endpoint (xem phần test API).

### 2. Thiết lập Keycloak
Keycloak được sử dụng để quản lý danh tính và xác thực JWT. Dưới đây là cách thiết lập realm và client để lấy token.

#### **Bước 1: Truy cập Keycloak Admin Console**
1. Mở trình duyệt, truy cập: `http://localhost:9090`.
2. Đăng nhập với:
    - Username: `admin`
    - Password: `admin`
3. Nếu lần đầu, bạn có thể được yêu cầu đổi mật khẩu admin.

#### **Bước 2: Tạo Realm**
1. Trong giao diện Keycloak, click **Add realm** (hoặc chọn từ dropdown góc trên bên trái).
2. **Name**: `trip-ticket-platform`.
3. **Save**.

#### **Bước 3: Tạo Client**
1. Trong realm `trip-ticket-platform`, vào tab **Clients** > **Create client**.
2. Cấu hình client:
    - **Client type**: OpenID Connect.
    - **Client ID**: `busplatform-client`.
    - **Name**: Bus Platform Client (tùy chọn).
    - **Client authentication**: **On** (confidential client, yêu cầu client secret).
    - **Standard flow**: On (cho authorization code flow nếu có frontend).
    - **Direct access grants**: **On** (cho phép lấy token bằng `grant_type=password`).
    - **Service accounts roles**: On (cho client credentials nếu cần).
    - **Valid redirect URIs**: `http://localhost:8080/*` (hoặc `*` để test).
    - **Web origins**: `*` (cho CORS).
    - **Save**.
3. Lấy **Client Secret**:
    - Vào tab **Credentials** của client `busplatform-client`.
    - Copy **Client secret** (e.g., `abc123def456`) để dùng trong Postman.

#### **Bước 4: Tạo Roles**
1. Vào tab **Realm roles** > **Create role**.
2. Tạo các role:
    - `OPERATOR` (cho nhà điều hành).
    - `PASSENGER` (cho hành khách).
    - `STAFF` (cho nhân viên xác thực vé).
3. **Save** mỗi role.

#### **Bước 5: Tạo User**
1. Vào tab **Users** > **Add user**.
2. Điền:
    - **Username**: `test-user`.
    - **Email**: `test@example.com` (tùy chọn).
    - **First Name**, **Last Name**: Tùy chọn.
3. **Save**.
4. Đặt mật khẩu:
    - Vào tab **Credentials** > **Set Password**.
    - **Password**: `password`.
    - **Temporary**: **Off** (để tránh yêu cầu reset mật khẩu).
    - **Save**.
5. Gán role:
    - Vào tab **Role Mappings** > Chọn **Client Roles** > `busplatform-client`.
    - Gán role (e.g., `PASSENGER`, `STAFF`, hoặc `OPERATOR`) từ **Available Roles** sang **Assigned Roles**.
    - **Save**.

#### **Bước 6: Test lấy JWT Token**
1. Mở Postman, tạo request mới:
    - **Method**: POST
    - **URL**: `http://localhost:9090/realms/trip-ticket-platform/protocol/openid-connect/token`
    - **Headers**:
      ```
      Content-Type: application/x-www-form-urlencoded
      ```
    - **Body** (tab `x-www-form-urlencoded`):
      ```
      grant_type: password
      client_id: busplatform-client
      client_secret: <your-client-secret>
      username: test-user
      password: password
      scope: openid profile roles
      ```
2. Nhấn **Send**:
    - **Kỳ vọng**: Status `200 OK`, response chứa `access_token`:
      ```json
      {
          "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6...",
          "expires_in": 300,
          "refresh_token": "...",
          "token_type": "Bearer",
          "scope": "openid profile roles"
      }
      ```
3. Copy `access_token` để dùng trong các request API (header `Authorization: Bearer <access_token>`).

#### **Bước 7: Dừng Docker nếu cần**
- Dừng container:
  ```bash
  docker-compose down
  ```
- Xóa dữ liệu (nếu cần reset):
  ```bash
  docker-compose down -v
  ```

## Tổng quan về công việc đã thực hiện
Dự án đã hoàn thành các phần sau để thiết lập nền tảng cho hệ thống đặt vé xe khách:

### 1. Các Entity
Các thực thể JPA đã được thiết kế và triển khai để mô hình hóa dữ liệu của hệ thống, đặt trong gói `com.long_bus_distance.tickets.entity`:

- **User**:
    - Đại diện cho người dùng (nhà điều hành, hành khách, nhân viên).
    - Các trường: `id` (UUID từ Keycloak), `username`, `password`, `firstName`, `lastName`, `dateOfBirth`, `email`, `role` (enum: `PASSENGER`, `OPERATOR`, `STAFF`), `createdAt`, `updatedAt`.
    - Quan hệ:
        - `organizedTrips` (One-to-Many với `Trip`): Chuyến xe do nhà điều hành quản lý.
        - `bookedTrips` (Many-to-Many với `Trip`): Chuyến xe hành khách đã đặt.
        - `staffingTrips` (Many-to-Many với `Trip`): Chuyến xe nhân viên làm việc.

- **Trip**:
    - Đại diện cho một chuyến xe khách.
    - Các trường: `id` (UUID), `routeName`, `departureTime`, `departurePoint`, `arrivalTime`, `destination`, `durationMinutes`, `busType` (enum: `STANDARD`, `SLEEPER`, `LIMOUSINE`), `tripSchedule`, `salesStart`, `salesEnd`, `status` (enum: `DRAFT`, `PUBLISHED`, `CANCELLED`, `COMPLETED`), `createdAt`, `updatedAt`.
    - Quan hệ:
        - `operator` (Many-to-One với `User`): Nhà điều hành chuyến xe.
        - `passengers` (Many-to-Many với `User`): Hành khách đã đặt vé.
        - `staff` (Many-to-Many với `User`): Nhân viên làm việc.
        - `ticketTypes` (One-to-Many với `TicketType`): Các loại vé của chuyến xe.

- **TicketType**:
    - Đại diện cho loại vé (ví dụ: ghế tiêu chuẩn, giường nằm tầng trên).
    - Các trường: `id` (UUID), `name`, `price`, `description` (nullable), `totalAvailable`, `deck` (enum: `UPPER`, `LOWER`, null cho tiêu chuẩn), `createdAt`, `updatedAt`.
    - Quan hệ:
        - `trip` (Many-to-One với `Trip`): Chuyến xe liên quan.
        - `tickets` (One-to-Many với `Ticket`): Các vé thuộc loại này.

- **Ticket**:
    - Đại diện cho một vé đã mua.
    - Các trường: `id` (UUID), `status` (enum: `PURCHASED`, `CANCELLED`), `selectedSeat` (nullable, ví dụ: "UPPER_A1"), `createdAt`, `updatedAt`.
    - Quan hệ:
        - `ticketType` (Many-to-One với `TicketType`): Loại vé.
        - `purchaser` (Many-to-One với `User`): Người mua vé.
        - `validations` (One-to-Many với `TicketValidation`): Các lần xác thực vé.
        - `qrCodes` (One-to-Many với `QRCode`): Các mã QR liên quan.

- **TicketValidation**:
    - Đại diện cho một lần xác thực vé (ví dụ: quét QR).
    - Các trường: `id` (UUID), `status` (enum: `VALID`, `INVALID`, `EXPIRED`), `validationMethod` (enum: `QR_SCAN`, `MANUAL`), `createdAt`, `updatedAt`.
    - Quan hệ:
        - `ticket` (Many-to-One với `Ticket`): Vé được xác thực.

- **QRCode**:
    - Đại diện cho mã QR của một vé.
    - Các trường: `id` (UUID), `status` (enum: `ACTIVE`, `EXPIRED`), `value` (nội dung mã QR), `createdAt`, `updatedAt`.
    - Quan hệ:
        - `ticket` (Many-to-One với `Ticket`): Vé liên quan.

### 2. Cấu hình JPA Auditing
- **JPA Auditing**: Đã kích hoạt thông qua lớp `JPAConfiguration` với annotation `@EnableJpaAuditing` và file `src/main/resources/META-INF/orm.xml`, đảm bảo các trường `createdAt` và `updatedAt` được tự động điền.
- **orm.xml**: Đăng ký `AuditingEntityListener` để áp dụng kiểm toán cho tất cả các entity.

### 3. Spring Security và User Provisioning
- **SecurityConfig**: Cấu hình bảo mật với Spring Security:
    - Yêu cầu xác thực cho tất cả các yêu cầu HTTP, trừ `/api/v1/published-trips/**` (public).
    - Vô hiệu hóa CSRF vì đây là REST API.
    - Sử dụng quản lý phiên không trạng thái (stateless) với OAuth2 JWT.
    - Tích hợp `UserProvisioningFilter` để tự động tạo người dùng từ Keycloak JWT.
- **UserProvisioningFilter**: Kiểm tra và tạo người dùng trong cơ sở dữ liệu dựa trên thông tin JWT (ID, username, email) sau khi xác thực.
- **UserRepository**: Cung cấp các thao tác CRUD cho entity `User`.

### 4. DTOs và Service Layer
- **DTOs**:
    - `CreateTripRequest`: Chứa thông tin để tạo chuyến xe (`routeName`, `departureTime`, `arrivalTime`, v.v.) và danh sách `CreateTicketTypeRequest`.
    - `CreateTicketTypeRequest`: Chứa thông tin để tạo loại vé (`name`, `price`, `description`, `totalAvailable`, `deck`).
- **TripService**: Giao diện định nghĩa phương thức `createTrip` để tạo chuyến xe, nhận `CreateTripRequest` và `operatorId` (UUID).

### 5. Xử lý Ngoại lệ
- **BusTicketException**: Lớp ngoại lệ cha cho tất cả các ngoại lệ tùy chỉnh, kế thừa `RuntimeException`.
- **UserNotFoundException**: Ngoại lệ cụ thể khi `operatorId` không tồn tại trong cơ sở dữ liệu.

## Hướng dẫn bắt đầu làm việc
1. **Kéo mã nguồn**:
    - Clone repository từ Git (liên hệ mình để lấy URL).
    - Mở dự án trong IDE và chạy `mvn clean install` để tải phụ thuộc.

2. **Khám phá mã nguồn**:
    - Các entity nằm trong `com.long_bus_distance.tickets.entity`.
    - DTOs nằm trong `com.long_bus_distance.tickets.domain`.
    - Dịch vụ trong `com.long_bus_distance.tickets.service`.
    - Bộ lọc và cấu hình trong `com.long_bus_distance.tickets.filters` và `com.long_bus_distance.tickets.config`.

3. **Nhiệm vụ tiếp theo**:
    - **Triển khai TripService**: Viết lớp triển khai cho `TripService` để xử lý logic tạo chuyến xe, bao gồm chuyển đổi DTO sang entity và kiểm tra dữ liệu.
    - **API Endpoints**: Tạo các REST controller để xử lý yêu cầu tạo, cập nhật, và truy vấn chuyến xe/vé.
    - **QR Code Generation**: Tích hợp thư viện để tạo mã QR cho `QRCode` entity.
    - **Validation Logic**: Thêm logic kiểm tra vé qua QR code hoặc nhập tay.

4. **Phân chia công việc**:
    - Bạn có thể chọn triển khai `TripService` hoặc REST controllers.
    - Hãy liên hệ để phân công nhiệm vụ cụ thể và tránh xung đột code.

## Lưu ý
- Đảm bảo Docker, PostgreSQL, và Keycloak đang chạy trước khi khởi động ứng dụng.
- Kiểm tra `application.properties` để cấu hình đúng thông tin database và Keycloak.
- Nếu gặp lỗi, kiểm tra log trong console hoặc liên hệ mình để debug.

Chào mừng các bạn tham gia dự án! Hãy thoải mái đặt câu hỏi nếu cần làm rõ bất kỳ phần nào. 😊