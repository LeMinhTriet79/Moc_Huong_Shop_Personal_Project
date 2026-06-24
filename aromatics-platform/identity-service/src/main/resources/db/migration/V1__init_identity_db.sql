-- 1. Bảng users
CREATE TABLE users (
                       id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(150) NOT NULL,
                       phone_number VARCHAR(20) NULL UNIQUE,
                       avatar_url VARCHAR(500) NULL,
                       role ENUM('CUSTOMER','STAFF','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
                       provider ENUM('LOCAL','GOOGLE') NOT NULL DEFAULT 'LOCAL',
                       provider_id VARCHAR(255) NULL,
                       is_active TINYINT(1) NOT NULL DEFAULT 1,
                       is_email_verified TINYINT(1) NOT NULL DEFAULT 0,
                       date_of_birth DATE NULL,
                       created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                       updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
);

CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_phone ON users(phone_number);
CREATE INDEX idx_role_active ON users(role, is_active);
CREATE INDEX idx_created_at ON users(created_at);

-- 2. Bảng user_addresses
CREATE TABLE user_addresses (
                                id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                user_id BIGINT UNSIGNED NOT NULL,
                                recipient_name VARCHAR(150) NOT NULL,
                                recipient_phone VARCHAR(20) NOT NULL,
                                province_code VARCHAR(10) NOT NULL,
                                province_name VARCHAR(100) NOT NULL,
                                district_name VARCHAR(100) NOT NULL,
                                ward_name VARCHAR(100) NOT NULL,
                                street_address VARCHAR(255) NOT NULL,
                                is_default TINYINT(1) NOT NULL DEFAULT 0,
                                created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_id ON user_addresses(user_id);
CREATE INDEX idx_user_default ON user_addresses(user_id, is_default);

-- 3. Bảng wishlists
CREATE TABLE wishlists (
                           id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT UNSIGNED NOT NULL,
                           product_id BIGINT UNSIGNED NOT NULL,
                           created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                           CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                           UNIQUE KEY uq_user_product (user_id, product_id)
);

CREATE INDEX idx_wishlist_user_id ON wishlists(user_id);

-- 4. Bảng refresh_tokens
CREATE TABLE refresh_tokens (
                                id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                user_id BIGINT UNSIGNED NOT NULL,
                                token_hash VARCHAR(255) NOT NULL UNIQUE,
                                device_info VARCHAR(255) NULL,
                                ip_address VARCHAR(45) NULL,
                                expires_at DATETIME(3) NOT NULL,
                                is_revoked TINYINT(1) NOT NULL DEFAULT 0,
                                created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_token_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_expires_at ON refresh_tokens(expires_at);

-- 5. Bảng email_verifications
CREATE TABLE email_verifications (
                                     id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                     user_id BIGINT UNSIGNED NOT NULL,
                                     token VARCHAR(255) NOT NULL UNIQUE,
                                     type ENUM('VERIFY_EMAIL','RESET_PASSWORD') NOT NULL,
                                     expires_at DATETIME(3) NOT NULL,
                                     is_used TINYINT(1) NOT NULL DEFAULT 0,
                                     created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                     CONSTRAINT fk_verification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_verification_token ON email_verifications(token);
CREATE INDEX idx_user_type ON email_verifications(user_id, type);
CREATE INDEX idx_verification_expires ON email_verifications(expires_at);