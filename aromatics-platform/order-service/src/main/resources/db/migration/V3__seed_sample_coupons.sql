-- V3__seed_sample_coupons.sql
-- Thêm các mã giảm giá chuẩn cho Mộc Hương Shop (bao gồm TINHTAM10 khớp Banner Trang Chủ)

INSERT INTO coupons (code, name, type, value, max_discount_amount, min_order_value, max_uses, max_uses_per_user, used_count, is_active, starts_at, expires_at, created_at, updated_at)
VALUES 
('TINHTAM10', 'Ưu đãi Tĩnh Tâm - Giảm 10% đơn hàng đầu tiên', 'PERCENTAGE', 10.00, 100000.00, 200000.00, 500, 2, 12, true, NOW() - INTERVAL '30 days', NOW() + INTERVAL '365 days', NOW(), NOW()),
('MOCHUONG50', 'Ưu đãi Mộc Hương - Giảm 50.000 ₫ cho đơn từ 500.000 ₫', 'FIXED_AMOUNT', 50000.00, 50000.00, 500000.00, 300, 2, 8, true, NOW() - INTERVAL '30 days', NOW() + INTERVAL '365 days', NOW(), NOW()),
('TRAMHUONG10', 'Tri ân khách hàng Trầm Hương - Giảm 10% tối đa 150.000 ₫', 'PERCENTAGE', 10.00, 150000.00, 1000000.00, 200, 1, 5, true, NOW() - INTERVAL '30 days', NOW() + INTERVAL '365 days', NOW(), NOW()),
('FREESHIP0D', 'Miễn phí vận chuyển toàn quốc cho đơn từ 300.000 ₫', 'FREE_SHIPPING', 30000.00, 30000.00, 300000.00, 1000, 3, 25, true, NOW() - INTERVAL '30 days', NOW() + INTERVAL '365 days', NOW(), NOW()),
('VIP300K', 'Đặc quyền thành viên VIP - Giảm 300.000 ₫ cho đơn từ 3.000.000 ₫', 'FIXED_AMOUNT', 300000.00, 300000.00, 3000000.00, 100, 1, 2, true, NOW() - INTERVAL '30 days', NOW() + INTERVAL '365 days', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;
