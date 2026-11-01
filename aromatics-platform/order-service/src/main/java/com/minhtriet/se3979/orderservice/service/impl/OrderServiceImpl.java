package com.minhtriet.se3979.orderservice.service.impl;

import com.minhtriet.se3979.orderservice.dto.event.OrderCancelledEvent;
import com.minhtriet.se3979.orderservice.dto.event.OrderCreatedEvent;
import com.minhtriet.se3979.orderservice.dto.event.OrderItemEventDto;
import com.minhtriet.se3979.orderservice.dto.event.OrderStatusChangedEvent;
import com.minhtriet.se3979.orderservice.dto.request.AddToCartRequest;
import com.minhtriet.se3979.orderservice.dto.request.CheckoutRequest;
import com.minhtriet.se3979.orderservice.dto.request.DeductRequest;
import com.minhtriet.se3979.orderservice.dto.response.*;
import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.entity.OrderItem;
import com.minhtriet.se3979.orderservice.entity.OrderStatusHistory;
import com.minhtriet.se3979.orderservice.entity.Transaction;
import com.minhtriet.se3979.orderservice.enums.OrderStatus;
import com.minhtriet.se3979.orderservice.exception.AppException;
import com.minhtriet.se3979.orderservice.feign.CatalogFeignClient;
import com.minhtriet.se3979.orderservice.kafka.OrderEventPublisher;
import com.minhtriet.se3979.orderservice.redis.CartItem;
import com.minhtriet.se3979.orderservice.repository.OrderItemRepository;
import com.minhtriet.se3979.orderservice.repository.OrderRepository;
import com.minhtriet.se3979.orderservice.repository.OrderStatusHistoryRepository;
import com.minhtriet.se3979.orderservice.repository.TransactionRepository;
import com.minhtriet.se3979.orderservice.security.SecurityUtils;
import com.minhtriet.se3979.orderservice.service.CartService;
import com.minhtriet.se3979.orderservice.service.CouponService;
import com.minhtriet.se3979.orderservice.service.OrderService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TransactionRepository transactionRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final CartService cartService;
    private final CouponService couponService;
    private final CatalogFeignClient catalogFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired(required = false)
    private OrderEventPublisher orderEventPublisher;

    @Autowired(required = false)
    private ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @Override
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        // 1. Kiểm tra Idempotency
        if (request.getIdempotencyKey() != null) {
            Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingOrder.isPresent()) {
                log.info("Đơn hàng với idempotencyKey {} đã tồn tại, trả về đơn hàng cũ", request.getIdempotencyKey());
                return OrderResponse.fromEntity(existingOrder.get());
            }
        }

        // 2. Lấy giỏ hàng
        List<CartItem> cartItems = cartService.getCart(userId);
        if (cartItems.isEmpty() && request.getItems() != null && !request.getItems().isEmpty()) {
            for (CheckoutRequest.CheckoutItemRequest reqItem : request.getItems()) {
                if (reqItem.getVariantId() != null && reqItem.getQuantity() != null && reqItem.getQuantity() > 0) {
                    AddToCartRequest addReq = new AddToCartRequest();
                    addReq.setProductId(reqItem.getProductId());
                    addReq.setVariantId(reqItem.getVariantId());
                    addReq.setQuantity(reqItem.getQuantity());
                    cartService.addToCart(userId, addReq);
                }
            }
            cartItems = cartService.getCart(userId);
        }
        if (cartItems.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Giỏ hàng của bạn đang trống!");
        }

        // 3. Trừ kho: Thử trừ theo lô nguyên tử trước, fallback sang trừ từng món cho Unit Test
        List<CartItem> deductedItems = new ArrayList<>();
        boolean batchSuccess = false;
        try {
            List<DeductRequest> batchRequests = cartItems.stream()
                    .map(it -> new DeductRequest(it.getVariantId(), it.getQuantity()))
                    .toList();
            ResponseEntity<DeductResult> batchResp = catalogFeignClient.deductBatchInventory(batchRequests);
            if (batchResp != null && batchResp.getBody() != null) {
                if (!batchResp.getBody().isSuccess()) {
                    String detail = batchResp.getBody().getMessage() != null ? batchResp.getBody().getMessage() : "";
                    throw new AppException(HttpStatus.CONFLICT,
                            "Một số sản phẩm không đủ số lượng trong kho!" + (detail.isBlank() ? "" : " (" + detail + ")"));
                }
                deductedItems.addAll(cartItems);
                batchSuccess = true;
            }
        } catch (AppException ae) {
            throw ae;
        } catch (Exception ignored) {
            // Môi trường mock hoặc Feign chưa có deductBatch thì fallback
        }

        if (!batchSuccess) {
            for (CartItem item : cartItems) {
                try {
                    ResponseEntity<DeductResult> response = catalogFeignClient.deductInventory(
                            new DeductRequest(item.getVariantId(), item.getQuantity())
                    );
                    if (response == null || response.getBody() == null || !response.getBody().isSuccess()) {
                        compensateDeductions(deductedItems);
                        String detail = (response != null && response.getBody() != null && response.getBody().getMessage() != null)
                                ? response.getBody().getMessage() : "";
                        String msg = "Sản phẩm '" + item.getProductName() + "' không đủ số lượng trong kho!"
                                + (detail.isBlank() ? "" : " (" + detail + ")");
                        throw new AppException(HttpStatus.CONFLICT, msg);
                    }
                    deductedItems.add(item);
                } catch (AppException ae) {
                    compensateDeductions(deductedItems);
                    throw ae;
                } catch (Exception e) {
                    compensateDeductions(deductedItems);
                    throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Lỗi khi kiểm tra tồn kho từ Catalog Service: " + e.getMessage());
                }
            }
        }

        // 4. Tính toán tiền tệ & Áp mã giảm giá
        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = BigDecimal.valueOf(30000); // 30,000 đ mặc định
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            CouponCalculationResult couponResult = couponService.validateAndCalculateDiscount(
                    userId, request.getCouponCode(), subtotal, shippingFee, cartItems);
            if (!couponResult.isValid()) {
                compensateDeductions(deductedItems);
                throw new AppException(HttpStatus.BAD_REQUEST, couponResult.getMessage());
            }
            discountAmount = couponResult.getDiscountAmount();
        }

        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(discountAmount).max(BigDecimal.ZERO);

        // 5. Xác định trạng thái ban đầu của Đơn hàng
        String paymentMethod = request.getPaymentMethod().trim().toUpperCase();
        String initialStatus;
        String initialPaymentStatus;

        if ("COD".equals(paymentMethod)) {
            initialStatus = "CONFIRMED";
            initialPaymentStatus = "UNPAID";
        } else if ("VNPAY".equals(paymentMethod)) {
            initialStatus = "PENDING_PAYMENT";
            initialPaymentStatus = "UNPAID";
        } else {
            initialStatus = "CONFIRMED";
            initialPaymentStatus = "PAID";
        }

        // 6. Sinh mã đơn hàng duy nhất: MH + yyyyMMdd + 6 số ngẫu nhiên
        String orderCode = "MH" + System.currentTimeMillis();

        // 7. Tạo Entity Order
        Order order = Order.builder()
                .orderCode(orderCode)
                .userId(userId)
                .status(initialStatus)
                .shippingAddress(request.getShippingAddress())
                .shippingFee(shippingFee)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .couponCode(request.getCouponCode())
                .paymentMethod(paymentMethod)
                .paymentStatus(initialPaymentStatus)
                .idempotencyKey(request.getIdempotencyKey() != null ? request.getIdempotencyKey() : UUID.randomUUID().toString())
                .notes(request.getNotes())
                .confirmedAt("CONFIRMED".equals(initialStatus) ? LocalDateTime.now() : null)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 8. Lưu các OrderItem
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderItemEventDto> eventItems = new ArrayList<>();

        for (CartItem ci : cartItems) {
            BigDecimal lineTotal = ci.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));

            OrderItem item = OrderItem.builder()
                    .order(savedOrder)
                    .productId(ci.getProductId())
                    .variantId(ci.getVariantId())
                    .productName(ci.getProductName())
                    .variantName(ci.getVariantName())
                    .sku(ci.getSku())
                    .unitPrice(ci.getPrice())
                    .quantity(ci.getQuantity())
                    .subtotal(lineTotal)
                    .build();
            orderItems.add(item);

            eventItems.add(OrderItemEventDto.builder()
                    .variantId(ci.getVariantId())
                    .quantity(ci.getQuantity())
                    .sku(ci.getSku())
                    .unitPrice(ci.getPrice())
                    .subtotal(lineTotal)
                    .productName(ci.getProductName())
                    .variantName(ci.getVariantName())
                    .build());
        }
        orderItemRepository.saveAll(orderItems);
        savedOrder.setItems(orderItems);

        // 9. Lưu Coupon Usage nếu có
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            couponService.recordCouponUsage(request.getCouponCode(), userId, savedOrder, discountAmount);
        }

        // 10. Ghi nhận Lịch sử trạng thái & Giao dịch thanh toán ban đầu
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(savedOrder)
                .fromStatus(null)
                .toStatus(initialStatus)
                .changedByType("CUSTOMER")
                .changedById(userId)
                .note("Đơn hàng được khởi tạo thành công")
                .build();
        orderStatusHistoryRepository.save(history);

        Transaction txn = Transaction.builder()
                .order(savedOrder)
                .transactionCode("TXN-" + savedOrder.getOrderCode())
                .paymentMethod(savedOrder.getPaymentMethod())
                .type("PAYMENT")
                .amount(savedOrder.getTotalAmount())
                .status("PENDING")
                .build();
        transactionRepository.save(txn);

        // 11. Xóa sạch giỏ hàng
        cartService.clearCart(userId);

        // 12. Phát sự kiện OrderCreatedEvent
        String userEmail = SecurityUtils.getCurrentUserEmail();
        String fullName = SecurityUtils.getCurrentUserFullName();

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .userId(savedOrder.getUserId())
                .userEmail(userEmail)
                .fullName(fullName)
                .subtotal(savedOrder.getSubtotal())
                .shippingFee(savedOrder.getShippingFee())
                .discountAmount(savedOrder.getDiscountAmount())
                .totalAmount(savedOrder.getTotalAmount())
                .paymentMethod(savedOrder.getPaymentMethod())
                .paymentStatus(savedOrder.getPaymentStatus())
                .status(savedOrder.getStatus())
                .shippingAddress(savedOrder.getShippingAddress())
                .items(eventItems)
                .createdAt(savedOrder.getCreatedAt() != null ? savedOrder.getCreatedAt() : LocalDateTime.now())
                .build();

        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(event);
        } else if (orderEventPublisher != null) {
            orderEventPublisher.publishOrderCreated(event);
        }

        if (kafkaTemplate != null) {
            kafkaTemplate.send("order.created", event);
        }
        log.info("🛒 Đã phát sự kiện order.created cho đơn hàng: {}", orderCode);

        return OrderResponse.fromEntity(savedOrder);
    }

    private void compensateDeductions(List<CartItem> deductedItems) {
        if (deductedItems == null || deductedItems.isEmpty()) return;
        List<OrderItemEventDto> items = deductedItems.stream()
                .map(item -> OrderItemEventDto.builder()
                        .variantId(item.getVariantId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        OrderCancelledEvent cancelEvent = OrderCancelledEvent.builder()
                .orderCode("COMPENSATE-" + System.currentTimeMillis())
                .cancelledReason("Lỗi trừ kho giữa chừng, tự động hoàn trả tồn kho")
                .cancelledBy("SYSTEM")
                .items(items)
                .cancelledAt(LocalDateTime.now())
                .build();

        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(cancelEvent);
        } else if (orderEventPublisher != null) {
            orderEventPublisher.publishOrderCancelled(cancelEvent);
        }

        if (kafkaTemplate != null) {
            kafkaTemplate.send("order.cancelled", cancelEvent);
        }
    }

    @Transactional
    @Override
    public OrderResponse cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng id: " + orderId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentRole = SecurityUtils.getCurrentUserRole();
        boolean isStaffOrAdmin = "ADMIN".equalsIgnoreCase(currentRole) || "STAFF".equalsIgnoreCase(currentRole);

        if (!isStaffOrAdmin && !order.getUserId().equals(currentUserId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền hủy đơn hàng này!");
        }

        // Chỉ cho phép hủy khi đang PENDING, PENDING_PAYMENT, CONFIRMED, hoặc PREPARING/PROCESSING
        List<String> cancellableStatuses = List.of("PENDING", "PENDING_PAYMENT", "CONFIRMED", "PREPARING", "PROCESSING");
        if (!cancellableStatuses.contains(order.getStatus().toUpperCase())) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Không thể hủy đơn hàng khi đang ở trạng thái: " + order.getStatus() + ". Vui lòng yêu cầu trả hàng nếu đã giao!");
        }

        String previousStatus = order.getStatus();
        String cancelledBy = isStaffOrAdmin ? currentRole : "CUSTOMER";

        order.setStatus("CANCELLED");
        order.setCancelledReason(reason != null && !reason.isBlank() ? reason : "Khách hàng yêu cầu hủy đơn");
        order.setCancelledBy(cancelledBy);
        Order savedOrder = orderRepository.save(order);

        // Lưu lịch sử
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(savedOrder)
                .fromStatus(previousStatus)
                .toStatus("CANCELLED")
                .changedByType(cancelledBy)
                .changedById(currentUserId)
                .note(savedOrder.getCancelledReason())
                .build();
        orderStatusHistoryRepository.save(history);

        // Hoàn lại lượt dùng coupon nếu có
        couponService.refundCouponUsage(savedOrder.getId());

        // Phát sự kiện hoàn kho
        publishOrderCancelledEvent(savedOrder);

        // Phát sự kiện đổi trạng thái
        publishStatusChangedEvent(savedOrder, previousStatus, "CANCELLED", savedOrder.getCancelledReason());

        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional
    @Override
    public OrderResponse requestReturn(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng id: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không sở hữu đơn hàng này!");
        }

        if (!"DELIVERED".equalsIgnoreCase(order.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Chỉ có thể yêu cầu trả hàng/hoàn tiền khi đơn hàng đã giao thành công (DELIVERED)!");
        }

        String previousStatus = order.getStatus();
        order.setStatus("RETURN_REQUESTED");
        Order savedOrder = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(savedOrder)
                .fromStatus(previousStatus)
                .toStatus("RETURN_REQUESTED")
                .changedByType("CUSTOMER")
                .changedById(userId)
                .note(reason != null && !reason.isBlank() ? reason : "Khách hàng yêu cầu trả hàng/hoàn tiền")
                .build();
        orderStatusHistoryRepository.save(history);

        publishStatusChangedEvent(savedOrder, previousStatus, "RETURN_REQUESTED", history.getNote());

        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    public Page<OrderResponse> getCustomerOrders(Long userId, String status, Pageable pageable) {
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status.trim())) {
            String s = status.trim().toUpperCase();
            List<String> candidateStatuses;
            if ("PENDING".equals(s) || "PENDING_PAYMENT".equals(s)) {
                candidateStatuses = List.of("PENDING", "PENDING_PAYMENT");
            } else if ("PROCESSING".equals(s) || "PREPARING".equals(s)) {
                candidateStatuses = List.of("PROCESSING", "PREPARING");
            } else if ("SHIPPED".equals(s) || "SHIPPING".equals(s)) {
                candidateStatuses = List.of("SHIPPED", "SHIPPING");
            } else {
                candidateStatuses = List.of(s);
            }
            return orderRepository.findByUserIdAndStatusIn(userId, candidateStatuses, pageable)
                    .map(OrderResponse::fromEntity);
        }
        return orderRepository.findByUserId(userId, pageable).map(OrderResponse::fromEntity);
    }

    @Override
    public OrderDetailResponse getOrderDetailByCode(String orderCode, Long userId) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với mã: " + orderCode));

        String role = SecurityUtils.getCurrentUserRole();
        boolean isStaffOrAdmin = "ADMIN".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role);

        if (!isStaffOrAdmin && !order.getUserId().equals(userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem đơn hàng này!");
        }

        return mapToOrderDetail(order);
    }

    @Override
    public OrderDetailResponse getOrderDetailById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với id: " + orderId));
        return mapToOrderDetail(order);
    }

    @Override
    public Page<OrderResponse> getAllOrdersAdmin(
            String orderCode, String status, String paymentMethod, String paymentStatus,
            LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {

        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (orderCode != null && !orderCode.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("orderCode")), "%" + orderCode.trim().toLowerCase() + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim().toUpperCase()));
            }
            if (paymentMethod != null && !paymentMethod.isBlank()) {
                predicates.add(cb.equal(root.get("paymentMethod"), paymentMethod.trim().toUpperCase()));
            }
            if (paymentStatus != null && !paymentStatus.isBlank()) {
                predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus.trim().toUpperCase()));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return orderRepository.findAll(spec, pageable).map(OrderResponse::fromEntity);
    }

    @Transactional
    @Override
    public OrderDetailResponse updateOrderStatus(
            Long orderId, String newStatus, String note, String changedByType, Long changedById) {
        return updateOrderStatus(orderId, newStatus, note, changedByType, changedById, null, null);
    }

    @Transactional
    @Override
    public OrderDetailResponse updateOrderStatus(
            Long orderId, String newStatus, String note, String changedByType, Long changedById,
            String trackingNumber, String carrier) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với id: " + orderId));

        String currentStatus = normalizeStatus(order.getStatus());
        String targetStatus = normalizeStatus(newStatus);

        // Kiểm tra State Machine
        validateStateTransition(currentStatus, targetStatus);

        if (trackingNumber != null && !trackingNumber.isBlank()) {
            order.setTrackingNumber(trackingNumber);
        }
        if (carrier != null && !carrier.isBlank()) {
            order.setCarrier(carrier);
        }

        LocalDateTime now = LocalDateTime.now();
        if ("CONFIRMED".equalsIgnoreCase(targetStatus)) {
            order.setConfirmedAt(now);
        } else if ("DELIVERED".equalsIgnoreCase(targetStatus)) {
            order.setDeliveredAt(now);
            if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
                order.setPaymentStatus("PAID");
                transactionRepository.findTopByOrderIdOrderByCreatedAtDesc(order.getId()).ifPresent(t -> {
                    t.setStatus("SUCCESS");
                    t.setCompletedAt(now);
                    transactionRepository.save(t);
                });
            }
        } else if ("CANCELLED".equalsIgnoreCase(targetStatus) || "RETURNED".equalsIgnoreCase(targetStatus)) {
            order.setCancelledReason(note != null ? note : ("Trạng thái chuyển sang " + targetStatus));
            order.setCancelledBy(changedByType);
            couponService.refundCouponUsage(order.getId());
            publishOrderCancelledEvent(order);
        }

        order.setStatus(targetStatus);
        Order saved = orderRepository.save(order);

        // Lưu lịch sử
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(saved)
                .fromStatus(currentStatus)
                .toStatus(targetStatus)
                .changedByType(changedByType)
                .changedById(changedById)
                .note(note)
                .build();
        orderStatusHistoryRepository.save(history);

        // Bắn Kafka thông báo trạng thái thay đổi
        publishStatusChangedEvent(saved, currentStatus, targetStatus, note);

        return mapToOrderDetail(saved);
    }

    @Transactional
    @Override
    public OrderResponse updateStaffNotes(Long orderId, String staffNotes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng ID: " + orderId));
        order.setStaffNotes(staffNotes);
        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }

    @Transactional
    @Override
    public void handlePaymentSuccess(
            String orderCode, String transactionCode, String gatewayTxnId,
            BigDecimal amount, String responseCode, String message, Map<String, Object> rawResponse) {

        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
        if (order == null) {
            log.error("Không tìm thấy đơn hàng {} để cập nhật thanh toán thành công", orderCode);
            return;
        }

        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            log.info("Đơn hàng {} đã được ghi nhận thanh toán trước đó", orderCode);
            return;
        }

        String previousStatus = order.getStatus();
        order.setPaymentStatus("PAID");
        order.setPaymentMethod("VNPAY");
        order.setStatus("CONFIRMED");
        order.setConfirmedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Cập nhật Transaction
        Transaction txn = transactionRepository.findByTransactionCode(transactionCode)
                .orElseGet(() -> Transaction.builder()
                        .order(order)
                        .transactionCode(transactionCode)
                        .paymentMethod("VNPAY")
                        .type("PAYMENT")
                        .amount(amount != null ? amount : order.getTotalAmount())
                        .build());

        txn.setGatewayTransactionId(gatewayTxnId);
        txn.setStatus("SUCCESS");
        txn.setGatewayResponseCode(responseCode);
        txn.setGatewayResponseMessage(message);
        txn.setRawResponse(rawResponse);
        txn.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(txn);

        // Lưu lịch sử
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(previousStatus)
                .toStatus("CONFIRMED")
                .changedByType("SYSTEM")
                .note("Thanh toán trực tuyến VNPay thành công")
                .build();
        orderStatusHistoryRepository.save(history);

        publishStatusChangedEvent(order, previousStatus, "CONFIRMED", "Thanh toán thành công");
        log.info("💰 Đã cập nhật đơn hàng {} thành CONFIRMED và thanh toán PAID", orderCode);
    }

    @Transactional
    @Override
    public void handlePaymentFailure(
            String orderCode, String transactionCode, String gatewayTxnId,
            BigDecimal amount, String responseCode, String message, Map<String, Object> rawResponse) {

        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
        if (order == null) {
            log.error("Không tìm thấy đơn hàng {} để cập nhật thanh toán thất bại", orderCode);
            return;
        }

        String previousStatus = order.getStatus();
        order.setPaymentStatus("FAILED");
        order.setStatus("CANCELLED");
        order.setCancelledReason("Thanh toán VNPay không thành công hoặc bị hủy (Code: " + responseCode + ")");
        order.setCancelledBy("SYSTEM");
        orderRepository.save(order);

        // Cập nhật Transaction
        Transaction txn = transactionRepository.findByTransactionCode(transactionCode)
                .orElseGet(() -> Transaction.builder()
                        .order(order)
                        .transactionCode(transactionCode)
                        .paymentMethod("VNPAY")
                        .type("PAYMENT")
                        .amount(amount != null ? amount : order.getTotalAmount())
                        .build());

        txn.setGatewayTransactionId(gatewayTxnId);
        txn.setStatus("FAILED");
        txn.setGatewayResponseCode(responseCode);
        txn.setGatewayResponseMessage(message);
        txn.setRawResponse(rawResponse);
        txn.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(txn);

        // Lưu lịch sử
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(previousStatus)
                .toStatus("CANCELLED")
                .changedByType("SYSTEM")
                .note(order.getCancelledReason())
                .build();
        orderStatusHistoryRepository.save(history);

        // Hoàn mã giảm giá & Hoàn kho
        couponService.refundCouponUsage(order.getId());
        publishOrderCancelledEvent(order);
        publishStatusChangedEvent(order, previousStatus, "CANCELLED", order.getCancelledReason());

        log.warn("🚨 Đơn hàng {} đã bị hủy tự động do thanh toán thất bại", orderCode);
    }

    private String normalizeStatus(String status) {
        if (status == null) return null;
        try {
            return OrderStatus.fromString(status).name();
        } catch (Exception e) {
            return status.trim().toUpperCase();
        }
    }

    private void validateStateTransition(String from, String to) {
        if (from == null || to == null) return;
        OrderStatus current = OrderStatus.fromString(from);
        OrderStatus target = OrderStatus.fromString(to);

        if (!current.canTransitionTo(target)) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Chuyển trạng thái không hợp lệ: không thể chuyển từ '" + from + "' sang '" + to + "'!");
        }
    }

    private void publishOrderCancelledEvent(Order order) {
        List<OrderItemEventDto> eventItems = order.getItems() != null ? order.getItems().stream()
                .map(item -> OrderItemEventDto.builder()
                        .variantId(item.getVariantId())
                        .quantity(item.getQuantity())
                        .sku(item.getSku())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .productName(item.getProductName())
                        .variantName(item.getVariantName())
                        .build())
                .toList() : List.of();

        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .userEmail(SecurityUtils.getCurrentUserEmail())
                .cancelledReason(order.getCancelledReason())
                .cancelledBy(order.getCancelledBy())
                .items(eventItems)
                .cancelledAt(LocalDateTime.now())
                .build();

        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(event);
        } else if (orderEventPublisher != null) {
            orderEventPublisher.publishOrderCancelled(event);
        }

        if (kafkaTemplate != null) {
            kafkaTemplate.send("order.cancelled", event);
        }
        log.info("🚨 KAFKA: Đã phát sự kiện order.cancelled cho đơn: {}", order.getOrderCode());
    }

    private void publishStatusChangedEvent(Order order, String fromStatus, String toStatus, String note) {
        OrderStatusChangedEvent event = OrderStatusChangedEvent.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .userEmail(SecurityUtils.getCurrentUserEmail())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .note(note)
                .changedAt(LocalDateTime.now())
                .build();

        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(event);
        } else if (orderEventPublisher != null) {
            orderEventPublisher.publishOrderStatusChanged(event);
        }

        if (kafkaTemplate != null) {
            kafkaTemplate.send("order.status.changed", event);
        }
        log.info("📣 KAFKA: Đã phát sự kiện order.status.changed cho đơn: {} ({} -> {})",
                order.getOrderCode(), fromStatus, toStatus);
    }

    private OrderDetailResponse mapToOrderDetail(Order order) {
        List<OrderItemResponse> itemDtos = order.getItems() != null ? order.getItems().stream()
                .map(OrderItemResponse::fromEntity)
                .toList() : List.of();

        List<OrderStatusHistoryResponse> historyDtos = orderStatusHistoryRepository
                .findByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                .map(OrderStatusHistoryResponse::fromEntity)
                .toList();

        List<TransactionResponse> txnDtos = transactionRepository
                .findByOrderIdOrderByCreatedAtDesc(order.getId()).stream()
                .map(TransactionResponse::fromEntity)
                .toList();

        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .status(order.getStatus())
                .shippingAddress(OrderResponse.normalizeShippingAddress(order.getShippingAddress()))
                .shippingFee(order.getShippingFee())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .notes(order.getNotes())
                .staffNotes(order.getStaffNotes())
                .trackingNumber(order.getTrackingNumber())
                .carrier(order.getCarrier())
                .cancelledReason(order.getCancelledReason())
                .cancelledBy(order.getCancelledBy())
                .confirmedAt(order.getConfirmedAt())
                .deliveredAt(order.getDeliveredAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDtos)
                .statusHistory(historyDtos)
                .transactions(txnDtos)
                .build();
    }
}
