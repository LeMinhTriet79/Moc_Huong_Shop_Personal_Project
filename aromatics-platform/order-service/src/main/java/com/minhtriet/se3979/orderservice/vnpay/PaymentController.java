package com.minhtriet.se3979.orderservice.vnpay;

import com.minhtriet.se3979.orderservice.dto.response.ApiResponse;
import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.repository.OrderRepository;
import com.minhtriet.se3979.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/order/payment")
@RequiredArgsConstructor
public class PaymentController {

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

    // 1. API Tạo Link Thanh Toán
    @GetMapping("/create-url/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPaymentUrl(
            @PathVariable Long orderId, HttpServletRequest request) {
        return buildVNPayUrl(orderId, null, null, request);
    }

    @GetMapping("/create-payment")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPayment(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String orderInfo,
            HttpServletRequest request) {
        return buildVNPayUrl(orderId, amount, orderInfo, request);
    }

    private ResponseEntity<ApiResponse<Map<String, String>>> buildVNPayUrl(
            Long orderId, BigDecimal amountParam, String orderInfoParam, HttpServletRequest request) {

        String txnRef;
        long amountVal;
        String info;

        if (orderId != null) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
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

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountVal));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", info);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Hết hạn sau 15 phút
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
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
        String paymentUrl = vnp_PayUrl + "?" + queryUrl;

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("paymentUrl", paymentUrl), "Tạo link thanh toán VNPay thành công"));
    }

    // 2. API Nhận Kết Quả Trả Về Từ VNPay
    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<String>> vnpayReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Map<String, Object> rawResponse = new HashMap<>();

        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                fields.put(fieldName, fieldValue);
                rawResponse.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

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

        if (signValue.equals(vnp_SecureHash)) {
            String orderCode = request.getParameter("vnp_TxnRef");
            String responseCode = request.getParameter("vnp_ResponseCode");
            String transactionNo = request.getParameter("vnp_TransactionNo");
            String transactionCode = "TXN-" + orderCode;
            String amountStr = request.getParameter("vnp_Amount");
            BigDecimal amount = amountStr != null ? new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

            if ("00".equals(responseCode)) {
                // THANH TOÁN THÀNH CÔNG
                orderService.handlePaymentSuccess(
                        orderCode, transactionCode, transactionNo, amount, responseCode, "Thành công", rawResponse);
                return ResponseEntity.ok(ApiResponse.success(
                        "Giao dịch thành công! Mã đơn: " + orderCode, "Thanh toán thành công"));
            } else {
                // THANH TOÁN THẤT BẠI / BỊ HỦY -> TỰ ĐỘNG CHUYỂN CANCELLED & HOÀN KHO
                orderService.handlePaymentFailure(
                        orderCode, transactionCode, transactionNo, amount, responseCode, "Giao dịch không thành công", rawResponse);
                return ResponseEntity.badRequest().body(ApiResponse.error(
                        "Giao dịch thất bại hoặc bị hủy (Code: " + responseCode + ")"));
            }
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Chữ ký không hợp lệ!"));
        }
    }
}