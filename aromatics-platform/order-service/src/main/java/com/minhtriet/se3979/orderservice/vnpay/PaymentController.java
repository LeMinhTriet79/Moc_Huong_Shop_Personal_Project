package com.minhtriet.se3979.orderservice.vnpay;

import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> createPaymentUrl(@PathVariable Long orderId, HttpServletRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        // VNPay quy định số tiền phải nhân thêm 100
        long amount = order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue();
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", order.getOrderCode()); // Mã hóa đơn
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
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

        // Thuật toán tạo chữ ký (Hash)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Build query
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

        // Trả về cái Link cho Frontend (React/Angular) chuyển hướng khách hàng
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    // 2. API Nhận Kết Quả Trả Về Từ VNPay
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // Xác thực chữ ký xem có đúng là VNPay gọi không (chống giả mạo)
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
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
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                // THANH TOÁN THÀNH CÔNG -> Cập nhật Database
                Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
                if(order != null) {
                    order.setPaymentStatus("PAID");
                    order.setPaymentMethod("VNPAY");
                    orderRepository.save(order);
                    log.info("💰 Đơn hàng {} đã thanh toán thành công qua VNPay!", orderCode);
                }
                return ResponseEntity.ok("Giao dịch thành công! Bạn có thể tắt trang này.");
            } else {
                return ResponseEntity.badRequest().body("Giao dịch thất bại / Bị hủy!");
            }
        } else {
            return ResponseEntity.badRequest().body("Chữ ký không hợp lệ (Dữ liệu bị hack)!");
        }
    }
}