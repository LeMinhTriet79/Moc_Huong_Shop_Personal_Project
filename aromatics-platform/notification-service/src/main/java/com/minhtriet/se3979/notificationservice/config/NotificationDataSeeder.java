package com.minhtriet.se3979.notificationservice.config;

import com.minhtriet.se3979.notificationservice.entity.NotificationLog;
import com.minhtriet.se3979.notificationservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class NotificationDataSeeder implements CommandLineRunner {

    private final NotificationLogRepository notificationLogRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (notificationLogRepository.count() > 0) {
            log.info("ℹ️ Notification database đã có dữ liệu, bỏ qua bước seed.");
            return;
        }

        log.info("🌱 Đang khởi tạo dữ liệu mẫu NotificationLog cho Mộc Hương Shop...");

        notificationLogRepository.saveAll(List.of(
                NotificationLog.builder()
                        .idempotencyKey("SEED-NOTIF-0001")
                        .userId(3L)
                        .channel("EMAIL")
                        .recipient("customer@mochuong.vn")
                        .subject("[Mộc Hương] Chào mừng Nguyễn Minh Triết đến với Không Gian Thiền Hương")
                        .contentPreview("Cảm ơn quý khách đã đăng ký tài khoản tại Mộc Hương Shop.")
                        .bodyContent("<p>Cảm ơn quý khách đã đăng ký tài khoản tại Mộc Hương Shop. Kính chúc quý khách có những phút giây tĩnh tâm bên tách trà và làn khói trầm.</p>")
                        .eventType("USER_REGISTERED")
                        .referenceType("USER")
                        .referenceId(3L)
                        .status("SENT")
                        .retryCount(0)
                        .sentAt(LocalDateTime.now().minusDays(7))
                        .build(),

                NotificationLog.builder()
                        .idempotencyKey("SEED-NOTIF-0002")
                        .userId(3L)
                        .channel("EMAIL")
                        .recipient("customer@mochuong.vn")
                        .subject("[Mộc Hương] Xác nhận đơn hàng #MH-20261026-0001")
                        .contentPreview("Đơn hàng #MH-20261026-0001 trị giá 950.000 ₫ đã được thanh toán qua VNPay.")
                        .bodyContent("<p>Đơn hàng #MH-20261026-0001 trị giá 950.000 ₫ đã được thanh toán thành công qua cổng VNPay. Nghệ nhân đang chuẩn bị lễ hộp chu đáo.</p>")
                        .eventType("ORDER_CONFIRMED")
                        .referenceType("ORDER")
                        .referenceId(1L)
                        .status("SENT")
                        .retryCount(0)
                        .sentAt(LocalDateTime.now().minusDays(5))
                        .build(),

                NotificationLog.builder()
                        .idempotencyKey("SEED-NOTIF-0003")
                        .userId(3L)
                        .channel("EMAIL")
                        .recipient("customer@mochuong.vn")
                        .subject("[Mộc Hương] Đơn hàng #MH-20261026-0001 đã giao thành công")
                        .contentPreview("Cảm ơn quý khách đã tin chọn phẩm hương Mộc Hương. Mời quý khách chia sẻ cảm nhận!")
                        .bodyContent("<p>Đơn hàng #MH-20261026-0001 đã giao thành công tới tay quý khách. Hãy cùng thắp một nén trầm và thưởng lãm hương thanh tịnh.</p>")
                        .eventType("ORDER_DELIVERED")
                        .referenceType("ORDER")
                        .referenceId(1L)
                        .status("SENT")
                        .retryCount(0)
                        .sentAt(LocalDateTime.now().minusDays(2))
                        .build(),

                NotificationLog.builder()
                        .idempotencyKey("SEED-NOTIF-0004")
                        .userId(3L)
                        .channel("EMAIL")
                        .recipient("customer@mochuong.vn")
                        .subject("[Mộc Hương] Đơn hàng #MH-20261026-0002 đang trên đường giao đến bạn")
                        .contentPreview("Đơn hàng #MH-20261026-0002 đã được bàn giao cho Viettel Post (Mã vận đơn: VTPOST-992011).")
                        .bodyContent("<p>Đơn hàng #MH-20261026-0002 đã được bàn giao cho đơn vị vận chuyển Viettel Post. Mã tra cứu hành trình: VTPOST-992011.</p>")
                        .eventType("ORDER_SHIPPED")
                        .referenceType("ORDER")
                        .referenceId(2L)
                        .status("SENT")
                        .retryCount(0)
                        .sentAt(LocalDateTime.now().minusHours(12))
                        .build()
        ));

        log.info("✅ Đã khởi tạo 4 bản ghi NotificationLog mẫu thành công!");
    }
}
