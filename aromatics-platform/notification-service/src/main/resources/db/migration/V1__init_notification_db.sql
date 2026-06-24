-- 1. Bảng notification_templates (Quản lý nội dung Email/SMS)
CREATE TABLE notification_templates (
                                        id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                        template_key VARCHAR(100) NOT NULL UNIQUE,
                                        channel ENUM('EMAIL','SMS') NOT NULL,
                                        event_type VARCHAR(100) NOT NULL,
                                        subject_template VARCHAR(255) NULL,
                                        body_template LONGTEXT NOT NULL,
                                        variables_schema JSON NULL,
                                        is_active TINYINT(1) NOT NULL DEFAULT 1,
                                        version INT UNSIGNED NOT NULL DEFAULT 1,
                                        description VARCHAR(500) NULL,
                                        last_modified_by BIGINT UNSIGNED NULL,
                                        created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                        updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
);
CREATE UNIQUE INDEX idx_template_key ON notification_templates(template_key);
CREATE INDEX idx_event_channel ON notification_templates(event_type, channel, is_active);
CREATE INDEX idx_updated_at ON notification_templates(updated_at);

-- 2. Bảng notification_logs (Lịch sử hệ thống thông báo gửi ra)
CREATE TABLE notification_logs (
                                   id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                   idempotency_key VARCHAR(150) NOT NULL UNIQUE,
                                   event_type VARCHAR(100) NOT NULL,
                                   channel ENUM('EMAIL','SMS','PUSH') NOT NULL,
                                   recipient VARCHAR(255) NOT NULL,
                                   user_id BIGINT UNSIGNED NULL,
                                   reference_type VARCHAR(50) NULL,
                                   reference_id BIGINT UNSIGNED NULL,
                                   subject VARCHAR(255) NULL,
                                   content_preview VARCHAR(500) NULL,
                                   status ENUM('PENDING','SENT','FAILED','SKIPPED') NOT NULL DEFAULT 'PENDING',
                                   error_message VARCHAR(500) NULL,
                                   retry_count TINYINT UNSIGNED NOT NULL DEFAULT 0,
                                   next_retry_at DATETIME(3) NULL,
                                   sent_at DATETIME(3) NULL,
                                   created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);
CREATE UNIQUE INDEX idx_idempotency_key ON notification_logs(idempotency_key);
CREATE INDEX idx_status_retry ON notification_logs(status, retry_count, next_retry_at);
CREATE INDEX idx_user_id ON notification_logs(user_id);
CREATE INDEX idx_recipient ON notification_logs(recipient);
CREATE INDEX idx_reference ON notification_logs(reference_type, reference_id);
CREATE INDEX idx_event_type ON notification_logs(event_type, created_at DESC);
CREATE INDEX idx_created_at ON notification_logs(created_at DESC);