package com.minhtriet.se3979.orderservice.service.impl;

import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.exception.AppException;
import com.minhtriet.se3979.orderservice.repository.OrderRepository;
import com.minhtriet.se3979.orderservice.security.SecurityUtils;
import com.minhtriet.se3979.orderservice.service.OrderService;
import com.minhtriet.se3979.orderservice.service.VNPayService;
import com.minhtriet.se3979.orderservice.vnpay.VNPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String secretKey;

    @Value("${vnpay.pay-url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNPAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String createPaymentUrl(Long orderId, BigDecimal amountParam, String orderInfoParam, String ipAddress) {
        String txnRef;
        long amountVal;
        String info;

        if (orderId != null) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với id: " + orderId));

            // Vá IDOR: Kiểm tra quyền sở hữu đơn hàng
            SecurityUtils.getCurrentUser().ifPresent(currentUser -> {
                String role = currentUser.getRole();
                boolean isPrivileged = "ADMIN".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role);
                if (!isPrivileged && (order.getUserId() == null || !order.getUserId().equals(currentUser.getUserId()))) {
                    throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền thanh toán đơn hàng này!");
                }
            });

            txnRef = order.getOrderCode();
            BigDecimal actualAmount = (amountParam != null && amountParam.compareTo(BigDecimal.ZERO) > 0)
                    ? amountParam : order.getTotalAmount();
            amountVal = actualAmount.multiply(BigDecimal.valueOf(100)).longValue();
            info = (orderInfoParam != null && !orderInfoParam.isBlank())
                    ? orderInfoParam : "Thanh toan don hang " + order.getOrderCode();
        } else {
            txnRef = "MH" + System.currentTimeMillis();
            BigDecimal actualAmount = (amountParam != null && amountParam.compareTo(BigDecimal.ZERO) > 0)
                    ? amountParam : BigDecimal.valueOf(100000);
            amountVal = actualAmount.multiply(BigDecimal.valueOf(100)).longValue();
            info = (orderInfoParam != null && !orderInfoParam.isBlank())
                    ? orderInfoParam : "Thanh toan don hang " + txnRef;
        }

        ZonedDateTime now = ZonedDateTime.now(VN_ZONE);
        String vnp_CreateDate = now.format(VNPAY_DATE_FORMATTER);
        String vnp_ExpireDate = now.plusMinutes(15).format(VNPAY_DATE_FORMATTER);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountVal));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", info);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", (ipAddress != null && !ipAddress.isBlank()) ? ipAddress : "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnp_PayUrl + "?" + queryUrl;
    }

    @Override
    public Map<String, Object> processReturn(Map<String, String> requestParams) {
        Map<String, String> fields = new HashMap<>(requestParams);
        Map<String, Object> rawResponse = new HashMap<>(requestParams);

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String signValue = VNPayUtil.hmacSHA512(secretKey, hashData.toString());
        if (!signValue.equalsIgnoreCase(vnp_SecureHash)) {
            return Map.of("success", false, "message", "Chữ ký không hợp lệ!");
        }

        String orderCode = requestParams.get("vnp_TxnRef");
        String responseCode = requestParams.get("vnp_ResponseCode");
        String transactionNo = requestParams.get("vnp_TransactionNo");
        String transactionCode = "TXN-" + orderCode;
        String amountStr = requestParams.get("vnp_Amount");
        BigDecimal amount = amountStr != null
                ? new BigDecimal(amountStr).divide(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        if ("00".equals(responseCode)) {
            orderService.handlePaymentSuccess(
                    orderCode, transactionCode, transactionNo, amount, responseCode, "Thành công", rawResponse);
            return Map.of(
                    "success", true,
                    "orderCode", orderCode,
                    "message", "Giao dịch thành công! Mã đơn: " + orderCode
            );
        } else {
            orderService.handlePaymentFailure(
                    orderCode, transactionCode, transactionNo, amount, responseCode, "Giao dịch không thành công", rawResponse);
            return Map.of(
                    "success", false,
                    "orderCode", orderCode,
                    "responseCode", responseCode,
                    "message", "Giao dịch thất bại hoặc bị hủy (Code: " + responseCode + ")"
            );
        }
    }
}
