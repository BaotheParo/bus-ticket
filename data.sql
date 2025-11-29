DELETE FROM ticket_validations;
DELETE FROM qr_codes;
DELETE FROM user_staffing_trips;
DELETE FROM user_booked_trips; -- Thêm bảng này nếu bạn đã tạo
DELETE FROM token_storage;
DELETE FROM tickets;
DELETE FROM decks;
DELETE FROM trips;
DELETE FROM users;
DELETE FROM bus_types;

-- -----------------------------------------------------------------
-- BƯỚC 1: ĐỒNG BỘ CẤU TRÚC DATABASE (Thêm cột nếu thiếu)
-- -----------------------------------------------------------------
-- Thêm cột cho bảng trips nếu chưa có
ALTER TABLE trips
ADD COLUMN IF NOT EXISTS sale_start TIMESTAMP WITHOUT TIME ZONE NULL, -- Cho phép NULL ban đầu
ADD COLUMN IF NOT EXISTS sale_end TIMESTAMP WITHOUT TIME ZONE NULL;   -- Cho phép NULL ban đầu

-- Thêm cột cho bảng users nếu chưa có, cho phép NULL ban đầu để tránh lỗi
ALTER TABLE users
ADD COLUMN IF NOT EXISTS roles VARCHAR(255) NULL,
ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true,
ADD COLUMN IF NOT EXISTS managed_by_operator_id UUID NULL;

ALTER TABLE ticket_validations
    DROP CONSTRAINT ticket_validations_status_check;

-- 2. Thêm constraint mới (nhớ liệt kê TẤT CẢ các trạng thái bạn cần, ví dụ dưới đây)
ALTER TABLE ticket_validations
    ADD CONSTRAINT ticket_validations_status_check
        CHECK (status IN ('VALID', 'INVALID'));
-- Hãy thay đổi list trên cho đúng với Enum trong Java của bạn

