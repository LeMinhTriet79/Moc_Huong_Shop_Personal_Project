package com.minhtriet.se3979.orderservice.config;

import com.minhtriet.se3979.orderservice.entity.Coupon;
import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.entity.OrderItem;
import com.minhtriet.se3979.orderservice.entity.OrderStatusHistory;
import com.minhtriet.se3979.orderservice.entity.Transaction;
import com.minhtriet.se3979.orderservice.repository.CouponRepository;
import com.minhtriet.se3979.orderservice.repository.OrderRepository;
import com.minhtriet.se3979.orderservice.repository.OrderStatusHistoryRepository;
import com.minhtriet.se3979.orderservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedCoupons();
        seedOrders();
    }

    private void seedCoupons() {
        if (couponRepository.count() == 0) {
            log.info("🌱 Đang khởi tạo bộ mã giảm giá chuẩn (bao gồm TINHTAM10)...");
            couponRepository.saveAll(List.of(
                    Coupon.builder()
                            .code("TINHTAM10")
                            .name("Ưu đãi Tĩnh Tâm - Giảm 10% đơn hàng đầu tiên")
                            .type(Coupon.CouponType.PERCENTAGE)
                            .value(new BigDecimal("10.00"))
                            .maxDiscountAmount(new BigDecimal("100000.00"))
                            .minOrderValue(new BigDecimal("200000.00"))
                            .maxUses(500)
                            .maxUsesPerUser(2)
                            .usedCount(12)
                            .startsAt(LocalDateTime.now().minusMonths(1))
                            .expiresAt(LocalDateTime.now().plusYears(1))
                            .isActive(true)
                            .build(),
                    Coupon.builder()
                            .code("MOCHUONG50")
                            .name("Ưu đãi Mộc Hương - Giảm 50.000 ₫ cho đơn từ 500.000 ₫")
                            .type(Coupon.CouponType.FIXED_AMOUNT)
                            .value(new BigDecimal("50000.00"))
                            .maxDiscountAmount(new BigDecimal("50000.00"))
                            .minOrderValue(new BigDecimal("500000.00"))
                            .maxUses(300)
                            .maxUsesPerUser(2)
                            .usedCount(8)
                            .startsAt(LocalDateTime.now().minusMonths(1))
                            .expiresAt(LocalDateTime.now().plusYears(1))
                            .isActive(true)
                            .build(),
                    Coupon.builder()
                            .code("TRAMHUONG10")
                            .name("Tri ân khách hàng Trầm Hương - Giảm 10% tối đa 150.000 ₫")
                            .type(Coupon.CouponType.PERCENTAGE)
                            .value(new BigDecimal("10.00"))
                            .maxDiscountAmount(new BigDecimal("150000.00"))
                            .minOrderValue(new BigDecimal("1000000.00"))
                            .maxUses(200)
                            .maxUsesPerUser(1)
                            .usedCount(5)
                            .startsAt(LocalDateTime.now().minusMonths(1))
                            .expiresAt(LocalDateTime.now().plusYears(1))
                            .isActive(true)
                            .build(),
                    Coupon.builder()
                            .code("FREESHIP0D")
                            .name("Miễn phí vận chuyển toàn quốc cho đơn từ 300.000 ₫")
                            .type(Coupon.CouponType.FREE_SHIPPING)
                            .value(new BigDecimal("30000.00"))
                            .maxDiscountAmount(new BigDecimal("30000.00"))
                            .minOrderValue(new BigDecimal("300000.00"))
                            .maxUses(1000)
                            .maxUsesPerUser(3)
                            .usedCount(25)
                            .startsAt(LocalDateTime.now().minusMonths(1))
                            .expiresAt(LocalDateTime.now().plusYears(1))
                            .isActive(true)
                            .build(),
                    Coupon.builder()
                            .code("VIP300K")
                            .name("Đặc quyền thành viên VIP - Giảm 300.000 ₫ cho đơn từ 3.000.000 ₫")
                            .type(Coupon.CouponType.FIXED_AMOUNT)
                            .value(new BigDecimal("300000.00"))
                            .maxDiscountAmount(new BigDecimal("300000.00"))
                            .minOrderValue(new BigDecimal("3000000.00"))
                            .maxUses(100)
                            .maxUsesPerUser(1)
                            .usedCount(2)
                            .startsAt(LocalDateTime.now().minusMonths(1))
                            .expiresAt(LocalDateTime.now().plusYears(1))
                            .isActive(true)
                            .build()
            ));
            log.info("✅ Đã khởi tạo 5 mã giảm giá mẫu (TINHTAM10, MOCHUONG50, TRAMHUONG10, FREESHIP0D, VIP300K)!");
        } else if (couponRepository.findByCodeIgnoreCase("TINHTAM10").isEmpty()) {
            couponRepository.save(Coupon.builder()
                    .code("TINHTAM10")
                    .name("Ưu đãi Tĩnh Tâm - Giảm 10% đơn hàng đầu tiên")
                    .type(Coupon.CouponType.PERCENTAGE)
                    .value(new BigDecimal("10.00"))
                    .maxDiscountAmount(new BigDecimal("100000.00"))
                    .minOrderValue(new BigDecimal("200000.00"))
                    .maxUses(500)
                    .maxUsesPerUser(2)
                    .usedCount(12)
                    .startsAt(LocalDateTime.now().minusMonths(1))
                    .expiresAt(LocalDateTime.now().plusYears(1))
                    .isActive(true)
                    .build());
            log.info("✅ Đã bổ sung mã giảm giá TINHTAM10 khớp Banner Trang Chủ!");
        }
    }

    private Map<String, Object> buildAddress(String name, String phone, String street, String ward, String district, String province) {
        Map<String, Object> addr = new LinkedHashMap<>();
        addr.put("recipientName", name);
        addr.put("recipient_name", name);
        addr.put("phoneNumber", phone);
        addr.put("recipientPhone", phone);
        addr.put("recipient_phone", phone);
        addr.put("streetAddress", street);
        addr.put("street_address", street);
        addr.put("ward", ward);
        addr.put("wardName", ward);
        addr.put("ward_name", ward);
        addr.put("district", district);
        addr.put("districtName", district);
        addr.put("district_name", district);
        addr.put("province", province);
        addr.put("provinceName", province);
        addr.put("province_name", province);
        return addr;
    }

    private void seedOrders() {
        if (orderRepository.count() > 0) {
            log.info("ℹ️ Order database đã có dữ liệu đơn hàng, bỏ qua bước seed.");
            return;
        }

        log.info("🌱 Đang khởi tạo 6 đơn hàng mẫu đa trạng thái cho Mộc Hương Shop...");

        Map<String, Object> addrCustomer3 = buildAddress(
                "Nguyễn Minh Triết", "0901234569",
                "Số 108 Đường Nguyễn Huệ, Tòa nhà Sen Vàng",
                "Phường Bến Nghé", "Quận 1", "TP. Hồ Chí Minh"
        );
        Map<String, Object> addrCustomer4 = buildAddress(
                "Trần Thanh Hà", "0912345678",
                "Số 24 Đường Bạch Đằng",
                "Phường Thạch Thang", "Quận Hải Châu", "TP. Đà Nẵng"
        );
        Map<String, Object> addrCustomer5 = buildAddress(
                "Lê Hoàng Nam", "0987654321",
                "Số 88 Đường Pasteur",
                "Phường Võ Thị Sáu", "Quận 3", "TP. Hồ Chí Minh"
        );

        // ĐƠN 1: DELIVERED (Đã giao thành công - cho khách hàng userId=3)
        Order order1 = Order.builder()
                .orderCode("MH-20261026-0001")
                .userId(3L)
                .status("DELIVERED")
                .shippingAddress(addrCustomer3)
                .shippingFee(BigDecimal.ZERO)
                .subtotal(new BigDecimal("1000000"))
                .discountAmount(new BigDecimal("50000"))
                .totalAmount(new BigDecimal("950000"))
                .couponCode("MOCHUONG50")
                .paymentMethod("VNPAY")
                .paymentStatus("PAID")
                .idempotencyKey("SEED-IDEMP-0001")
                .notes("Giao giờ hành chính, gói quà lịch sự giúp mình nhé.")
                .staffNotes("Khách VIP thân thiết, đã tặng kèm bộ đế cắm nhang bằng đồng.")
                .trackingNumber("GHN-MH882910")
                .carrier("Giao Hàng Nhanh")
                .confirmedAt(LocalDateTime.now().minusDays(5))
                .deliveredAt(LocalDateTime.now().minusDays(2))
                .build();

        order1.addItem(OrderItem.builder()
                .productId(1L).variantId(1L)
                .productName("Nhang Trầm Hương Kiến Tự Nhiên Thượng Hạng (30 Năm Tích Trầm)")
                .variantName("Hộp 50g (~65 nén)")
                .productImageUrl("https://images.unsplash.com/photo-1602928321679-560bb453f190?auto=format&fit=crop&w=800&q=80")
                .sku("MH-NTH-50G")
                .unitPrice(new BigDecimal("350000"))
                .quantity(2)
                .subtotal(new BigDecimal("700000"))
                .reviewId(1L)
                .build());

        order1.addItem(OrderItem.builder()
                .productId(3L).variantId(5L)
                .productName("Nụ Trầm Hương Khói Ngược Thác Liên Hoa (45 Nụ Cao Cấp)")
                .variantName("Hộp 45 nụ")
                .productImageUrl("https://images.unsplash.com/photo-1515377905703-c4788e51af15?auto=format&fit=crop&w=800&q=80")
                .sku("MH-NT-45V")
                .unitPrice(new BigDecimal("300000"))
                .quantity(1)
                .subtotal(new BigDecimal("300000"))
                .build());

        Order savedOrder1 = orderRepository.save(order1);

        orderStatusHistoryRepository.saveAll(List.of(
                OrderStatusHistory.builder().order(savedOrder1).fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(3L).note("Khách hàng khởi tạo đơn hàng").build(),
                OrderStatusHistory.builder().order(savedOrder1).fromStatus("PENDING").toStatus("CONFIRMED").changedByType("SYSTEM").changedById(0L).note("Hệ thống xác nhận thanh toán VNPay thành công").build(),
                OrderStatusHistory.builder().order(savedOrder1).fromStatus("CONFIRMED").toStatus("PREPARING").changedByType("STAFF").changedById(2L).note("Nghệ nhân đang đóng gói lễ hộp").build(),
                OrderStatusHistory.builder().order(savedOrder1).fromStatus("PREPARING").toStatus("SHIPPING").changedByType("STAFF").changedById(2L).note("Đã bàn giao cho Giao Hàng Nhanh - Mã vận đơn GHN-MH882910").build(),
                OrderStatusHistory.builder().order(savedOrder1).fromStatus("SHIPPING").toStatus("DELIVERED").changedByType("STAFF").changedById(2L).note("Giao hàng thành công tới tay quý khách").build()
        ));

        transactionRepository.save(Transaction.builder()
                .order(savedOrder1)
                .transactionCode("TXN-MH202610260001")
                .gatewayTransactionId("VNP14589201")
                .paymentMethod("VNPAY").type("PAYMENT")
                .amount(new BigDecimal("950000")).status("SUCCESS")
                .gatewayResponseCode("00").gatewayResponseMessage("Giao dịch thành công")
                .ipAddress("127.0.0.1")
                .build());

        // ĐƠN 2: SHIPPING (Đang vận chuyển - cho userId=3)
        Order order2 = Order.builder()
                .orderCode("MH-20261026-0002")
                .userId(3L)
                .status("SHIPPING")
                .shippingAddress(addrCustomer3)
                .shippingFee(BigDecimal.ZERO)
                .subtotal(new BigDecimal("2850000"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("2850000"))
                .paymentMethod("COD")
                .paymentStatus("UNPAID")
                .idempotencyKey("SEED-IDEMP-0002")
                .notes("Gọi trước khi giao 15 phút.")
                .trackingNumber("VTPOST-992011")
                .carrier("Viettel Post")
                .confirmedAt(LocalDateTime.now().minusDays(1))
                .build();

        order2.addItem(OrderItem.builder()
                .productId(6L).variantId(11L)
                .productName("Vòng Tay Trầm Hương Tốc Kiến Tự Nhiên 108 Hạt Phối Charm Sen Vàng")
                .variantName("Size hạt 6mm (Nữ)")
                .productImageUrl("https://images.unsplash.com/photo-1611591437281-460bfbe1220a?auto=format&fit=crop&w=800&q=80")
                .sku("MH-VT108-6MM")
                .unitPrice(new BigDecimal("2850000"))
                .quantity(1)
                .subtotal(new BigDecimal("2850000"))
                .build());

        Order savedOrder2 = orderRepository.save(order2);

        orderStatusHistoryRepository.saveAll(List.of(
                OrderStatusHistory.builder().order(savedOrder2).fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(3L).note("Khách hàng đặt đơn COD").build(),
                OrderStatusHistory.builder().order(savedOrder2).fromStatus("PENDING").toStatus("CONFIRMED").changedByType("STAFF").changedById(2L).note("Nhân viên đã gọi xác nhận kích thước cổ tay").build(),
                OrderStatusHistory.builder().order(savedOrder2).fromStatus("CONFIRMED").toStatus("PREPARING").changedByType("STAFF").changedById(2L).note("Đã xâu chuỗi và trì chú thanh tịnh").build(),
                OrderStatusHistory.builder().order(savedOrder2).fromStatus("PREPARING").toStatus("SHIPPING").changedByType("STAFF").changedById(2L).note("Đang vận chuyển qua Viettel Post").build()
        ));

        transactionRepository.save(Transaction.builder()
                .order(savedOrder2)
                .transactionCode("TXN-MH202610260002")
                .paymentMethod("COD").type("PAYMENT")
                .amount(new BigDecimal("2850000")).status("PENDING")
                .ipAddress("127.0.0.1")
                .build());

        // ĐƠN 3: PENDING (Chờ xác nhận - cho userId=3)
        Order order3 = Order.builder()
                .orderCode("MH-20261026-0003")
                .userId(3L)
                .status("PENDING")
                .shippingAddress(addrCustomer3)
                .shippingFee(BigDecimal.ZERO)
                .subtotal(new BigDecimal("540000"))
                .discountAmount(new BigDecimal("54000"))
                .totalAmount(new BigDecimal("486000"))
                .couponCode("TINHTAM10")
                .paymentMethod("COD")
                .paymentStatus("UNPAID")
                .idempotencyKey("SEED-IDEMP-0003")
                .notes("Đóng gói chống sốc kỹ vì là chai thủy tinh.")
                .build();

        order3.addItem(OrderItem.builder()
                .productId(5L).variantId(9L)
                .productName("Tinh Dầu Trầm Hương Nguyên Chất Thượng Hạng (Agarwood Essential Oil)")
                .variantName("Lọ 10ml")
                .productImageUrl("https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=800&q=80")
                .sku("MH-TD-TH-10ML")
                .unitPrice(new BigDecimal("270000"))
                .quantity(2)
                .subtotal(new BigDecimal("540000"))
                .build());

        Order savedOrder3 = orderRepository.save(order3);

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(savedOrder3).fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(3L)
                .note("Khách hàng vừa đặt đơn hàng mới").build());

        transactionRepository.save(Transaction.builder()
                .order(savedOrder3)
                .transactionCode("TXN-MH202610260003")
                .paymentMethod("COD").type("PAYMENT")
                .amount(new BigDecimal("486000")).status("PENDING")
                .ipAddress("127.0.0.1")
                .build());

        // ĐƠN 4: COMPLETED (Hoàn tất - khách hàng Trần Thanh Hà userId=4)
        Order order4 = Order.builder()
                .orderCode("MH-20261026-0004")
                .userId(4L)
                .status("COMPLETED")
                .shippingAddress(addrCustomer4)
                .shippingFee(BigDecimal.ZERO)
                .subtotal(new BigDecimal("1950000"))
                .discountAmount(new BigDecimal("150000"))
                .totalAmount(new BigDecimal("1800000"))
                .couponCode("TRAMHUONG10")
                .paymentMethod("VNPAY")
                .paymentStatus("PAID")
                .idempotencyKey("SEED-IDEMP-0004")
                .trackingNumber("GHN-DN771829")
                .carrier("Giao Hàng Nhanh")
                .confirmedAt(LocalDateTime.now().minusDays(10))
                .deliveredAt(LocalDateTime.now().minusDays(6))
                .build();

        order4.addItem(OrderItem.builder()
                .productId(1L).variantId(3L)
                .productName("Nhang Trầm Hương Kiến Tự Nhiên Thượng Hạng (30 Năm Tích Trầm)")
                .variantName("Ống Sơn Mài Quà Biếu 200g")
                .productImageUrl("https://images.unsplash.com/photo-1602928321679-560bb453f190?auto=format&fit=crop&w=800&q=80")
                .sku("MH-NTH-200G-VIP")
                .unitPrice(new BigDecimal("1950000"))
                .quantity(1)
                .subtotal(new BigDecimal("1950000"))
                .reviewId(2L)
                .build());

        Order savedOrder4 = orderRepository.save(order4);

        orderStatusHistoryRepository.saveAll(List.of(
                OrderStatusHistory.builder().order(savedOrder4).fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(4L).note("Khách đặt Lễ hộp Sơn Mài Quà Biếu").build(),
                OrderStatusHistory.builder().order(savedOrder4).fromStatus("PENDING").toStatus("CONFIRMED").changedByType("SYSTEM").changedById(0L).note("Thanh toán VNPay thành công").build(),
                OrderStatusHistory.builder().order(savedOrder4).fromStatus("CONFIRMED").toStatus("DELIVERED").changedByType("STAFF").changedById(2L).note("Đã giao thành công tại Đà Nẵng").build(),
                OrderStatusHistory.builder().order(savedOrder4).fromStatus("DELIVERED").toStatus("COMPLETED").changedByType("CUSTOMER").changedById(4L).note("Khách hàng xác nhận hài lòng và hoàn tất đơn").build()
        ));

        transactionRepository.save(Transaction.builder()
                .order(savedOrder4)
                .transactionCode("TXN-MH202610260004")
                .gatewayTransactionId("VNP14580099")
                .paymentMethod("VNPAY").type("PAYMENT")
                .amount(new BigDecimal("1800000")).status("SUCCESS")
                .gatewayResponseCode("00").gatewayResponseMessage("Giao dịch thành công")
                .ipAddress("127.0.0.1")
                .build());

        // ĐƠN 5: PREPARING (Đang chuẩn bị - khách hàng Lê Hoàng Nam userId=5)
        Order order5 = Order.builder()
                .orderCode("MH-20261026-0005")
                .userId(5L)
                .status("PREPARING")
                .shippingAddress(addrCustomer5)
                .shippingFee(BigDecimal.ZERO)
                .subtotal(new BigDecimal("870000"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("870000"))
                .paymentMethod("VNPAY")
                .paymentStatus("PAID")
                .idempotencyKey("SEED-IDEMP-0005")
                .confirmedAt(LocalDateTime.now().minusHours(6))
                .build();

        order5.addItem(OrderItem.builder()
                .productId(2L).variantId(4L)
                .productName("Nhang Vòng Trầm Hương Quảng Nam 24 Giờ Tịnh Tâm")
                .variantName("Hộp 10 vòng lớn")
                .productImageUrl("https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?auto=format&fit=crop&w=800&q=80")
                .sku("MH-NV24H-10V")
                .unitPrice(new BigDecimal("450000"))
                .quantity(1)
                .subtotal(new BigDecimal("450000"))
                .build());

        order5.addItem(OrderItem.builder()
                .productId(4L).variantId(7L)
                .productName("Lư Xông Trầm Gốm Tử Sa Nghi Hưng Thủ Công Nắp Đồng Chạm Hoa Sen")
                .variantName("Bộ lư tiêu chuẩn")
                .productImageUrl("https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?auto=format&fit=crop&w=800&q=80")
                .sku("MH-LX-TUSA-01")
                .unitPrice(new BigDecimal("420000"))
                .quantity(1)
                .subtotal(new BigDecimal("420000"))
                .build());

        Order savedOrder5 = orderRepository.save(order5);

        orderStatusHistoryRepository.saveAll(List.of(
                OrderStatusHistory.builder().order(savedOrder5).fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(5L).note("Khách hàng tạo đơn").build(),
                OrderStatusHistory.builder().order(savedOrder5).fromStatus("PENDING").toStatus("CONFIRMED").changedByType("SYSTEM").changedById(0L).note("Đã thanh toán qua VNPay").build(),
                OrderStatusHistory.builder().order(savedOrder5).fromStatus("CONFIRMED").toStatus("PREPARING").changedByType("STAFF").changedById(2L).note("Đang bọc chống sốc lư gốm Tử Sa").build()
        ));

        transactionRepository.save(Transaction.builder()
                .order(savedOrder5)
                .transactionCode("TXN-MH202610260005")
                .gatewayTransactionId("VNP14591122")
                .paymentMethod("VNPAY").type("PAYMENT")
                .amount(new BigDecimal("870000")).status("SUCCESS")
                .gatewayResponseCode("00").gatewayResponseMessage("Giao dịch thành công")
                .ipAddress("127.0.0.1")
                .build());

        // ĐƠN 6: CANCELLED (Đã hủy đơn - cho khách hàng Lê Hoàng Nam userId=5)
        Order order6 = Order.builder()
                .orderCode("MH-20261026-0006")
                .userId(5L)
                .status("CANCELLED")
                .shippingAddress(addrCustomer5)
                .shippingFee(new BigDecimal("30000"))
                .subtotal(new BigDecimal("350000"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("380000"))
                .paymentMethod("COD")
                .paymentStatus("UNPAID")
                .idempotencyKey("SEED-IDEMP-0006")
                .cancelledReason("Khách hàng đổi ý muốn đặt sang lễ hộp 200g")
                .cancelledBy("CUSTOMER")
                .build();

        order6.addItem(OrderItem.builder()
                .productId(1L).variantId(1L)
                .productName("Nhang Trầm Hương Kiến Tự Nhiên Thượng Hạng (30 Năm Tích Trầm)")
                .variantName("Hộp 50g (~65 nén)")
                .productImageUrl("https://images.unsplash.com/photo-1602928321679-560bb453f190?auto=format&fit=crop&w=800&q=80")
                .sku("MH-NTH-50G")
                .unitPrice(new BigDecimal("350000"))
                .quantity(1)
                .subtotal(new BigDecimal("350000"))
                .build());

        Order savedOrder6 = orderRepository.save(order6);

        orderStatusHistoryRepository.saveAll(List.of(
                OrderStatusHistory.builder().order(savedOrder6).fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(5L).note("Khách tạo đơn").build(),
                OrderStatusHistory.builder().order(savedOrder6).fromStatus("PENDING").toStatus("CANCELLED").changedByType("CUSTOMER").changedById(5L).note("Khách hàng đổi ý muốn đặt sang lễ hộp 200g").build()
        ));

        log.info("✅ Đã khởi tạo thành công 6 đơn hàng mẫu đa trạng thái cho Mộc Hương Shop!");
    }
}
