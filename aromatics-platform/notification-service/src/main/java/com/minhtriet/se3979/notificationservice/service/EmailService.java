package com.minhtriet.se3979.notificationservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    // Gửi email HTML trực tiếp với tiêu đề và nội dung tùy biến
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("📩 [Mộc Hương Shop] Đã gửi email thành công tới: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email tới {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage(), e);
        }
    }

    // Gửi email hóa đơn qua Thymeleaf template
    public void sendInvoiceEmail(String toEmail, String orderCode, Double totalAmount) {
        try {
            Context context = new Context();
            context.setVariable("orderCode", orderCode);
            context.setVariable("totalAmount", totalAmount);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            context.setVariable("orderDate", sdf.format(new Date()));

            String htmlContent = templateEngine.process("invoice-email", context);
            sendHtmlEmail(toEmail, "🎉 [Mộc Hương Shop] Xác nhận đơn hàng " + orderCode, htmlContent);

        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email hóa đơn: {}", e.getMessage());
        }
    }
}