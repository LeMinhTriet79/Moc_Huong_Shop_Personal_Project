# **TÀI LIỆU TỔNG QUAN KIẾN TRÚC & ĐẶC TẢ KỸ THUẬT TOÀN BỘ BACKEND — MỘC HƯƠNG SHOP (aromatics-platform)**

## **1\. Giới Thiệu Tổng Quan Dự Án & Kiến Trúc Hệ Thống**

Dự án **Mộc Hương Shop** ([Moc\_Huong\_Shop\_Personal\_Project](https://drive.google.com/drive/folders/1kRhQsQLfCRxmvkwVjMSCZHzis3JM1Klh)) là nền tảng Thương mại Điện tử Microservices chuyên kinh doanh các sản phẩm Trầm hương, Nhang sạch, Tinh dầu thiên nhiên, Vòng gỗ phong thủy và Lư xông trầm. Toàn bộ mã nguồn Backend nằm trong thư mục [aromatics-platform](https://drive.google.com/drive/folders/1VikEPRk4CatMNn9g-YNYoSuY7XHtthHv) được xây dựng trên nền tảng **Java 21** và **Spring Boot 3.2.5** kết hợp **Spring Cloud 2023.0.1**.

### **1.1. Sơ Đồ 6 Microservices & Cổng Giao Tiếp (Port Matrix)**

| Tên Microservice | Cổng (Port) | Cơ sở dữ liệu / Hạ tầng | Trách nhiệm cốt lõi |
| :---- | :---- | :---- | :---- |
| **service-discovery** | `8761` | In-Memory (Eureka Server) | Đăng ký và định vị dịch vụ nội bộ (`lb://IDENTITY-SERVICE`, `lb://CATALOG-SERVICE`, `lb://ORDER-SERVICE`, `lb://NOTIFICATION-SERVICE`) |
| **api-gateway** | `8080` | Stateless (Spring Cloud Gateway WebFlux) | Cổng vào duy nhất cho Frontend (`http://localhost:8080`), kiểm tra JWT thông qua `AuthenticationFilter`, trích xuất `userId`, `email`, `role` để gắn vào Header nội bộ `X-User-Id`, `X-User-Email`, `X-User-Role` |
| **identity-service** | `8081` | MySQL (`identity_db` \- cổng host `3307`), Kafka Producer | Quản lý Xác thực (JWT \+ Refresh Token Rotation, Google OAuth2, Verify Email, Forgot/Reset Password), Hồ sơ cá nhân, Sổ địa chỉ (`addresses`), Danh sách yêu thích (`wishlists`), Quản trị người dùng (`AdminUserController`) |
| **catalog-service** | `8082` | MySQL (`catalog_db` \- cổng host `3308`), Redis Cache (`6379`), Cloudinary, Kafka Consumer | Quản lý Danh mục (`categories`), Sản phẩm (`products`), Biến thể (`product_variants` kèm JSON `attributes`), Hình ảnh (`product_images`), Tồn kho (`inventories` với Pessimistic Write Lock & giữ chỗ `reservedQuantity`), Đánh giá sản phẩm (`reviews`) |
| **order-service** | `8083` | MySQL (`order_db` \- cổng host `3309`), Redis Cart (`6379`), OpenFeign, VNPay, iText7 PDF, Apache POI Excel, Kafka Producer | Quản lý Giỏ hàng Redis (`cart:user:{userId}`), Mã giảm giá (`coupons`, `coupon_usages`), Đặt hàng đồng bộ chống trùng (`idempotencyKey`), Vòng đời đơn hàng (State Machine), Lịch sử trạng thái (`order_status_history`), Thanh toán VNPay (`transactions`), Xuất Hóa đơn PDF, Xuất Báo cáo Excel, Thống kê KPI Dashboard |
| **notification-service** | `8084` | MySQL (`notification_db` \- cổng host `3310`), Spring Mail SMTP, Kafka Consumer, `@Scheduled` Retry | Lắng nghe sự kiện Kafka (`auth-notification-topic`, `order-events-topic`), quản lý mẫu thông báo động (`notification_templates`), lịch sử gửi thông báo chống trùng (`notification_logs` với `idempotencyKey`), tự động gửi lại (`retryFailedNotifications`) khi lỗi SMTP |

## **2\. Cơ Chế Bảo Mật & Phân Quyền 3 Vai Trò (Role-Based Access Control)**

Hệ thống áp dụng mô hình **Gateway Offloading kết hợp Internal Security Context Propagation**:

1. **Tại api-gateway (8080):** Khi Frontend gửi request kèm header `Authorization: Bearer <accessToken>`, `AuthenticationFilter` kiểm tra chữ ký HMAC-SHA256 của JWT, giải mã `userId`, `sub` (email) và `role`, đồng thời **xóa bỏ mọi header giả mạo từ bên ngoài** trước khi đính kèm 3 header nội bộ:  
- `X-User-Id`: ID định danh người dùng (`Long`)  
- `X-User-Email`: Địa chỉ email người dùng (`String`)  
- `X-User-Role`: Quyền hạn (`CUSTOMER`, `STAFF`, `ADMIN`)  
2. **Tại các Downstream Services (identity-service, catalog-service, order-service):**  
- Class `InternalAuthFilter` ([SecurityConfig.java](https://drive.google.com/file/d/1kVnaIq1-GF23cQGHFIzMSLffA6b8wq8e/view?usp=drivesdk)) đọc `X-User-Id` và `X-User-Role`, khởi tạo `UsernamePasswordAuthenticationToken` với `SimpleGrantedAuthority("ROLE_" + role)` nạp vào `SecurityContextHolder`.  
- Class tiện ích [SecurityUtils.java](https://drive.google.com/file/d/12Ee0Dae7g3l_qYAGavdszkS5Mliw785d/view?usp=drivesdk) cung cấp `SecurityUtils.getCurrentUserId()`, `SecurityUtils.getCurrentUserEmail()`, `SecurityUtils.getCurrentUserRole()` để các Controller lấy đúng danh tính người dùng từ Token mà không bao giờ tin tưởng tham số `userId` truyền từ client.  
3. **Ma trận 3 Role trong hệ thống:**  
- **`CUSTOMER`**: Xem sản phẩm, quản lý giỏ hàng Redis, áp mã giảm giá, đặt hàng (COD/VNPay), xem lịch sử đơn, hủy đơn (`PENDING`/`CONFIRMED`), yêu cầu trả hàng (`DELIVERED`), tải hóa đơn PDF của chính mình, quản lý Sổ địa chỉ, Wishlist, gửi đánh giá sản phẩm.  
- **`STAFF`**: Toàn bộ quyền vận hành cửa hàng: Xem thống kê Dashboard, lọc/xem toàn bộ đơn hàng, chuyển trạng thái đơn hàng theo State Machine, ghi chú nội bộ (`staffNotes`), xuất báo cáo Excel, thêm/sửa/ẩn/khôi phục Sản phẩm & Danh mục, quản lý Biến thể, cập nhật Tồn kho, xem cảnh báo sắp hết hàng (`low-stock`), phản hồi và ẩn/hiện Đánh giá của khách hàng.  
- **`ADMIN`**: Kế thừa toàn bộ quyền của `STAFF`, cộng thêm quyền quản trị tối cao: Quản lý người dùng ([AdminUserController.java](https://drive.google.com/file/d/1LBkdGZxEYCy8fS7Pc5JBABjRaSPc-SXt/view?usp=drivesdk) \- đổi Role, khóa/mở khóa tài khoản và thu hồi toàn bộ Refresh Token), Quản lý Mã giảm giá ([AdminCouponController.java](https://drive.google.com/file/d/1blDR4TAo15JpLdYR_1ndfY0Dqd7OlyvR/view?usp=drivesdk) \- tạo/sửa/xóa/bật/tắt Coupon), và xóa vĩnh viễn sản phẩm khỏi DB (`DELETE /api/catalog/products/{id}/force`).

## **3\. Chuẩn Hóa Dữ Liệu Trả Về (ApiResponse\<T\>)**

Tất cả các REST Controller trong toàn bộ hệ thống đều bọc kết quả trả về trong cấu trúc `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Thông báo kết quả bằng tiếng Việt rõ ràng",
  "data": { ... },
  "errorCode": null,
  "timestamp": "2026-10-26T23:30:00"
}
```

## **4\. Đặc Tả Chi Tiết Từng Microservice, Schema DB & Danh Mục API**

### **4.1. Identity Service (identity-service \- Port 8081, DB identity\_db)**

4. **Các Bảng Cơ Sở Dữ Liệu (Entities):**  
- `users`: Lưu `id`, `email` (unique), `password_hash`, `full_name`, `phone_number`, `avatar_url`, `date_of_birth`, `role` (`CUSTOMER`, `STAFF`, `ADMIN`), `provider` (`LOCAL`, `GOOGLE`), `provider_id`, `is_email_verified`, `is_active`, `last_login_at`.  
- `refresh_tokens`: Quản lý phiên đăng nhập dài hạn với cơ chế **Refresh Token Rotation** (`token` unique, `device_info`, `ip_address`, `is_revoked`, `expires_at`).  
- `email_verifications`: Lưu mã xác thực email và đặt lại mật khẩu (`token`, `type` \= `VERIFY_EMAIL` / `RESET_PASSWORD`, `expires_at`, `is_used`).  
- `addresses`: Sổ địa chỉ nhận hàng của khách (`recipient_name`, `phone_number`, `province`, `district`, `ward`, `street_address`, `is_default`). Đảm bảo mỗi user chỉ có tối đa 1 địa chỉ mặc định ([AddressServiceImpl.java](https://drive.google.com/file/d/1jLtaOu-5_yve7VV4Oa4X12H7KGbUwWW_/view?usp=drivesdk)).  
- `wishlists`: Danh sách sản phẩm yêu thích (`user_id`, `product_id`, unique constraint `(user_id, product_id)`) ([WishlistServiceImpl.java](https://drive.google.com/file/d/177czE4b9R_mZmj-pRiHFJiuB_JENC9SC/view?usp=drivesdk)).  
5. **Danh sách Endpoints:**  
- [AuthController.java](https://drive.google.com/file/d/18wjvRlKUa9f2C9huO3xcXz8tZdwoNo4m/view?usp=drivesdk) (`/api/identity/auth`):  
  1. `POST /register`: Đăng ký tài khoản mới, tạo token xác thực và bắn sự kiện Kafka [AuthNotificationEvent.java](https://drive.google.com/file/d/10_5PzRRZJ8uqqZhsvwV5AGlJlctzHprU/view?usp=drivesdk).  
     2. `POST /login`: Đăng nhập, trả về `accessToken`, `refreshToken`, `userId`, `email`, `fullName`, `role`.  
     3. `POST /refresh`: Xoay vòng Refresh Token cũ và cấp cặp Token mới.  
     4. `POST /logout`: Thu hồi Refresh Token hiện tại.  
     5. `GET /verify-email?token=...`: Kích hoạt xác thực email.  
     6. `POST /forgot-password` & `POST /reset-password`: Luồng quên và đặt lại mật khẩu qua email.  
     7. `POST /google`: Đăng nhập/Đăng ký một chạm qua Google OAuth2.  
- [UserController.java](https://drive.google.com/file/d/17u_6tzmL6SWr1Am_BPP-QgZje46PXzIO/view?usp=drivesdk) (`/api/identity/users`):  
  1. `GET /me`: Xem thông tin hồ sơ của chính mình.  
     2. `PUT /me`: Cập nhật họ tên, số điện thoại, ngày sinh, ảnh đại diện.  
     3. `PUT /me/password`: Đổi mật khẩu và tự động thu hồi toàn bộ Refresh Token cũ.  
     4. `GET /internal/{userId}`: API nội bộ cho Feign Client.  
- [AddressController.java](https://drive.google.com/file/d/1TXqCsmvKPuCgOOCoYtAo3JUdKQbOsUni/view?usp=drivesdk) (`/api/identity/users/me/addresses`):  
  1. `GET /`, `POST /`, `PUT /{id}`, `PUT /{id}/default`, `DELETE /{id}`.  
- [WishlistController.java](https://drive.google.com/file/d/1aDjbK1wDDXPTO1p9HRdLiC0FAGnOfIAa/view?usp=drivesdk) (`/api/identity/users/me/wishlists`):  
  1. `GET /`, `POST /{productId}`, `DELETE /{productId}`, `GET /{productId}/check`.  
- [AdminUserController.java](https://drive.google.com/file/d/1LBkdGZxEYCy8fS7Pc5JBABjRaSPc-SXt/view?usp=drivesdk) (`/api/identity/admin/users` \- `ROLE_ADMIN`):  
  1. `GET /`: Tìm kiếm & phân trang người dùng theo `keyword`, `role`, `isActive`.  
     2. `GET /{id}`: Xem chi tiết người dùng kèm sổ địa chỉ.  
     3. `PUT /{id}/role`: Thay đổi Role (chặn Admin tự hạ quyền chính mình).  
     4. `PUT /{id}/status`: Khóa/Mở khóa tài khoản (tự động thu hồi mọi phiên đăng nhập khi khóa).

### **4.2. Catalog Service (catalog-service \- Port 8082, DB catalog\_db, Redis Cache)**

6. **Các Bảng Cơ Sở Dữ Liệu (Entities):**  
- `categories`: Danh mục sản phẩm phân cấp (`parent_id`, `name`, `slug`, `description`, `image_url`, `is_active`).  
- `products`: Sản phẩm gốc (`category_id`, `name`, `slug`, `sku_prefix`, `description`, `brand`, `base_price`, `attributes` JSON như mùi hương, thời gian cháy, nguồn gốc trầm, `is_active`).  
- `product_variants`: Biến thể bán hàng thực tế (`product_id`, `sku` unique, `variant_name`, `price`, `weight_grams`, `attributes` JSON như kích cỡ hạt, số lượng nụ, `is_active`).  
- `product_images`: Bộ sưu tập ảnh Cloudinary của sản phẩm (`image_url`, `alt_text`, `display_order`, `is_primary`).  
- `inventories`: Quản lý tồn kho theo từng `variant_id` (`quantity`, `reserved_quantity`, `low_stock_threshold`). Sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` khi trừ/cộng kho để chống Race Condition.  
- `reviews`: Đánh giá 1–5 sao từ khách hàng đã mua (`product_id`, `user_id`, `order_id`, `rating`, `comment`, `reply_message`, `replied_at`, `is_verified_purchase`, `is_visible`).  
7. **Danh sách Endpoints:**  
- [CategoryController.java](https://drive.google.com/file/d/1QMXqp_yXjUpJsvc1Q4xPRjVyB2ZXA1dn/view?usp=drivesdk) (`/api/catalog/categories`):  
  1. `GET /`, `GET /inactive`, `GET /{id}`, `POST /` (Multipart), `PUT /{id}` (Multipart), `DELETE /{id}`.  
- [ProductController.java](https://drive.google.com/file/d/1b6p0bEGLOTzgvNQrc7Vn0F_aAUmFRKuk/view?usp=drivesdk) (`/api/catalog/products`):  
  1. `GET /`: Lọc đa tầng theo `keyword`, `categoryId`, `brand`, `minPrice`, `maxPrice`, phân trang & sắp xếp.  
     2. `GET /featured`: Top 8 sản phẩm nổi bật (lưu Redis Cache).  
     3. `GET /inactive`: Danh sách sản phẩm trong thùng rác (`STAFF`, `ADMIN`).  
     4. `GET /{id}`: Chi tiết sản phẩm kèm toàn bộ biến thể, hình ảnh, tồn kho và điểm đánh giá trung bình (lưu Redis Cache `products`).  
     5. `POST /` & `PUT /{id}`: Tạo/Cập nhật sản phẩm đa biến thể kèm upload nhiều ảnh lên Cloudinary (`STAFF`, `ADMIN`).  
     6. `DELETE /{id}` (xóa mềm), `PUT /{id}/restore` (khôi phục), `DELETE /{id}/force` (xóa vĩnh viễn \- chỉ `ADMIN`).  
- [ProductVariantController.java](https://drive.google.com/file/d/17IgPb10mJdAdCs-rRx7-TTcRRAgQnwLX/view?usp=drivesdk) (`/api/catalog/products`):  
  1. `GET /{productId}/variants`, `POST /{productId}/variants`, `PUT /{productId}/variants/{variantId}`, `DELETE /{productId}/variants/{variantId}`.  
     2. API nội bộ cho Order Service Feign Client: `GET /variants/{variantId}/internal` và `POST /variants/batch-info`.  
- [InventoryController.java](https://drive.google.com/file/d/1eRzV6AkzzvODHpWe4k_mrw98xrHmRzF8/view?usp=drivesdk) (`/api/catalog/inventory`):  
  1. `GET /variant/{variantId}`: Xem số lượng tồn kho khả dụng (`availableStock`).  
     2. `PUT /variant/{variantId}`: Cập nhật số lượng kho và ngưỡng cảnh báo (`STAFF`, `ADMIN`).  
     3. `GET /low-stock`: Danh sách biến thể chạm ngưỡng sắp hết hàng (`STAFF`, `ADMIN`).  
     4. `POST /deduct` & `POST /restore`: Trừ kho và hoàn kho đồng bộ khi đặt/hủy đơn.  
- [ReviewController.java](https://drive.google.com/file/d/1IDS--Bytm3BoYL5VzBI-P_VpJZ1TYElP/view?usp=drivesdk) (`/api/catalog/reviews`):  
  1. `POST /`: Khách hàng gửi đánh giá (lấy `userId` từ `SecurityUtils.getCurrentUserId()`).  
     2. `GET /product/{productId}`: Xem danh sách đánh giá hiển thị của sản phẩm.  
     3. `GET /admin/reviews`, `PUT /{reviewId}/reply`, `PUT /{reviewId}/toggle-visibility` (`STAFF`, `ADMIN`).

### **4.3. Order Service (order-service \- Port 8083, DB order\_db, Redis Cart)**

8. **Các Bảng Cơ Sở Dữ Liệu (Entities):**  
- `orders`: Đơn hàng (`order_code` unique, `idempotency_key` unique, `user_id`, `status`, `currency`, `subtotal`, `shipping_fee`, `discount_amount`, `total_amount`, `coupon_code`, `payment_method`, `payment_status`, `shipping_address` JSON, `tracking_number`, `carrier`, `notes`, `staff_notes`, `cancel_reason`).  
- `order_items`: Chi tiết từng dòng sản phẩm trong đơn (`product_id`, `variant_id`, `sku`, `product_name`, `variant_name`, `product_image_url`, `unit_price`, `discount_amount`, `quantity`, `total_price`).  
- `order_status_history`: Lưu vết toàn bộ lịch sử chuyển trạng thái đơn (`from_status`, `to_status`, `changed_by`, `changed_by_role`, `note`, `created_at`).  
- `coupons` & `coupon_usages`: Quản lý mã khuyến mãi (`PERCENTAGE`, `FIXED_AMOUNT`, `FREE_SHIPPING`, `min_order_value`, `max_discount_amount`, `usage_limit`, `usage_per_user`) ([CouponServiceImpl.java](https://drive.google.com/file/d/10lw3cIp3SeuGZnP5I7GMzYa3k-YU8hHm/view?usp=drivesdk)).  
- `transactions`: Giao dịch thanh toán VNPay / COD (`transaction_reference`, `gateway_transaction_no`, `amount`, `status`, `bank_code`, `card_type`, `raw_Notify_payload` JSON).  
9. **Danh sách Endpoints:**  
- [CartController.java](https://drive.google.com/file/d/1F9Ju7miKTDuNFe3ftJiE-qgB-kV7lVic/view?usp=drivesdk) (`/api/order/cart`):  
  1. `GET /`, `POST /add` (gọi Feign sang Catalog Service lấy giá thật và kiểm tra tồn kho trong [CartServiceImpl.java](https://drive.google.com/file/d/1I_u9ls3_1NP4wi1rg_GqMXcWyDWxrnvt/view?usp=drivesdk)), `PUT /update`, `DELETE /remove/{productId}/{variantId}`, `DELETE /clear`.  
- [CouponController.java](https://drive.google.com/file/d/1AQGQwK_eOF29oMkKXwQRjtwRQRJkg3Yz/view?usp=drivesdk) (`/api/order/coupons`) & [AdminCouponController.java](https://drive.google.com/file/d/1blDR4TAo15JpLdYR_1ndfY0Dqd7OlyvR/view?usp=drivesdk) (`/api/order/admin/coupons`):  
  1. `GET /active`, `POST /preview` (tính thử số tiền giảm), và đầy đủ CRUD Coupon cho Admin.  
- [OrderController.java](https://drive.google.com/file/d/1Ob0Mg-9B9yui1-uVPxhYrJ77wEK8kJgs/view?usp=drivesdk) (`/api/order/orders`):  
  1. `POST /checkout`: Đặt hàng có kiểm tra `idempotencyKey`, trừ kho qua Feign (tự động bù trừ hoàn kho `compensateDeductions` nếu 1 món trong giỏ hết hàng), áp mã giảm giá, lưu lịch sử trạng thái và bắn Kafka `OrderCreatedEvent` ([OrderServiceImpl.java](https://drive.google.com/file/d/1IkWb9VlMNGWjoqMS3kyqHOPdJK8YBcQ9/view?usp=drivesdk)).  
     2. `GET /`: Lịch sử đơn hàng của khách.  
     3. `GET /{orderCode}`: Xem chi tiết đơn hàng kèm `items`, `statusHistory`, `transactions`.  
     4. `PUT /{orderId}/cancel`: Khách hủy đơn khi đang ở `PENDING` hoặc `CONFIRMED` (tự động hoàn kho và hoàn lượt dùng Coupon).  
     5. `POST /{orderId}/return-request`: Yêu cầu trả hàng khi đơn ở `DELIVERED`.  
     6. `GET /{orderId}/invoice-pdf`: Xuất hóa đơn PDF chuyên nghiệp bằng `iText7` ([OrderExportServiceImpl.java](https://drive.google.com/file/d/1rTsz99ZKPFfGr2IMNdfRcGYHggTqm-KJ/view?usp=drivesdk)).  
- [AdminOrderController.java](https://drive.google.com/file/d/1GY8qrvl5nH-9bLtlSyhP5fW-AVKOz-DA/view?usp=drivesdk) (`/api/order/admin/orders` \- `STAFF`, `ADMIN`):  
  1. `GET /`: Tìm kiếm & lọc đơn hàng đa tiêu chí (`keyword`, `status`, `paymentStatus`, `paymentMethod`, `startDate`, `endDate`).  
     2. `GET /{orderId}`: Xem chi tiết đơn hàng.  
     3. `PUT /{orderId}/status`: Chuyển trạng thái đơn hàng theo State Machine (`PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED`).  
     4. `PUT /{orderId}/staff-notes`: Lưu ghi chú nội bộ của nhân viên.  
     5. `GET /export-excel`: Xuất báo cáo doanh thu & đơn hàng ra file `.xlsx` bằng Apache POI.  
     6. `GET /statistics`: Thống kê tổng doanh thu, số đơn theo trạng thái, doanh thu theo ngày và Top sản phẩm bán chạy.  
- [PaymentController.java](https://drive.google.com/file/d/106J9oDPwu5JqeTYNc0ccWGZjWTCdzhef/view?usp=drivesdk) (`/api/order/payment`):  
  1. `GET /create-payment`: Tạo URL thanh toán VNPay.  
     2. `GET /vnpay-callback`: Xác thực chữ ký `vnp_SecureHash`, lưu bảng `transactions` và cập nhật trạng thái đơn hàng.

### **4.4. Notification Service (notification-service \- Port 8084, DB notification\_db)**

10. **Các Bảng Cơ Sở Dữ Liệu (Entities):**  
- `notification_templates` ([NotificationTemplate.java](https://drive.google.com/file/d/1pBqzyTjgVoFsFS3sGmh0-i2K7paiLuHk/view?usp=drivesdk)): Lưu mẫu email HTML (`ORDER_CREATED_EMAIL`, `ORDER_STATUS_CHANGED_EMAIL`, `ORDER_CANCELLED_EMAIL`, `VERIFY_EMAIL`, `RESET_PASSWORD`).  
- `notification_logs` ([NotificationLog.java](https://drive.google.com/file/d/1ceTub2S66jm9vOLPOatyqEBEMTcuzTgo/view?usp=drivesdk)): Lưu `idempotency_key` chống gửi trùng khi Kafka re-balance, trạng thái `PENDING`/`SENT`/`FAILED`, `retry_count` và `next_retry_at`.  
11. **Cơ chế vận hành:**  
- [NotificationDispatcherService.java](https://drive.google.com/file/d/1BjJjJ-xKs0tqy8xkT4AYfegm-BWeA2XW/view?usp=drivesdk) kiểm tra `idempotencyKey`, nạp mẫu HTML từ DB, thay thế các biến `{{fullName}}`, `{{orderCode}}`, `{{totalAmount}}`, gửi mail qua SMTP và tự động chạy `@Scheduled(fixedDelay = 60000)` để thử lại tối đa 3 lần nếu mạng hoặc SMTP gặp sự cố.

## **5\. Hướng Dẫn Khởi Chạy Toàn Bộ Hệ Thống Backend**

12. **Khởi chạy hạ tầng (MySQL, Redis, Kafka, Zookeeper):**

```sh
cd aromatics-platform
docker compose -f docker-compose.infra.yml up -d
```

13. **Biên dịch và chạy kiểm thử toàn bộ 6 Microservices:**

```sh
mvn clean test
```

14. **Khởi chạy các Microservices (theo thứ tự: service-discovery \-\> api-gateway \-\> identity-service, catalog-service, order-service, notification-service hoặc qua Docker Compose):**

```sh
docker compose -f docker-compose.infra.yml -f docker-compose.services.yml up -d --build
```

