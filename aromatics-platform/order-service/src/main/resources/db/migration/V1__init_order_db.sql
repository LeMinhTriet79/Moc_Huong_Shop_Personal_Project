-- 1. Bảng coupons (Mã giảm giá)
CREATE TABLE coupons (
                         id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         code VARCHAR(50) NOT NULL UNIQUE,
                         name VARCHAR(150) NOT NULL,
                         type ENUM('PERCENTAGE','FIXED_AMOUNT','FREE_SHIPPING') NOT NULL,
                         value DECIMAL(15,2) NOT NULL,
                         max_discount_amount DECIMAL(15,2) NULL,
                         min_order_value DECIMAL(15,2) NOT NULL DEFAULT 0,
                         max_uses INT NULL,
                         max_uses_per_user INT NOT NULL DEFAULT 1,
                         used_count INT UNSIGNED NOT NULL DEFAULT 0,
                         applicable_product_ids JSON NULL,
                         applicable_category_ids JSON NULL,
                         starts_at DATETIME(3) NOT NULL,
                         expires_at DATETIME(3) NULL,
                         is_active TINYINT(1) NOT NULL DEFAULT 1,
                         created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);
CREATE UNIQUE INDEX idx_code ON coupons(code);
CREATE INDEX idx_active_dates ON coupons(is_active, starts_at, expires_at);
CREATE INDEX idx_used_count ON coupons(used_count);

-- 2. Bảng orders (Đơn hàng)
CREATE TABLE orders (
                        id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                        order_code VARCHAR(30) NOT NULL UNIQUE,
                        user_id BIGINT UNSIGNED NOT NULL,
                        status ENUM('PENDING_PAYMENT','CONFIRMED','PREPARING','SHIPPING','DELIVERED','COMPLETED','CANCELLED','RETURN_REQUESTED','RETURNED') NOT NULL DEFAULT 'PENDING_PAYMENT',
                        shipping_address JSON NOT NULL,
                        shipping_fee DECIMAL(15,2) NOT NULL DEFAULT 0,
                        subtotal DECIMAL(15,2) NOT NULL,
                        discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
                        total_amount DECIMAL(15,2) NOT NULL,
                        coupon_code VARCHAR(50) NULL,
                        payment_method ENUM('VNPAY','MOMO','COD') NOT NULL,
                        payment_status ENUM('UNPAID','PAID','REFUNDING','REFUNDED','FAILED') NOT NULL DEFAULT 'UNPAID',
                        idempotency_key VARCHAR(100) NOT NULL UNIQUE,
                        notes TEXT NULL,
                        staff_notes TEXT NULL,
                        cancelled_reason VARCHAR(500) NULL,
                        cancelled_by ENUM('CUSTOMER','STAFF','ADMIN','SYSTEM') NULL,
                        confirmed_at DATETIME(3) NULL,
                        delivered_at DATETIME(3) NULL,
                        created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                        updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
);
CREATE UNIQUE INDEX idx_order_code ON orders(order_code);
CREATE UNIQUE INDEX idx_idempotency_key ON orders(idempotency_key);
CREATE INDEX idx_user_id ON orders(user_id);
CREATE INDEX idx_status ON orders(status);
CREATE INDEX idx_user_status ON orders(user_id, status);
CREATE INDEX idx_payment_status ON orders(payment_status);
CREATE INDEX idx_created_at ON orders(created_at DESC);
CREATE INDEX idx_delivered_at ON orders(delivered_at);

-- 3. Bảng coupon_usages (Lịch sử sử dụng mã giảm giá)
CREATE TABLE coupon_usages (
                               id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               coupon_id BIGINT UNSIGNED NOT NULL,
                               user_id BIGINT UNSIGNED NOT NULL,
                               order_id BIGINT UNSIGNED NOT NULL,
                               discount_amount DECIMAL(15,2) NOT NULL,
                               is_refunded TINYINT(1) NOT NULL DEFAULT 0,
                               used_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                               CONSTRAINT fk_usage_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE RESTRICT,
                               CONSTRAINT fk_usage_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT
);
CREATE INDEX idx_coupon_id ON coupon_usages(coupon_id);
CREATE INDEX idx_user_coupon ON coupon_usages(user_id, coupon_id);
CREATE INDEX idx_order_id ON coupon_usages(order_id);

