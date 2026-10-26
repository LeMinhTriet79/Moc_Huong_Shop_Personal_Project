package com.minhtriet.se3979.orderservice.service;

import com.minhtriet.se3979.orderservice.dto.request.CheckoutRequest;
import com.minhtriet.se3979.orderservice.dto.request.DeductRequest;
import com.minhtriet.se3979.orderservice.dto.response.DeductResult;
import com.minhtriet.se3979.orderservice.dto.response.OrderResponse;
import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.entity.OrderStatusHistory;
import com.minhtriet.se3979.orderservice.entity.Transaction;
import com.minhtriet.se3979.orderservice.exception.AppException;
import com.minhtriet.se3979.orderservice.feign.CatalogFeignClient;
import com.minhtriet.se3979.orderservice.redis.CartItem;
import com.minhtriet.se3979.orderservice.repository.OrderItemRepository;
import com.minhtriet.se3979.orderservice.repository.OrderRepository;
import com.minhtriet.se3979.orderservice.repository.OrderStatusHistoryRepository;
import com.minhtriet.se3979.orderservice.repository.TransactionRepository;
import com.minhtriet.se3979.orderservice.security.AuthenticatedUser;
import com.minhtriet.se3979.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private CartService cartService;

    @Mock
    private CouponService couponService;

    @Mock
    private CatalogFeignClient catalogFeignClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CheckoutRequest sampleCheckoutRequest;
    private CartItem sampleCartItem;

    @BeforeEach
    void setUp() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId(1L)
                .email("triet@mochuong.vn")
                .fullName("Lê Minh Triết")
                .role("CUSTOMER")
                .build();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        sampleCheckoutRequest = CheckoutRequest.builder()
                .shippingAddress(Map.of(
                        "recipientName", "Lê Minh Triết",
                        "phoneNumber", "0901234567",
                        "addressLine", "123 Nguyễn Huệ, Quận 1, TP. HCM"
                ))
                .paymentMethod("COD")
                .idempotencyKey("IDEMP-KEY-999")
                .build();

        sampleCartItem = CartItem.builder()
                .variantId(1L)
                .productId(10L)
                .productName("Nhang Trầm Hương Cao Cấp")
                .variantName("Hộp 50 cây")
                .sku("MH-NHANG-50")
                .price(new BigDecimal("250000.00"))
                .quantity(2)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Đặt hàng thất bại khi giỏ hàng trống")
    void testCheckout_EmptyCart_ThrowsBadRequest() {
        when(cartService.getCart(1L)).thenReturn(Collections.emptyList());

        AppException ex = assertThrows(AppException.class, () -> orderService.checkout(1L, sampleCheckoutRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Giỏ hàng của bạn đang trống"));
    }

    @Test
    @DisplayName("Đặt hàng thất bại khi kho không đủ hàng")
    void testCheckout_OutOfStock_ThrowsConflict() {
        when(cartService.getCart(1L)).thenReturn(List.of(sampleCartItem));

        DeductResult deductFail = new DeductResult();
        deductFail.setSuccess(false);
        deductFail.setMessage("Kho không đủ hàng");

        when(catalogFeignClient.deductInventory(any(DeductRequest.class)))
                .thenReturn(ResponseEntity.ok(deductFail));

        AppException ex = assertThrows(AppException.class, () -> orderService.checkout(1L, sampleCheckoutRequest));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertTrue(ex.getMessage().contains("không đủ số lượng"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Đặt hàng COD thành công khi kho còn đủ")
    void testCheckout_Success_COD() {
        when(cartService.getCart(1L)).thenReturn(List.of(sampleCartItem));

        DeductResult deductSuccess = new DeductResult();
        deductSuccess.setSuccess(true);

        when(catalogFeignClient.deductInventory(any(DeductRequest.class)))
                .thenReturn(ResponseEntity.ok(deductSuccess));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L);
            o.setItems(new ArrayList<>());
            return o;
        });

        when(orderItemRepository.saveAll(any())).thenReturn(Collections.emptyList());
        when(orderStatusHistoryRepository.save(any(OrderStatusHistory.class))).thenReturn(new OrderStatusHistory());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        OrderResponse response = orderService.checkout(1L, sampleCheckoutRequest);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("COD", response.getPaymentMethod());
        assertEquals("CONFIRMED", response.getStatus());

        verify(cartService, times(1)).clearCart(1L);
        verify(kafkaTemplate, times(1)).send(eq("order.created"), any());
    }
}
