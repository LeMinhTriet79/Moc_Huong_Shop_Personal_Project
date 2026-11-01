-- Cho phép khách hàng đánh giá sản phẩm trực tiếp trên trang chi tiết (order_item_id có thể NULL)
-- Trong MySQL InnoDB, UNIQUE index trên cột NULLABLE vẫn cho phép nhiều dòng có giá trị NULL
-- nhưng chặn trùng lặp khi order_item_id khác NULL.
ALTER TABLE reviews
    MODIFY COLUMN order_item_id BIGINT UNSIGNED NULL;
