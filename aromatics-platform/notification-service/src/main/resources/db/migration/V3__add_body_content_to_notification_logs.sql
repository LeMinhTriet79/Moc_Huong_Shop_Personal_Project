-- Lưu toàn văn nội dung HTML đã render để khi SMTP lỗi và chạy @Scheduled retry sẽ gửi đúng HTML đầy đủ
ALTER TABLE notification_logs
    ADD COLUMN body_content LONGTEXT NULL AFTER content_preview;
