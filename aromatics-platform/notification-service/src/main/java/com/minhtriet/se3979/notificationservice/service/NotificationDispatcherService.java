package com.minhtriet.se3979.notificationservice.service;

import com.minhtriet.se3979.notificationservice.entity.NotificationLog;
import com.minhtriet.se3979.notificationservice.entity.NotificationTemplate;
import com.minhtriet.se3979.notificationservice.repository.NotificationLogRepository;
import com.minhtriet.se3979.notificationservice.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcherService {

    private final NotificationLogRepository logRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailService emailService;

    @Value("${app.mail.fallback-recipient:trietleminh7979@gmail.com}")
    private String fallbackRecipient;

    @Transactional
    public void dispatchEmail(
            String idempotencyKey,
            String templateKey,
            String recipient,
            Long userId,
            String referenceType,
            Long referenceId,
            Map<String, Object> variables) {

        // 1. Kiểm tra chống gửi trùng (Idempotency)
        Optional<NotificationLog> existingOpt = logRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOpt.isPresent()) {
            NotificationLog existing = existingOpt.get();
            if ("SENT".equalsIgnoreCase(existing.getStatus())) {
                log.info("⏩ IdempotencyKey '{}' đã được gửi thành công trước đó. Bỏ qua (SKIPPED)", idempotencyKey);
                return;
            }
        }

        String targetRecipient = (recipient != null && !recipient.isBlank()) ? recipient : fallbackRecipient;

        // 2. Tìm Template tương ứng
        Optional<NotificationTemplate> templateOpt = templateRepository.findByTemplateKeyAndIsActiveTrue(templateKey);

        String subject = "[Mộc Hương Shop] Thông báo";
        String body = "<p>Nội dung thông báo từ Mộc Hương Shop.</p>";

        if (templateOpt.isPresent()) {
            NotificationTemplate template = templateOpt.get();
            subject = substituteVariables(template.getSubjectTemplate(), variables);
            body = substituteVariables(template.getBodyTemplate(), variables);
        } else {
            log.warn("Không tìm thấy template '{}', sử dụng nội dung mặc định", templateKey);
        }

        String contentPreview = body.length() > 200 ? body.substring(0, 200) + "..." : body;

        NotificationLog nLog;
        if (existingOpt.isPresent()) {
            nLog = existingOpt.get();
        } else {
            nLog = NotificationLog.builder()
                    .idempotencyKey(idempotencyKey)
                    .eventType(templateOpt.map(NotificationTemplate::getEventType).orElse("general"))
                    .channel("EMAIL")
                    .recipient(targetRecipient)
                    .userId(userId)
                    .referenceType(referenceType)
                    .referenceId(referenceId)
                    .subject(subject)
                    .contentPreview(contentPreview)
                    .status("PENDING")
                    .retryCount(0)
                    .build();
        }

        nLog.setRecipient(targetRecipient);
        nLog.setSubject(subject);
        nLog.setContentPreview(contentPreview);

        try {
            // 3. Tiến hành gửi mail
            emailService.sendHtmlEmail(targetRecipient, subject, body);

            // 4. Cập nhật Log thành công
            nLog.setStatus("SENT");
            nLog.setSentAt(LocalDateTime.now());
            nLog.setErrorMessage(null);
            logRepository.save(nLog);

            log.info("✅ Gửi email '{}' thành công tới {}", templateKey, targetRecipient);

        } catch (Exception e) {
            // 5. Cập nhật Log thất bại & đặt lịch retry
            nLog.setStatus("FAILED");
            nLog.setErrorMessage(e.getMessage() != null && e.getMessage().length() > 500
                    ? e.getMessage().substring(0, 500) : e.getMessage());
            nLog.setRetryCount(nLog.getRetryCount() + 1);
            nLog.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
            logRepository.save(nLog);

            log.error("❌ Gửi email '{}' thất bại (Lần {}): {}", templateKey, nLog.getRetryCount(), e.getMessage());
        }
    }

    // Tự động quét và retry các thông báo gửi thất bại
    @Scheduled(fixedDelay = 60000) // 1 phút chạy 1 lần
    public void retryFailedNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<NotificationLog> failedLogs = logRepository
                .findByStatusAndRetryCountLessThanAndNextRetryAtLessThanEqual("FAILED", 3, now);

        if (failedLogs.isEmpty()) return;

        log.info("🔄 Đang thử gửi lại {} thông báo bị lỗi...", failedLogs.size());

        for (NotificationLog nLog : failedLogs) {
            try {
                emailService.sendHtmlEmail(nLog.getRecipient(), nLog.getSubject(), nLog.getContentPreview());
                nLog.setStatus("SENT");
                nLog.setSentAt(LocalDateTime.now());
                nLog.setErrorMessage(null);
                logRepository.save(nLog);
                log.info("✅ Thử lại thành công thông báo: {}", nLog.getIdempotencyKey());
            } catch (Exception e) {
                nLog.setRetryCount(nLog.getRetryCount() + 1);
                nLog.setErrorMessage(e.getMessage() != null && e.getMessage().length() > 500
                        ? e.getMessage().substring(0, 500) : e.getMessage());
                nLog.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
                logRepository.save(nLog);
                log.error("❌ Thử lại thất bại thông báo: {}", nLog.getIdempotencyKey());
            }
        }
    }

    private String substituteVariables(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
