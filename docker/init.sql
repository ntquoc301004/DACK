-- Chạy một lần khi volume MySQL được khởi tạo lần đầu (thư mục /docker-entrypoint-initdb.d).
-- Database db_j2ee_dack đã được tạo bởi MYSQL_DATABASE trong docker-compose.

USE db_j2ee_dack;

-- Ví dụ: bảng / dữ liệu mẫu (bỏ comment hoặc sửa theo schema thật của bạn)
-- CREATE TABLE IF NOT EXISTS example (
--   id BIGINT PRIMARY KEY AUTO_INCREMENT,
--   name VARCHAR(255) NOT NULL
-- );
