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

    public void sendInvoiceEmail(String toEmail, String orderCode, Double totalAmount) {
        try {
            // 1. Tạo Context để truyền biến vào file HTML Thymeleaf
            Context context = new Context();
            context.setVariable("orderCode", orderCode);
            context.setVariable("totalAmount", totalAmount);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            context.setVariable("orderDate", sdf.format(new Date()));

            // 2. Compile file "invoice-email.html" thành chuỗi HTML chuẩn
            String htmlContent = templateEngine.process("invoice-email", context);

            // 3. Chuẩn bị email gửi đi
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail); // Người nhận
            helper.setSubject("🎉 [Mộc Hương Shop] Xác nhận đơn hàng " + orderCode);
            helper.setText(htmlContent, true); // true = Bật chế độ HTML

            // 4. Bấm nút Gửi
            mailSender.send(message);
            log.info("📩 Đã gửi Email Hóa Đơn thành công đến hòm thư: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi Email: {}", e.getMessage());
        }
    }
}