-- 4. Bảng order_items (Chi tiết sản phẩm trong đơn)
CREATE TABLE order_items (
                             id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                             order_id BIGINT UNSIGNED NOT NULL,
                             product_id BIGINT UNSIGNED NOT NULL,
                             variant_id BIGINT UNSIGNED NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             variant_name VARCHAR(150) NOT NULL,
                             product_image_url VARCHAR(500) NULL,
                             sku VARCHAR(100) NOT NULL,
                             unit_price DECIMAL(15,2) NOT NULL,
                             quantity INT UNSIGNED NOT NULL,
                             subtotal DECIMAL(15,2) NOT NULL,
                             review_id BIGINT UNSIGNED NULL,
                             CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
CREATE INDEX idx_item_order_id ON order_items(order_id);
CREATE INDEX idx_item_product_id ON order_items(product_id);
CREATE INDEX idx_item_variant_id ON order_items(variant_id);
CREATE INDEX idx_review_id ON order_items(review_id);

-- 5. Bảng transactions (Lịch sử giao dịch thanh toán)
CREATE TABLE transactions (
                              id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                              order_id BIGINT UNSIGNED NOT NULL,
                              transaction_code VARCHAR(100) NOT NULL UNIQUE,
                              gateway_transaction_id VARCHAR(255) NULL,
                              payment_method ENUM('VNPAY','MOMO','COD') NOT NULL,
                              type ENUM('PAYMENT','REFUND') NOT NULL DEFAULT 'PAYMENT',
                              amount DECIMAL(15,2) NOT NULL,
                              status ENUM('PENDING','SUCCESS','FAILED','CANCELLED','REFUNDING','REFUNDED') NOT NULL DEFAULT 'PENDING',
                              gateway_response_code VARCHAR(20) NULL,
                              gateway_response_message VARCHAR(255) NULL,
                              raw_response JSON NULL,
                              ip_address VARCHAR(45) NULL,
                              created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                              completed_at DATETIME(3) NULL,
                              CONSTRAINT fk_transaction_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT
);
CREATE UNIQUE INDEX idx_transaction_code ON transactions(transaction_code);
CREATE INDEX idx_transaction_order_id ON transactions(order_id);
CREATE INDEX idx_gateway_transaction_id ON transactions(gateway_transaction_id);
CREATE INDEX idx_status_created ON transactions(status, created_at DESC);
CREATE INDEX idx_method_status ON transactions(payment_method, status);

-- 6. Bảng order_status_history (Lịch sử thay đổi trạng thái đơn)
CREATE TABLE order_status_history (
                                      id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                      order_id BIGINT UNSIGNED NOT NULL,
                                      from_status ENUM('PENDING_PAYMENT','CONFIRMED','PREPARING','SHIPPING','DELIVERED','COMPLETED','CANCELLED','RETURN_REQUESTED','RETURNED') NULL,
                                      to_status ENUM('PENDING_PAYMENT','CONFIRMED','PREPARING','SHIPPING','DELIVERED','COMPLETED','CANCELLED','RETURN_REQUESTED','RETURNED') NOT NULL,
                                      changed_by_type ENUM('CUSTOMER','STAFF','ADMIN','SYSTEM') NOT NULL,
                                      changed_by_id BIGINT UNSIGNED NULL,
                                      note VARCHAR(500) NULL,
                                      created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                      CONSTRAINT fk_history_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
CREATE INDEX idx_history_order_id ON order_status_history(order_id);
CREATE INDEX idx_order_created ON order_status_history(order_id, created_at ASC);
CREATE INDEX idx_to_status ON order_status_history(to_status, created_at);