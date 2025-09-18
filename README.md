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
3. **Database**: PostgreSQL (hoặc MySQL nếu bạn muốn thay đổi cấu hình).
   - Cài đặt PostgreSQL từ [trang chính thức](https://www.postgresql.org/download/).
   - Tạo một cơ sở dữ liệu, ví dụ: `busplatform_db`.
4. **Keycloak**: Hệ thống quản lý danh tính.
   - Tải và chạy Keycloak từ [Keycloak](https://www.keycloak.org/downloads).
   - Cấu hình một realm (ví dụ: `busplatform`) và tạo client để lấy thông tin JWT.
5. **IDE**: IntelliJ IDEA, Eclipse, hoặc VS Code (khuyến nghị IntelliJ cho Spring Boot).
6. **Docker** (tùy chọn): Nếu bạn muốn chạy Keycloak hoặc PostgreSQL trong container.
   - Cài đặt từ [Docker](https://www.docker.com/get-started).

## Cấu hình môi trường
1. **Cơ sở dữ liệu**:
   - Cập nhật file `src/main/resources/application.properties` hoặc `application.yml` với thông tin kết nối cơ sở dữ liệu:
     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/busplatform_db
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     spring.jpa.hibernate.ddl-auto=update
     ```
   - Đảm bảo PostgreSQL đang chạy.

2. **Keycloak**:
   - Chạy Keycloak (mặc định: `http://localhost:8080`).
   - Cấu hình client trong Keycloak với:
     - Client ID: `busplatform-client`
     - Protocol: `openid-connect`
     - Valid Redirect URIs: `http://localhost:8080/*`
   - Cập nhật `application.properties` với thông tin Keycloak:
     ```properties
     spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/auth/realms/busplatform
     ```

3. **Chạy ứng dụng**:
   - Mở dự án trong IDE.
   - Chạy lệnh `mvn clean install` để tải phụ thuộc.
   - Chạy ứng dụng bằng lệnh `mvn spring-boot:run` hoặc từ IDE.

4. **Kiểm tra**:
   - Truy cập `http://localhost:8080` (hoặc cổng được cấu hình) để kiểm tra API.
   - Sử dụng Postman hoặc curl để gọi các endpoint API sau khi đăng nhập qua Keycloak.

## Tổng quan về công việc đã thực hiện
Dự án đã hoàn thành các phần sau để thiết lập nền tảng cho hệ thống đặt vé xe khách:

### 1. Các Entity
Các thực thể JPA đã được thiết kế và triển khai để mô hình hóa dữ liệu của hệ thống, đặt trong gói `com.busplatform.api.model`:

- **User**:
  - Đại diện cho người dùng (nhà điều hành, hành khách, nhân viên).
  - Các trường: `id` (UUID từ Keycloak), `username`, `password`, `firstName`, `lastName`, `dateOfBirth`, `email`, `role` (enum: `PASSENGER`, `BUS_OPERATOR`, `BUS_STAFF`), `createdAt`, `updatedAt`.
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
  - Yêu cầu xác thực cho tất cả các yêu cầu HTTP.
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
   - Các entity nằm trong `com.busplatform.api.model`.
   - DTOs nằm trong `com.busplatform.api.domain`.
   - Dịch vụ trong `com.busplatform.api.service`.
   - Bộ lọc và cấu hình trong `com.busplatform.api.filters` và `com.busplatform.api.config`.

3. **Nhiệm vụ tiếp theo**:
   - **Triển khai TripService**: Viết lớp triển khai cho `TripService` để xử lý logic tạo chuyến xe, bao gồm chuyển đổi DTO sang entity và kiểm tra dữ liệu.
   - **API Endpoints**: Tạo các REST controller để xử lý yêu cầu tạo, cập nhật, và truy vấn chuyến xe/vé.
   - **QR Code Generation**: Tích hợp thư viện để tạo mã QR cho `QRCode` entity.
   - **Validation Logic**: Thêm logic kiểm tra vé qua QR code hoặc nhập tay.

4. **Phân chia công việc**:
   - Bạn có thể chọn triển khai `TripService` hoặc REST controllers.
   - Hãy liên hệ để phân công nhiệm vụ cụ thể và tránh xung đột code.

## Lưu ý
- Đảm bảo Keycloak và PostgreSQL đang chạy trước khi khởi động ứng dụng.
- Kiểm tra `application.properties` để cấu hình đúng thông tin database và Keycloak.
- Nếu gặp lỗi, kiểm tra log trong console hoặc liên hệ mình để debug.

Chào mừng các bạn tham gia dự án! Hãy thoải mái đặt câu hỏi nếu cần làm rõ bất kỳ phần nào. 😊