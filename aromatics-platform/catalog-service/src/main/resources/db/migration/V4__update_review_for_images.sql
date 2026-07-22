-- 1. Xóa cột images kiểu JSON cũ do thiết kế không tối ưu cho Cloudinary
ALTER TABLE reviews DROP COLUMN images;

-- 2. Tạo bảng review_images để lưu độc lập (Giống product_images)
CREATE TABLE review_images (
                               id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               review_id BIGINT UNSIGNED NOT NULL,
                               cloudinary_public_id VARCHAR(255) NOT NULL,
                               image_url VARCHAR(500) NOT NULL,
                               created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                               CONSTRAINT fk_review_image FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

CREATE INDEX idx_image_review_id ON review_images(review_id);