-- 1. Bảng categories (Danh mục)
CREATE TABLE categories (
                            id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                            parent_id BIGINT UNSIGNED NULL,
                            name VARCHAR(150) NOT NULL,
                            slug VARCHAR(150) NOT NULL UNIQUE,
                            image_url VARCHAR(500) NULL,
                            description TEXT NULL,
                            sort_order INT NOT NULL DEFAULT 0,
                            is_active TINYINT(1) NOT NULL DEFAULT 1,
                            created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                            updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                            CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);
CREATE INDEX idx_parent_id ON categories(parent_id);
CREATE INDEX idx_slug ON categories(slug);
CREATE INDEX idx_sort_order ON categories(is_active, sort_order);

-- 2. Bảng products (Sản phẩm)
CREATE TABLE products (
                          id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                          category_id BIGINT UNSIGNED NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          slug VARCHAR(255) NOT NULL UNIQUE,
                          short_description VARCHAR(500) NULL,
                          description LONGTEXT NULL,
                          brand VARCHAR(100) NULL,
                          tags JSON NULL,
                          average_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
                          total_reviews INT UNSIGNED NOT NULL DEFAULT 0,
                          total_sold INT UNSIGNED NOT NULL DEFAULT 0,
                          is_published TINYINT(1) NOT NULL DEFAULT 0,
                          created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                          updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                          CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);
CREATE INDEX idx_category_published ON products(category_id, is_published);
CREATE INDEX idx_name ON products(name);
CREATE INDEX idx_product_slug ON products(slug);
CREATE INDEX idx_rating ON products(average_rating DESC);
CREATE INDEX idx_sold ON products(total_sold DESC);

-- 3. Bảng product_variants (Biến thể sản phẩm: Khối lượng, Mùi hương...)
CREATE TABLE product_variants (
                                  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                  product_id BIGINT UNSIGNED NOT NULL,
                                  sku VARCHAR(100) NOT NULL UNIQUE,
                                  variant_name VARCHAR(150) NOT NULL,
                                  price DECIMAL(15,2) NOT NULL,
                                  original_price DECIMAL(15,2) NULL,
                                  weight_gram INT UNSIGNED NULL,
                                  attributes JSON NULL,
                                  is_active TINYINT(1) NOT NULL DEFAULT 1,
                                  sort_order INT NOT NULL DEFAULT 0,
                                  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                  CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
CREATE INDEX idx_variant_product_id ON product_variants(product_id);
CREATE INDEX idx_sku ON product_variants(sku);

-- 4. Bảng product_images (Thư viện ảnh gallery)
CREATE TABLE product_images (
                                id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                product_id BIGINT UNSIGNED NOT NULL,
                                cloudinary_public_id VARCHAR(255) NOT NULL,
                                image_url VARCHAR(500) NOT NULL,
                                thumbnail_url VARCHAR(500) NULL,
                                is_primary TINYINT(1) NOT NULL DEFAULT 0,
                                sort_order INT NOT NULL DEFAULT 0,
                                alt_text VARCHAR(255) NULL,
                                created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
CREATE INDEX idx_image_product_id ON product_images(product_id);
CREATE INDEX idx_product_primary ON product_images(product_id, is_primary);

-- 5. Bảng inventory (Tồn kho - Nơi diễn ra Khóa phân tán Redisson Lock)
CREATE TABLE inventory (
                           id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                           variant_id BIGINT UNSIGNED NOT NULL UNIQUE,
                           quantity INT NOT NULL DEFAULT 0,
                           reserved_quantity INT NOT NULL DEFAULT 0,
                           low_stock_threshold INT NOT NULL DEFAULT 10,
                           updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                           CONSTRAINT fk_inventory_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
);
CREATE INDEX idx_inventory_variant_id ON inventory(variant_id);
CREATE INDEX idx_low_stock ON inventory(quantity, low_stock_threshold);

-- 6. Bảng reviews (Đánh giá của khách hàng)
CREATE TABLE reviews (
                         id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         product_id BIGINT UNSIGNED NOT NULL,
                         user_id BIGINT UNSIGNED NOT NULL,
                         order_item_id BIGINT UNSIGNED NOT NULL UNIQUE,
                         variant_id BIGINT UNSIGNED NOT NULL,
                         rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                         title VARCHAR(200) NULL,
                         content TEXT NULL,
                         images JSON NULL,
                         admin_reply TEXT NULL,
                         admin_replied_at DATETIME(3) NULL,
                         is_visible TINYINT(1) NOT NULL DEFAULT 1,
                         created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                         CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                         CONSTRAINT fk_review_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
);
CREATE INDEX idx_review_product_id ON reviews(product_id);
CREATE INDEX idx_review_rating ON reviews(product_id, rating);