-- Thêm bảng token_storage nếu chưa có (Hibernate có thể tự tạo, nhưng thêm cho chắc)
CREATE TABLE IF NOT EXISTS token_storage (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    refresh_token VARCHAR(1000) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- =================================================================
-- BẢNG BUS_TYPES (Dữ liệu bạn đã cung cấp)
-- =================================================================
INSERT INTO bus_types (id, created_at, description, is_default, "name", num_decks, price_factor, seats_per_deck, updated_at) VALUES
('96ebadd3-64d6-4bcf-a966-38381e6ef642',	NOW(), 'Xe buýt ghế tiêu chuẩn',	true,	'Xe ghế ngồi',	1,	1,	45,	NOW()),
('a3d1eb31-bbbc-4a0d-bb90-81bf4d660444',	NOW(), 'Xe buýt giường nằm 2 tầng',	true,	'Xe giường nằm',	2,	1.2,	17,	NOW()),
('d6b7cb3e-b51d-4471-b896-5cf334cc434f',	NOW(), 'Fallback khi xóa BusType',	true,	'Không xác định',	1,	1,	0,	NOW());

-- =================================================================
-- BẢNG USERS (Cập nhật Password Hash và Roles)
-- !!! QUAN TRỌNG: Thay thế 'HASHED_PASSWORD_PLACEHOLDER' BẰNG HASH THỰC TẾ !!!
-- (Ví dụ hash BCrypt của 'password' là '$2a$10$9SEWq.Qk78XkS2wP3R4iL.b6z.q3c6t8j9K/L.mN0p1qR2sT3uW4.')
-- =================================================================
INSERT INTO users (id, created_at, date_of_birth, email, firstname, lastname, "password", updated_at, username, managed_by_operator_id, roles, is_active) VALUES
-- --- THÊM MỚI: ADMIN ---
('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', NOW(), '1980-01-01', 'admin@busticket.com', 'Super', 'Admin', '$2a$10$BskVFJsA2U3gfVCEd8s/rOwjX77NoQdi/gMI3sCczBWuxhoNGGG2y', NOW(), 'admin', NULL, 'ROLE_ADMIN', true),
-- Operators
('8a8f8e8a-8e8e-8e8e-8e8e-8e8e8e8e8e8a',	NOW(), '1990-01-15', 'operator.phuongtrang@example.com', 'Phương', 'Trang', '$2a$10$BskVFJsA2U3gfVCEd8s/rOwjX77NoQdi/gMI3sCczBWuxhoNGGG2y', NOW(), 'operator_phuongtrang', NULL, 'ROLE_OPERATOR', true),
('8b8f8e8a-8e8e-8e8e-8e8e-8e8e8e8e8e8b',	NOW(), '1985-05-20', 'operator.thanhbuoi@example.com',   'Thành',  'Bưởi',  '$2a$10$BskVFJsA2U3gfVCEd8s/rOwjX77NoQdi/gMI3sCczBWuxhoNGGG2y', NOW(), 'operator_thanhbuoi',  NULL, 'ROLE_OPERATOR', true),
-- Staff (Cập nhật có managed_by_operator_id)
('9a9f9e9a-9e9e-9e9e-9e9e-9e9e9e9e9e9a',	NOW(), '1995-03-10', 'staff.vana@example.com',           'Văn',    'A',     '$2a$10$BskVFJsA2U3gfVCEd8s/rOwjX77NoQdi/gMI3sCczBWuxhoNGGG2y', NOW(), 'staff_van_a',         '8a8f8e8a-8e8e-8e8e-8e8e-8e8e8e8e8e8a', 'ROLE_STAFF', true),
('9b9f9e9a-9e9e-9e9e-9e9e-9e9e9e9e9e9b',	NOW(), '1998-11-25', 'staff.thib@example.com',           'Thị',    'B',     '$2a$10$BskVFJsA2U3gfVCEd8s/rOwjX77NoQdi/gMI3sCczBWuxhoNGGG2y', NOW(), 'staff_thi_b',         '8a8f8e8a-8e8e-8e8e-8e8e-8e8e8e8e8e8a', 'ROLE_STAFF', true),
('9c9f9e9a-9e9e-9e9e-9e9e-9e9e9e9e9e9c',	NOW(), '1992-07-01', 'staff.vanc@example.com',           'Văn',    'C',     '$2a$10$BskVFJsA2U3gfVCEd8s/rOwjX77NoQdi/gMI3sCczBWuxhoNGGG2y', NOW(), 'staff_van_c',         '8b8f8e8a-8e8e-8e8e-8e8e-8e8e8e8e8e8b', 'ROLE_STAFF', true),
-- Passengers
('1a1f1e1a-1e1e-1e1e-1e1e-1e1e1e1e1e1a',	NOW(), '2000-08-08', 'passenger.minh@example.com',       'Thanh',  'Minh',  '$2a$10$BskVFJsA2U3gfVCEd8s/rOwjX77NoQdi/gMI3sCczBWuxhoNGGG2y', NOW(), 'passenger_minh',      NULL, 'ROLE_PASSENGER', true),
('1b1f1e1a-1e1e-1e1e-1e1e-1e1e1e1e1e1b',	NOW(), '2001-09-09', 'passenger.hoa@example.com',        'Lan',    'Hoa',   '$2a$10$BskVFJsA2U3gfVCEd8s/rOwjX77NoQdi/gMI3sCczBWuxhoNGGG2y', NOW(), 'passenger_hoa',       NULL, 'ROLE_PASSENGER', true);

-- Cập nhật roles cho các user hiện có (để đảm bảo không null nếu ALTER TABLE chạy sau INSERT)
UPDATE users SET roles = 'ROLE_OPERATOR' WHERE id IN ('8a8f8e8a-8e8e-8e8e-8e8e-8e8e8e8e8e8a', '8b8f8e8a-8e8e-8e8e-8e8e-8e8e8e8e8e8b');
UPDATE users SET roles = 'ROLE_STAFF' WHERE id IN ('9a9f9e9a-9e9e-9e9e-9e9e-9e9e9e9e9e9a', '9b9f9e9a-9e9e-9e9e-9e9e-9e9e9e9e9e9b', '9c9f9e9a-9e9e-9e9e-9e9e-9e9e9e9e9e9c');
UPDATE users SET roles = 'ROLE_PASSENGER' WHERE id IN ('1a1f1e1a-1e1e-1e1e-1e1e-1e1e1e1e1e1a', '1b1f1e1a-1e1e-1e1e-1e1e-1e1e1e1e1e1b');

-- Thêm ràng buộc NOT NULL cho roles sau khi đã cập nhật dữ liệu
ALTER TABLE users ALTER COLUMN roles SET NOT NULL;

-- =================================================================
-- BẢNG TRIPS
-- =================================================================
INSERT INTO trips (id, route_name, departure_time, departure_point, arrival_time, destination, duration_minutes, bus_type_id, status, base_price, operator_id, created_at, updated_at, sales_start, sales_end) VALUES
('7a7f7e7a-7e7e-7e7e-7e7e-7e7e7e7e7e7a', 'Sài Gòn - Đà Lạt', '2025-11-20 22:00:00', 'Bến xe Miền Đông', '2025-11-21 06:00:00', 'Bến xe Liên tỉnh Đà Lạt', 480, '67a13024-9af5-4de8-89c5-b86a6ee17fbc', 'PUBLISHED', 350000, '8a8f8e8a-8e8e-8e8e-8e8e-8e8e8e8e8e8a', NOW(), NOW(), '2025-10-01 00:00:00', '2025-11-20 21:00:00'),
('7b7f7e7a-7e7e-7e7e-7e7e-7e7e7e7e7e7b', 'Sài Gòn - Vũng Tàu', '2025-11-15 09:30:00', '266-268 Lê Hồng Phong', '2025-11-15 11:30:00', 'Bến xe Vũng Tàu', 120, '3ed77239-fd25-47c5-9039-42300157ca51', 'PUBLISHED', 180000, '8b8f8e8a-8e8e-8e8e-8e8e-8e8e8e8e8e8b', NOW(), NOW(), '2025-10-01 00:00:00', '2025-11-15 08:30:00');

-- =================================================================
-- BẢNG DECKS (Sử dụng UUID hợp lệ)
-- =================================================================
INSERT INTO decks (id, label, price_factor, total_seats, bus_type_id, trip_id) VALUES
('4f1d2c9a-6b3a-4e8c-9c1d-0a2b3e4f5a6b', 'A', 1.0, 17, NULL, '7a7f7e7a-7e7e-7e7e-7e7e-7e7e7e7e7e7a'),
('5f1d2c9a-6b3a-4e8c-9c1d-0a2b3e4f5a6c', 'B', 1.1, 17, NULL, '7a7f7e7a-7e7e-7e7e-7e7e-7e7e7e7e7e7a'),
('6f1d2c9a-6b3a-4e8c-9c1d-0a2b3e4f5a6d', 'A', 1.0, 45, NULL, '7b7f7e7a-7e7e-7e7e-7e7e-7e7e7e7e7e7b');

-- =================================================================
-- BẢNG TICKETS (Sử dụng UUID hợp lệ)
-- =================================================================
INSERT INTO tickets (id, status, selected_seat, price, deck_id, purchaser_id, created_at, updated_at) VALUES
('1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d', 'PURCHASED', 'A5', 350000.0, '4f1d2c9a-6b3a-4e8c-9c1d-0a2b3e4f5a6b', '1a1f1e1a-1e1e-1e1e-1e1e-1e1e1e1e1e1a', NOW(), NOW()),
('2a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6e', 'PURCHASED', 'B10', 385000.0, '5f1d2c9a-6b3a-4e8c-9c1d-0a2b3e4f5a6c', '1a1f1e1a-1e1e-1e1e-1e1e-1e1e1e1e1e1a', NOW(), NOW()),
('3a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6f', 'PURCHASED', 'A22', 180000.0, '6f1d2c9a-6b3a-4e8c-9c1d-0a2b3e4f5a6d', '1b1f1e1a-1e1e-1e1e-1e1e-1e1e1e1e1e1b', NOW(), NOW());

-- =================================================================
-- BẢNG PHÂN CÔNG NHÂN VIÊN (user_staffing_trips)
-- =================================================================
INSERT INTO user_staffing_trips(user_id, trip_id) VALUES
('9a9f9e9a-9e9e-9e9e-9e9e-9e9e9e9e9e9a', '7a7f7e7a-7e7e-7e7e-7e7e-7e7e7e7e7e7a'), -- Staff A vào Trip 1
('9b9f9e9a-9e9e-9e9e-9e9e-9e9e9e9e9e9b', '7a7f7e7a-7e7e-7e7e-7e7e-7e7e7e7e7e7a'), -- Staff B vào Trip 1
('9c9f9e9a-9e9e-9e9e-9e9e-9e9e9e9e9e9c', '7b7f7e7a-7e7e-7e7e-7e7e-7e7e7e7e7e7b'); -- Staff C vào Trip 2

-- =================================================================
-- BẢNG QR_CODES (Sử dụng UUID hợp lệ, value là ticketId-seat)
-- =================================================================
INSERT INTO qr_codes (id, status, value, ticket_id, created_at, updated_at, "version") VALUES
('8a8a8a8a-8a8a-8a8a-8a8a-8a8a8a8a8a8a', 'ACTIVE', '1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d-A5', '1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d', NOW(), NOW(), 0), -- QR cho ticket 1 (version nên bắt đầu từ 0)
('8b8b8b8b-8b8b-8b8b-8b8b-8b8b8b8b8b8b', 'ACTIVE', '2a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6e-B10', '2a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6e', NOW(), NOW(), 0), -- QR cho ticket 2
('8c8c8c8c-8c8c-8c8c-8c8c-8c8c8c8c8c8c', 'ACTIVE', '3a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6f-A22', '3a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6f', NOW(), NOW(), 0); -- QR cho ticket 3

-- =================================================================
-- BẢNG TICKET_VALIDATIONS (Sử dụng UUID hợp lệ)
-- =================================================================
INSERT INTO ticket_validations (id, status, validation_method, validation_time, updated_at, ticket_id) VALUES
('9a9a9a9a-9a9a-9a9a-9a9a-9a9a9a9a9a9a', 'PURCHASED', 'QR_SCAN', '2025-11-15 09:25:00', NOW(), '3a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6f');
