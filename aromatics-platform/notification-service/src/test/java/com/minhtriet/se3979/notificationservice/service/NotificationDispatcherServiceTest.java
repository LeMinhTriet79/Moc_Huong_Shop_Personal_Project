package com.minhtriet.se3979.notificationservice.service;

import com.minhtriet.se3979.notificationservice.entity.NotificationLog;
import com.minhtriet.se3979.notificationservice.entity.NotificationTemplate;
import com.minhtriet.se3979.notificationservice.repository.NotificationLogRepository;
import com.minhtriet.se3979.notificationservice.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherServiceTest {

    @Mock
    private NotificationLogRepository logRepository;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationDispatcherService dispatcherService;

    private NotificationTemplate sampleTemplate;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dispatcherService, "fallbackRecipient", "fallback@mochuong.vn");

        sampleTemplate = NotificationTemplate.builder()
                .id(1L)
                .templateKey("ORDER_CREATED_EMAIL")
                .channel("EMAIL")
                .description("Xác nhận đơn hàng")
                .eventType("order.created")
                .subjectTemplate("Mộc Hương - Đơn hàng {{orderCode}} đã tạo thành công")
                .bodyTemplate("<h3>Xin chào {{fullName}}</h3><p>Đơn hàng {{orderCode}} của bạn trị giá {{totalAmount}}đ.</p>")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Gửi email thông báo thành công với template biến số")
    void testDispatchEmail_Success() {
        when(logRepository.findByIdempotencyKey("KEY-123")).thenReturn(Optional.empty());
        when(templateRepository.findByTemplateKeyAndIsActiveTrue("ORDER_CREATED_EMAIL"))
                .thenReturn(Optional.of(sampleTemplate));

        Map<String, Object> vars = Map.of(
                "fullName", "Lê Minh Triết",
                "orderCode", "MH999",
                "totalAmount", "500.000"
        );

        dispatcherService.dispatchEmail("KEY-123", "ORDER_CREATED_EMAIL", "triet@mochuong.vn", 1L, "ORDER", 100L, vars);

        verify(emailService, times(1)).sendHtmlEmail(
                eq("triet@mochuong.vn"),
                eq("Mộc Hương - Đơn hàng MH999 đã tạo thành công"),
                contains("Xin chào Lê Minh Triết")
        );
        verify(logRepository, times(1)).save(argThat(log -> "SENT".equals(log.getStatus())));
    }

    @Test
    @DisplayName("Bỏ qua gửi email khi idempotencyKey đã có trạng thái SENT (chống gửi trùng)")
    void testDispatchEmail_Idempotent_Skip() {
        NotificationLog existingLog = NotificationLog.builder()
                .id(1L)
                .idempotencyKey("KEY-123")
                .status("SENT")
                .sentAt(LocalDateTime.now())
                .build();

        when(logRepository.findByIdempotencyKey("KEY-123")).thenReturn(Optional.of(existingLog));

        dispatcherService.dispatchEmail("KEY-123", "ORDER_CREATED_EMAIL", "triet@mochuong.vn", 1L, "ORDER", 100L, Map.of());

        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
        verify(templateRepository, never()).findByTemplateKeyAndIsActiveTrue(anyString());
    }

    @Test
    @DisplayName("Gửi email thất bại khi emailService gặp sự cố và ghi log FAILED")
    void testDispatchEmail_EmailServiceError_LogsFailed() {
        when(logRepository.findByIdempotencyKey("KEY-FAIL")).thenReturn(Optional.empty());
        when(templateRepository.findByTemplateKeyAndIsActiveTrue("ORDER_CREATED_EMAIL"))
                .thenReturn(Optional.of(sampleTemplate));
        doThrow(new RuntimeException("SMTP Connection failed"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        dispatcherService.dispatchEmail("KEY-FAIL", "ORDER_CREATED_EMAIL", "triet@mochuong.vn", 1L, "ORDER", 100L, Map.of("fullName", "Triết"));

        verify(logRepository, times(1)).save(argThat(log -> "FAILED".equals(log.getStatus())));
    }
}
