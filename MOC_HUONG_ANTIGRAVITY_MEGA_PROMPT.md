# SIÊU PROMPT TOÀN DIỆN (MEGA-PROMPT) HOÀN THIỆN 100% HỆ THỐNG MICROSERVICES MỘC HƯƠNG SHOP (`aromatics-platform`)

> **Hướng dẫn sử dụng trên Antigravity / Cursor / Windsurf:**  
> 

> 1. Đầu tiên, nạp file `MOC_HUONG_AI_AGENT_SKILL_RULES.md` vào phần Rules / System Instructions (hoặc đính kèm vào đầu phiên làm việc).  
> 2. Dự án đã được chia thành **7 Chặng (Phases) với 20 Tasks chi tiết** bám sát 100% source code thực tế (`com.minhtriet.se3979.*`) và Database Schema MySQL 8.0 (`V1__init_*.sql` đến `V4__*.sql`).  
> 3. Bạn có thể copy toàn bộ Siêu Prompt bên dưới hoặc copy từng **Phase (Chặng)** ném vào Antigravity và gõ lệnh: `"Hãy thực thi tuần tự từng Task trong Phase này, tạo mới/cập nhật đầy đủ các file Java và chạy mvn clean test để nghiệm thu."`

---

## PHẦN 1: BÁO CÁO KIỂM TOÁN KIẾN TRÚC & BẢNG PHÂN QUYỀN (ROLES MATRIX)

Qua kiểm toán trực tiếp toàn bộ mã nguồn trong thư mục [Mộc Hương](https://drive.google.com/drive/folders/1kRhQsQLfCRxmvkwVjMSCZHzis3JM1Klh) ([aromatics-platform](https://drive.google.com/drive/folders/1NutdcUf7ZWi6q7n_-VYkQwZ9pNpFsKwW)) và đối chiếu với [PROJECT\_STATUS.md](https://drive.google.com/file/d/1hiHNLaPgAffMghB-yyM4kOjLpRpgFiqR/view?usp=drivesdk), dưới đây là **Sự thật kỹ thuật (Ground Truth)** của dự án:

### 1.1. Sự thật về Hệ thống Phân Quyền (Có bao nhiêu ROLE trong dự án?)

Trong câu hỏi của em có nhắc là *"quên luôn có bao nhiêu ROLE trong đây"*, thầy đã kiểm tra trực tiếp file [Role.java](https://drive.google.com/file/d/1gArpdfPzyfKmKq6j0YejQhGpU-wk1vnP/view?usp=drivesdk), [V1\_\_init\_identity\_db.sql](https://drive.google.com/file/d/1qL60OaS2NVrZIR10vk0w-IIC_3Ebytpr/view?usp=drivesdk), và [V1\_\_init\_order\_db.sql](https://drive.google.com/file/d/1MfC8bzg8f5N-2_sDE1sFAU5f1O-4g6wi/view?usp=drivesdk) (chứ không bị đánh lừa bởi tài liệu `PROJECT_STATUS.md` ghi nhầm là `USER | ADMIN`). Hệ thống của em có chính xác **3 User Roles** và **1 System Actor**:

| Mã Role / Actor | Nơi khai báo trong Source Code | Ý nghĩa & Phạm vi Quyền hạn (RBAC) |
| :---- | :---- | :---- |
| **`CUSTOMER`** | `enum Role` ([Role.java](https://drive.google.com/file/d/1gArpdfPzyfKmKq6j0YejQhGpU-wk1vnP/view?usp=drivesdk)) & `users.role` ([V1\_\_init\_identity\_db.sql](https://drive.google.com/file/d/1qL60OaS2NVrZIR10vk0w-IIC_3Ebytpr/view?usp=drivesdk)) | **Khách hàng mua sắm** (Role mặc định khi đăng ký trong [AuthServiceImpl.java](https://drive.google.com/file/d/1Vgbu3FMVKTLGFIE00nldjdld43t9eNVZ/view?usp=drivesdk)). Quyền: Xem danh mục/sản phẩm, quản lý Profile, Sổ địa chỉ (`user_addresses`), Danh sách yêu thích (`wishlists`), Giỏ hàng Redis, Áp mã giảm giá (`coupons`), Đặt hàng (COD/VNPay/MoMo), Xem lịch sử đơn hàng, Hủy đơn khi đang `PENDING_PAYMENT`, Yêu cầu trả hàng (`RETURN_REQUESTED`), Viết/Sửa/Xóa đánh giá kèm ảnh Cloudinary cho sản phẩm đã mua (`order_item_id`). |
| **`STAFF`** | `enum Role` ([Role.java](https://drive.google.com/file/d/1gArpdfPzyfKmKq6j0YejQhGpU-wk1vnP/view?usp=drivesdk)), `@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")` trong [ProductController.java](https://drive.google.com/file/d/1g6JhKBC0igaUN4CkJny6WD53e_qxsxDT/view?usp=drivesdk) & [CategoryController.java](https://drive.google.com/file/d/12uNzlvPC58dkCeC_DgySh1iHousg65eA/view?usp=drivesdk) | **Nhân viên vận hành cửa hàng**. Quyền: Thêm/Sửa/Xóa mềm/Khôi phục Danh mục & Sản phẩm \+ Biến thể (`product_variants`), Nhập kho/Điều chỉnh tồn kho (`inventory`), Xem danh sách đơn hàng toàn hệ thống, Xác nhận & Cập nhật trạng thái đơn hàng (`CONFIRMED` \-\> `PREPARING` \-\> `SHIPPING` \-\> `DELIVERED` \-\> `COMPLETED`), Ghi chú nội bộ (`staff_notes`), Trả lời đánh giá của khách (`admin_reply`) và Ẩn/Hiện đánh giá vi phạm (`is_visible`). |
| **`ADMIN`** | `enum Role` ([Role.java](https://drive.google.com/file/d/1gArpdfPzyfKmKq6j0YejQhGpU-wk1vnP/view?usp=drivesdk)), [SecurityConfig.java](https://drive.google.com/file/d/1aOTb3trz2_YQk74kvl1xP3HO3mYF9JQd/view?usp=drivesdk) | **Chủ cửa hàng / Quản trị viên tối cao**. Có toàn bộ quyền của `STAFF`, cộng thêm: Quản lý tài khoản người dùng (Khóa/Mở khóa user `is_active`, Cấp quyền `STAFF`), Xóa vĩnh viễn sản phẩm trong thùng rác (`DELETE /force`), Quản lý Mã giảm giá (`coupons`), Quản lý mẫu thông báo (`notification_templates`), Xem Dashboard thống kê doanh thu & Xuất báo cáo Excel/PDF (`poi-ooxml`, `itext7-core`). |
| **`SYSTEM`** | `orders.cancelled_by` & `order_status_history.changed_by_type` ([V1\_\_init\_order\_db.sql](https://drive.google.com/file/d/1MfC8bzg8f5N-2_sDE1sFAU5f1O-4g6wi/view?usp=drivesdk)) | **Tác nhân tự động của hệ thống** (dùng để ghi Audit Log trong bảng `orders` và `order_status_history`). Ví dụ: CronJob tự động hủy đơn `VNPAY` quá hạn 15 phút chưa thanh toán và hoàn kho qua Kafka, hoặc Webhook callback tự động cập nhật trạng thái. |

### 1.2. Các Lỗ Hổng & Phần Chưa Code Giữa Database Schema và Code Java Thực Tế

Khi đọc kỹ từng file `.sql` và `.java`, thầy phát hiện em đã thiết kế **Database Schema (`V1__init_*.sql`) cực kỳ xịn và đầy đủ**, nhưng phần code Java mới chỉ đi được khoảng **55–60% số bảng**:

1. **Sai lệch giữa `PROJECT_STATUS.md` và Code thực tế:**  
   - File `PROJECT_STATUS.md` ghi dùng PostgreSQL \+ UUID, nhưng thực tế 100% `pom.xml` ([identity-service/pom.xml](https://drive.google.com/file/d/1_XoQTgoIt7xgL5GYoyAPWpboId9BuLh4/view?usp=drivesdk), [catalog-service/pom.xml](https://drive.google.com/file/d/1V_WhtGq9ZZ4jPUCVDiSenMjXR1McbJ81/view?usp=drivesdk), [order-service/pom.xml](https://drive.google.com/file/d/1U3PkpGTGbZxFDOcDrXPdOvGVztZ4kwim/view?usp=drivesdk), [notification-service/pom.xml](https://drive.google.com/file/d/156_QsF1eLSKZF5USpp0guzKQcHxkVD1r/view?usp=drivesdk)) và Flyway SQL đều dùng **MySQL 8.0** với khóa chính `BIGINT UNSIGNED AUTO_INCREMENT` (`Long id`).  
   - File `PROJECT_STATUS.md` đánh dấu ✅ Done cho Address CRUD, Wishlist CRUD, Refresh Token API, Verify Email API, Xem danh sách đơn hàng... nhưng trong code thực tế ([AuthController.java](https://drive.google.com/file/d/1ToeXvbM-R1qEvLIK7HKruDlB7UEbnUw0/view?usp=drivesdk), [UserController.java](https://drive.google.com/file/d/11Tm9N8guwR9gdSSMObRYNy8kLHgSrSW_/view?usp=drivesdk), [OrderController.java](https://drive.google.com/file/d/1NZNt2KLJBJNkSZsqtTBWgCPFoGGfVbnd/view?usp=drivesdk)), **các Controller này mới chỉ có vài hàm cơ bản** (`register`, `login`, `getCurrentUser`, `checkout`, `cancelOrder`)\!  
2. **4 Bảng trong `order_db` ([V1\_\_init\_order\_db.sql](https://drive.google.com/file/d/1MfC8bzg8f5N-2_sDE1sFAU5f1O-4g6wi/view?usp=drivesdk)) chưa có Entity/Code Java:**  
   - `coupons` (Mã giảm giá % / tiền mặt / freeship, giới hạn sản phẩm/danh mục JSON)  
   - `coupon_usages` (Lịch sử áp mã giảm giá của user)  
   - `transactions` (Lịch sử giao dịch thanh toán & hoàn tiền VNPay/MoMo/COD)  
   - `order_status_history` (Lịch sử chuyển trạng thái đơn hàng của `CUSTOMER`, `STAFF`, `ADMIN`, `SYSTEM`)  
3. **2 Bảng trong `notification_db` ([V1\_\_init\_notification\_db.sql](https://drive.google.com/file/d/1yaFcwWBRD1tNX6hjH4UITq0rvyJpM46j/view?usp=drivesdk)) chưa có Entity/Code Java:**  
   - `notification_templates` (Quản lý mẫu Email/SMS động)  
   - `notification_logs` (Lưu lịch sử gửi thông báo, chống gửi trùng bằng `idempotency_key`, cơ chế retry khi lỗi SMTP)  
4. **Thư viện đã khai báo trong `pom.xml` nhưng chưa viết tính năng:**  
   - Trong [order-service/pom.xml](https://drive.google.com/file/d/1U3PkpGTGbZxFDOcDrXPdOvGVztZ4kwim/view?usp=drivesdk) đã thêm `itext7-core` (xuất hóa đơn PDF) và `poi-ooxml` (xuất báo cáo Excel), nhưng chưa có bất kỳ Service/Controller nào sử dụng\!

---

## PHẦN 2: NỘI DUNG SIÊU PROMPT (COPY TỪ ĐÂY ĐƯA VÀO ANTIGRAVITY)

\# ROLE & CONTEXT

Bạn là Principal Java Microservices Architect & Senior Spring Boot Engineer. Bạn đang trực tiếp lập trình hoàn thiện dự án thương mại điện tử \*\*Mộc Hương Shop (\`aromatics-platform\`)\*\* — chuyên kinh doanh Nhang trầm, Hương thảo mộc, Tinh dầu thiên nhiên, Vòng gỗ phong thủy và Chuỗi hạt thiền định.

\#\# KIẾN TRÚC HIỆN TẠI & QUY ƯỚC BẮT BUỘC

\- \*\*Ngôn ngữ & Framework:\*\* Java 21, Spring Boot 3.2.5, Spring Cloud 2023.0.1.

\- \*\*Database:\*\* MySQL 8.0 (\`mysql-connector-j\`, \`flyway-mysql\`), mọi bảng dùng \`BIGINT UNSIGNED AUTO\_INCREMENT PRIMARY KEY\` ánh xạ sang \`Long id\` trong JPA Entity. Tuyệt đối KHÔNG đổi sang UUID hay PostgreSQL.

\- \*\*Base Package chuẩn của từng Service:\*\*

  \- \`service-discovery\` (:8761): \`com.minhtriet.se3979.servicediscovery\`

  \- \`api-gateway\` (:8080): \`com.minhtriet.se3979.apigateway\`

  \- \`identity-service\` (:8081): \`com.minhtriet.se3979.identityservice\`

  \- \`catalog-service\` (:8082): \`com.minhtriet.se3979.catalogservice\`

  \- \`order-service\` (:8083): \`com.minhtriet.se3979.orderservice\`

  \- \`notification-service\` (:8084): \`com.minhtriet.se3979.notificationservice\`

\- \*\*3 User Roles (\`Role.java\`):\*\* \`CUSTOMER\`, \`STAFF\`, \`ADMIN\`.

\- \*\*1 Audit Actor (trong \`order\_db\`):\*\* \`SYSTEM\`.

\- \*\*Nguyên tắc vàng:\*\*

  1\. Không phá vỡ các Flyway migration đã có (\`V1\` đến \`V4\`). Nếu cần thêm cột/index mới, phải tạo file migration tiếp theo (\`V2\_\_...\` hoặc \`V5\_\_...\`).

  2\. Mọi API trả về cấu trúc đồng nhất \`ApiResponse\<T\>\` và bắt lỗi tập trung qua \`GlobalExceptionHandler\` \+ \`AppException\`.

  3\. Tuyệt đối không trả trực tiếp JPA Entity (\`Order\`, \`User\`, \`Product\`) ra Controller; bắt buộc dùng Request/Response DTO.

  4\. Không gửi trực tiếp JPA Entity vào \`KafkaTemplate\`; bắt buộc dùng Event DTO độc lập (Record hoặc POJO) để tránh lỗi Jackson LazyInitializationException và Infinite Recursion.

\---

\#\# LỘ TRÌNH THỰC THI 7 CHẶNG (20 TASKS CHI TIẾT)

\#\#\# 🔴 PHASE 1: SỬA CÁC LỖI NGHIÊM TRỌNG & CHUẨN HÓA NỀN TẢNG (CRITICAL FIXES & CORE ALIGNMENT)

\#\#\#\# Task 1.1: Chuẩn hóa Root Maven Multi-Module (\`pom.xml\`) & Common Response/Exception

\- \*\*Hiện trạng:\*\* Root \`pom.xml\` ở thư mục gốc \`aromatics-platform/pom.xml\` đang để Spring Boot \`4.1.0\` và thiếu thẻ \`\<modules\>\`, trong khi 6 service con dùng Spring Boot \`3.2.5\` và Spring Cloud \`2023.0.1\`. Ngoài ra \`order-service\` và \`notification-service\` chưa có \`AppException\` và \`GlobalExceptionHandler\` chuẩn như \`identity-service\`.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Cập nhật \`aromatics-platform/pom.xml\` thành \`\<packaging\>pom\</packaging\>\`, đồng bộ parent \`spring-boot-starter-parent\` version \`3.2.5\`, khai báo \`\<modules\>\` gồm đầy đủ 6 module: \`service-discovery\`, \`api-gateway\`, \`identity-service\`, \`catalog-service\`, \`order-service\`, \`notification-service\`.

  2\. Dọn dẹp các thẻ \`\<dependency\>\` bị khai báo trùng lặp (duplicate) trong \`catalog-service/pom.xml\`, \`order-service/pom.xml\`, và \`notification-service/pom.xml\`.

  3\. Bổ sung \`ApiResponse\<T\>\`, \`ErrorResponse\`, \`AppException(HttpStatus status, String message)\`, và \`GlobalExceptionHandler\` (\`@RestControllerAdvice\` bắt \`AppException\`, \`MethodArgumentNotValidException\`, \`AccessDeniedException\`, \`Exception\`) đồng bộ cho cả 4 service: \`identity-service\`, \`catalog-service\`, \`order-service\`, \`notification-service\`.

\#\#\#\# Task 1.2: Chuẩn hóa JWT SecurityContext & Trích xuất \`userId\`, \`email\`, \`role\` trên toàn hệ thống

\- \*\*Hiện trạng:\*\*

  \- Trong \`OrderController\` và \`CartController\`, code đang phải viết hàm thủ công \`getUserIdFromRequest(HttpServletRequest)\` cắt chuỗi \`"Bearer "\`.

  \- Trong \`ReviewController\`, code đang dùng tạm \`@RequestHeader(value \= "X-UserId", defaultValue \= "1") Long userId\` và đang comment \`// Trong thực tế sẽ gắn @PreAuthorize("hasRole('ADMIN')") ở đây\`.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Trong \`identity-service\`, \`catalog-service\`, và \`order-service\`, tạo class \`AuthenticatedUser\` (chứa \`Long userId\`, \`String email\`, \`String role\`, \`String fullName\`) và cập nhật \`JwtAuthFilter\` để lưu \`AuthenticatedUser\` vào \`UsernamePasswordAuthenticationToken\` principal (hoặc tạo helper bean \`SecurityUtils.getCurrentUser()\` / \`SecurityUtils.getCurrentUserId()\` / \`SecurityUtils.getCurrentUserEmail()\`).

  2\. Cập nhật \`ReviewController\`:

     \- Bỏ \`@RequestHeader(value \= "X-UserId", defaultValue \= "1")\`, thay bằng lấy \`userId\` thật từ JWT thông qua \`SecurityUtils.getCurrentUserId()\`.

     \- Bật \`@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")\` cho 3 API quản trị: \`PUT /api/catalog/products/reviews/{reviewId}/reply\`, \`PUT /api/catalog/products/reviews/{reviewId}/toggle-visibility\`, và \`GET /api/catalog/products/{productId}/admin/reviews\`.

\#\#\#\# Task 1.3: Khắc phục triệt để lỗi Checkout, Kafka Event Serialization & Email Hardcode

\- \*\*Hiện trạng:\*\*

  \- \`OrderServiceImpl.checkout()\` đang truyền thẳng Entity \`Order\` vào \`kafkaTemplate.send("order.created", ...)\` khiến danh sách \`OrderItem\` không được map chuẩn hoặc gây lỗi vòng lặp JSON.

  \- \`OrderServiceImpl.checkout()\` chưa kiểm tra trùng \`idempotencyKey\` (khách bấm đúp sẽ lỗi SQL Unique Constraint 500 thay vì trả về đơn hàng đã tạo), đang gán cứng \`sku("SKU-" \+ c.getVariantId())\` và chưa lưu lịch sử vào \`order\_status\_history\`.

  \- \`notification-service\` (\`OrderEventListener.java\`) đang gán cứng \`String testEmail \= "trietleminh7979@gmail.com";\` thay vì gửi cho email thật của khách hàng.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Tạo các Kafka Event DTO chuẩn (dùng Java \`record\` hoặc \`@Data @Builder @NoArgsConstructor @AllArgsConstructor\`) trong \`order-service\`, \`catalog-service\`, và \`notification-service\`:

     \- \`OrderCreatedEvent\`: \`Long orderId, String orderCode, Long userId, String userEmail, String fullName, BigDecimal subtotal, BigDecimal shippingFee, BigDecimal discountAmount, BigDecimal totalAmount, String paymentMethod, String paymentStatus, String status, Map\<String, Object\> shippingAddress, List\<OrderItemEventDto\> items, LocalDateTime createdAt\`.

     \- \`OrderCancelledEvent\`: \`Long orderId, String orderCode, Long userId, String userEmail, String cancelledReason, String cancelledBy, List\<OrderItemEventDto\> items, LocalDateTime cancelledAt\`.

     \- \`OrderStatusChangedEvent\`: \`Long orderId, String orderCode, Long userId, String userEmail, String fromStatus, String toStatus, String note, LocalDateTime changedAt\`.

  2\. Trong \`OrderServiceImpl.checkout()\`:

     \- Kiểm tra \`orderRepository.findByIdempotencyKey(request.getIdempotencyKey())\`: nếu đã tồn tại thì trả về ngay \`OrderResponse\` của đơn đó (Idempotent).

     \- Lấy \`userEmail\` và \`fullName\` từ JWT Token (\`SecurityUtils\`) để đưa vào \`OrderCreatedEvent\`.

     \- Với \`paymentMethod \= "COD"\`: Đặt \`status \= "CONFIRMED"\` (hoặc \`"PENDING\_PAYMENT"\` chờ Staff xác nhận) và phát \`OrderCreatedEvent\` sang \`notification-service\` kèm đầy đủ danh sách món hàng và địa chỉ.

     \- Với \`paymentMethod \= "VNPAY"\`: Đặt \`status \= "PENDING\_PAYMENT"\`, \`paymentStatus \= "UNPAID"\`. Khi khách thanh toán thành công tại \`PaymentController.vnpayReturn\`, cập nhật \`paymentStatus \= "PAID"\`, \`status \= "CONFIRMED"\`, lưu bản ghi vào bảng \`transactions\` và \`order\_status\_history\`, sau đó phát sự kiện sang \`notification-service\` để gửi email xác nhận thanh toán thành công. Nếu VNPay trả về mã lỗi thất bại (\`vnp\_ResponseCode \!= "00"\`), tự động chuyển đơn sang \`CANCELLED\` (\`cancelledBy \= SYSTEM\`), lưu \`transactions\` (\`FAILED\`), và phát \`OrderCancelledEvent\` để \`catalog-service\` hoàn trả tồn kho (\`restoreStock\`)\!

\---

\#\#\# 🟠 PHASE 2: HOÀN THIỆN 100% \`identity-service\` (AUTH, PROFILE, ADDRESS, WISHLIST, ADMIN USER)

\#\#\#\# Task 2.1: Hoàn thiện Luồng Xác thực Email (OTP/Token), Quên Mật khẩu, Refresh Token Rotation & Logout

\- \*\*Hiện trạng:\*\* Bảng \`refresh\_tokens\` và \`email\_verifications\` cùng enum \`VerificationType (VERIFY\_EMAIL, RESET\_PASSWORD)\` đã có trong DB và Entity, nhưng \`AuthController\` mới chỉ có \`/register\` và \`/login\`. Hàm \`TokenServiceImpl.revokeAllUserTokens()\` đang để trống.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Hoàn thiện \`TokenServiceImpl\`:

     \- Thực thi \`revokeAllUserTokens(User user)\`: cập nhật \`isRevoked \= true\` cho tất cả token chưa bị thu hồi của \`user.getId()\` bằng \`@Modifying @Query\` trong \`RefreshTokenRepository\`.

     \- Thêm hàm \`RefreshToken verifyAndRotateRefreshToken(String rawRefreshToken, String ipAddress, String deviceInfo)\`: băm SHA-256 \`rawRefreshToken\`, tìm trong DB, kiểm tra \`isRevoked \== false\` và \`expiresAt \> now()\`. Nếu hợp lệ, đánh dấu token cũ \`isRevoked \= true\`, sinh refresh token mới và access token mới.

  2\. Hoàn thiện luồng \`EmailVerification\` & Kafka Notification:

     \- Khi đăng ký (\`register\`), tạo bản ghi \`EmailVerification\` (\`type \= VERIFY\_EMAIL\`, \`expiresAt \= now \+ 24h\`) và bắn sự kiện Kafka topic \`auth.notification\` (hoặc gửi mail) chứa link/mã xác thực.

     \- Bổ sung các API vào \`AuthController\` (\`/api/identity/auth\`):

       \- \`POST /refresh\`: Nhận \`{refreshToken}\`, trả về \`AuthResponse\` mới.

       \- \`POST /logout\`: Thu hồi refresh token hiện tại của user.

       \- \`GET /verify-email?token=...\` (hoặc \`POST /verify-email\`): Xác thực tài khoản (\`isEmailVerified \= true\`, \`isUsed \= true\`).

       \- \`POST /forgot-password\`: Nhận \`{email}\`, tạo \`EmailVerification\` (\`type \= RESET\_PASSWORD\`, \`expiresAt \= now \+ 15m\`), bắn Kafka event sang \`notification-service\` để gửi email đặt lại mật khẩu.

       \- \`POST /reset-password\`: Nhận \`{token, newPassword}\`, kiểm tra token hợp lệ, mã hóa BCrypt mật khẩu mới, cập nhật \`User\` và thu hồi toàn bộ \`RefreshToken\` cũ.

       \- \`POST /google\`: Nhận \`{idToken}\` hoặc thông tin OAuth2 Google (\`Provider.GOOGLE\`), tự động tạo hoặc liên kết user và trả về \`AuthResponse\`.

\#\#\#\# Task 2.2: Hoàn thiện Quản lý Profile, Đổi mật khẩu & Sổ Địa Chỉ (\`AddressController\`)

\- \*\*Hiện trạng:\*\* \`Address.java\`, \`AddressRepository.java\`, \`AddressRequest.java\` đã có sẵn nhưng chưa có Service và Controller\! \`UserController\` mới chỉ có \`GET /me\`.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Mở rộng \`UserController\` (\`/api/identity/users\`):

     \- \`PUT /me\`: Cập nhật \`fullName\`, \`phoneNumber\`, \`dateOfBirth\`, \`avatarUrl\` (validate trùng \`phoneNumber\`).

     \- \`PUT /me/password\`: Đổi mật khẩu khi đang đăng nhập (\`oldPassword\`, \`newPassword\`).

     \- \`GET /internal/{userId}\`: API nội bộ để \`order-service\` hoặc \`catalog-service\` lấy thông tin cơ bản (\`id, email, fullName, phoneNumber, avatarUrl\`) qua FeignClient.

  2\. Tạo \`AddressResponse\`, \`AddressService\`, \`AddressServiceImpl\`, và \`AddressController\` (\`/api/identity/users/me/addresses\`):

     \- \`GET /api/identity/users/me/addresses\`: Lấy danh sách địa chỉ của user đang đăng nhập (sắp xếp \`isDefault DESC, createdAt DESC\`).

     \- \`POST /api/identity/users/me/addresses\`: Thêm địa chỉ mới. Nếu là địa chỉ đầu tiên của user hoặc \`request.getIsDefault() \== true\`, tự động đặt \`isDefault \= true\` và hạ các địa chỉ cũ xuống \`false\`.

     \- \`PUT /api/identity/users/me/addresses/{id}\`: Cập nhật địa chỉ (kiểm tra đúng \`userId\` sở hữu).

     \- \`PUT /api/identity/users/me/addresses/{id}/default\`: Đặt làm địa chỉ mặc định.

     \- \`DELETE /api/identity/users/me/addresses/{id}\`: Xóa địa chỉ.

\#\#\#\# Task 2.3: Hoàn thiện Danh sách Yêu thích (\`WishlistController\`)

\- \*\*Hiện trạng:\*\* \`Wishlist.java\` và \`WishlistRepository.java\` đã có trong \`identity-service\` nhưng chưa có Service/Controller.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Tạo \`WishlistResponse\`, \`WishlistService\`, \`WishlistServiceImpl\`, và \`WishlistController\` (\`/api/identity/users/me/wishlists\`):

     \- \`GET /api/identity/users/me/wishlists\`: Lấy danh sách \`productId\` yêu thích của user.

     \- \`POST /api/identity/users/me/wishlists/{productId}\`: Thêm sản phẩm vào wishlist (kiểm tra \`existsByUserIdAndProductId\` để chống trùng lặp).

     \- \`DELETE /api/identity/users/me/wishlists/{productId}\`: Xóa sản phẩm khỏi wishlist.

     \- \`GET /api/identity/users/me/wishlists/{productId}/check\`: Kiểm tra trạng thái đã yêu thích hay chưa (\`boolean\`).

\#\#\#\# Task 2.4: Xây dựng Module Quản trị Người dùng cho ADMIN (\`AdminUserController\`)

\- \*\*Hiện trạng:\*\* Trong \`identity-service/security/SecurityConfig.java\` đã cấu hình \`.requestMatchers("/api/identity/admin/\*\*").hasRole("ADMIN")\`, nhưng chưa có \`AdminUserController\`\!

\- \*\*Yêu cầu thực thi:\*\*

  1\. Tạo \`AdminUserController\` (\`/api/identity/admin/users\`) yêu cầu quyền \`ADMIN\`:

     \- \`GET /api/identity/admin/users\`: Phân trang & lọc danh sách người dùng theo \`keyword\` (email, fullName, phoneNumber), \`role\` (\`CUSTOMER\`, \`STAFF\`, \`ADMIN\`), và \`isActive\` (\`true\`/\`false\`).

     \- \`GET /api/identity/admin/users/{id}\`: Xem chi tiết thông tin user kèm danh sách địa chỉ.

     \- \`PUT /api/identity/admin/users/{id}/role\`: Cập nhật Role cho user (VD: nâng cấp \`CUSTOMER\` lên \`STAFF\` hoặc hạ quyền; chặn Admin tự hạ quyền chính mình).

     \- \`PUT /api/identity/admin/users/{id}/status\`: Khóa hoặc Mở khóa tài khoản (\`isActive \= false/true\`). Khi khóa tài khoản (\`isActive \= false\`), tự động gọi \`tokenService.revokeAllUserTokens(user)\`.

\---

\#\#\# 🟡 PHASE 3: NÂNG CẤP \`catalog-service\` (MULTI-VARIANT, INVENTORY ADMIN, REDIS CACHE & VERIFIED REVIEW)

\#\#\#\# Task 3.1: Quản lý Đa Biến Thể Sản Phẩm (\`ProductVariant\`) & Thuộc tính Đặc thù Nhang/Tinh Dầu/Vòng Gỗ

\- \*\*Hiện trạng:\*\* Bảng \`product\_variants\` hỗ trợ nhiều biến thể cho 1 sản phẩm (VD: Nhang trầm hộp 40 nén / 100 nén, Tinh dầu lọ 10ml / 50ml, Vòng gỗ Trầm Hương 108 hạt size 6mm / 8mm, lưu trong cột \`attributes\` kiểu JSON, \`weight\_gram\`, \`original\_price\`). Tuy nhiên \`ProductServiceImpl.createProductWithImages\` hiện đang hardcode tạo duy nhất 1 variant tên \`"Default"\`.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Mở rộng \`ProductCreateRequest\` để hỗ trợ truyền danh sách \`List\<VariantCreateRequest\> variants\` (nếu không truyền thì vẫn fallback về \`sku\`, \`price\`, \`stockQuantity\` như cũ để tương thích ngược).

  2\. Tạo các API quản lý Biến thể cho \`STAFF\` và \`ADMIN\` trong \`ProductController\` (hoặc \`ProductVariantController\` \`/api/catalog/products/{productId}/variants\`):

     \- \`POST /api/catalog/products/{productId}/variants\`: Thêm biến thể mới (\`sku\`, \`variantName\`, \`price\`, \`originalPrice\`, \`weightGram\`, \`attributes\` JSON, \`initialStock\`) và tự động khởi tạo bản ghi \`Inventory\` tương ứng.

     \- \`PUT /api/catalog/products/{productId}/variants/{variantId}\`: Cập nhật thông tin biến thể (\`variantName\`, \`price\`, \`originalPrice\`, \`weightGram\`, \`attributes\`, \`isActive\`, \`sortOrder\`).

     \- \`DELETE /api/catalog/products/{productId}/variants/{variantId}\`: Ẩn biến thể (\`isActive \= false\`).

  3\. Tạo API nội bộ \`GET /api/catalog/products/variants/{variantId}/internal\` (hoặc batch \`POST /api/catalog/products/variants/batch-info\`) trả về thông tin chuẩn xác (\`productId, variantId, productName, variantName, sku, price, primaryImageUrl, availableStock, isActive\`) để \`order-service\` gọi qua FeignClient khi thêm vào giỏ hàng và khi Checkout.

\#\#\#\# Task 3.2: Hoàn thiện Quản lý Tồn Kho cho \`STAFF\` & \`ADMIN\` (\`InventoryController\`)

\- \*\*Hiện trạng:\*\* \`InventoryController\` hiện chỉ có duy nhất 1 API \`POST /api/catalog/inventory/deduct\`. Chưa có API xem tồn kho, nhập hàng (restock), hay cảnh báo sắp hết hàng (\`low\_stock\_threshold\`).

\- \*\*Yêu cầu thực thi:\*\*

  1\. Mở rộng \`InventoryController\` (\`/api/catalog/inventory\`):

     \- \`GET /api/catalog/inventory/variant/{variantId}\`: Xem chi tiết tồn kho (\`quantity\`, \`reservedQuantity\`, \`lowStockThreshold\`, \`updatedAt\`).

     \- \`PUT /api/catalog/inventory/variant/{variantId}\` (\`@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")\`): Cập nhật/Nhập thêm số lượng tồn kho và \`lowStockThreshold\` (sử dụng Redisson Lock \`lock:inventory:variant:{variantId}\` để đảm bảo an toàn đồng thời).

     \- \`GET /api/catalog/inventory/low-stock\` (\`@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")\`): Lấy danh sách các biến thể có \`quantity \<= low\_stock\_threshold\` để nhân viên biết đường nhập thêm nhang/tinh dầu/vòng gỗ.

     \- \`POST /api/catalog/inventory/restore\`: API nội bộ để hoàn kho đồng bộ khi cần.

\#\#\#\# Task 3.3: Tích hợp \`ProductCacheService\` (Redis Cache-Aside & Cache Eviction) & Bộ lọc Sản phẩm Nâng cao

\- \*\*Hiện trạng:\*\* Class \`ProductCacheService.java\` đã được viết trong package \`com.minhtriet.se3979.catalogservice.redis\` nhưng chưa được gọi ở bất kỳ đâu\!

\- \*\*Yêu cầu thực thi:\*\*

  1\. Tích hợp \`ProductCacheService\` vào \`ProductServiceImpl\`:

     \- Cache chi tiết sản phẩm theo slug (\`cache:product:slug:{slug}\`, TTL 10 phút) và danh sách sản phẩm nổi bật/bán chạy (\`cache:featured\_products\`, TTL 5 phút).

     \- Tự động xóa cache (Cache Eviction) mỗi khi \`createProduct\`, \`updateProduct\`, \`deleteProduct\`, \`restoreProduct\`, \`hardDeleteProduct\`, hoặc khi có đánh giá mới làm thay đổi \`averageRating\`.

  2\. Bổ sung API \`GET /api/catalog/products/featured\` (Top sản phẩm bán chạy \`totalSold DESC\` và điểm cao \`averageRating DESC\`) sử dụng \`ProductCacheService\`.

  3\. Lắng nghe sự kiện đơn hàng hoàn tất (\`COMPLETED\` / \`DELIVERED\`) hoặc tạo đơn để tự động tăng \`Product.totalSold\` trong \`catalog-service\`.

\---

\#\#\# 🟢 PHASE 4: HOÀN THIỆN 100% \`order-service\` (COUPONS, TRANSACTIONS, STATUS HISTORY, ADMIN ORDERS, PDF/EXCEL)

\#\#\#\# Task 4.1: Hiện thực hóa Hệ thống Mã Giảm Giá (\`Coupon\` & \`CouponUsage\`) theo đúng Schema \`V1\_\_init\_order\_db.sql\`

\- \*\*Hiện trạng:\*\* Hai bảng \`coupons\` và \`coupon\_usages\` đã có sẵn trong \`V1\_\_init\_order\_db.sql\` nhưng chưa có Entity, Repository, Service, Controller.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Tạo JPA Entity \`Coupon\` (ánh xạ bảng \`coupons\`: \`id, code, name, type \[PERCENTAGE, FIXED\_AMOUNT, FREE\_SHIPPING\], value, maxDiscountAmount, minOrderValue, maxUses, maxUsesPerUser, usedCount, applicableProductIds \[JSON List\<Long\>\], applicableCategoryIds \[JSON List\<Long\>\], startsAt, expiresAt, isActive, createdAt\`) và \`CouponUsage\` (ánh xạ bảng \`coupon\_usages\`: \`id, coupon, userId, order, discountAmount, isRefunded, usedAt\`).

  2\. Tạo \`CouponRepository\` và \`CouponUsageRepository\`.

  3\. Tạo \`CouponService\` & \`CouponServiceImpl\`:

     \- Hàm \`CouponCalculationResult validateAndCalculateDiscount(Long userId, String couponCode, BigDecimal subtotal, BigDecimal shippingFee, List\<CartItem\> items)\`:

       \- Kiểm tra mã tồn tại, \`isActive \== true\`, nằm trong khoảng \`startsAt\` đến \`expiresAt\`.

       \- Kiểm tra \`maxUses\` (\`usedCount \< maxUses\`) và \`maxUsesPerUser\` (đếm trong \`coupon\_usages\` với \`isRefunded \= false\`).

       \- Kiểm tra \`subtotal \>= minOrderValue\`.

       \- Kiểm tra \`applicableProductIds\` (nếu khác null/rỗng thì ít nhất 1 món trong giỏ phải nằm trong danh sách).

       \- Tính số tiền giảm:

         \- \`PERCENTAGE\`: \`subtotal \* (value / 100)\`, nếu vượt quá \`maxDiscountAmount\` thì lấy \`maxDiscountAmount\`.

         \- \`FIXED\_AMOUNT\`: giảm thẳng \`value\` (không vượt quá \`subtotal\`).

         \- \`FREE\_SHIPPING\`: giảm \`min(shippingFee, value)\`.

  4\. Tạo \`CouponController\`:

     \- Khách hàng (\`CUSTOMER\`):

       \- \`GET /api/order/coupons/available\`: Danh sách mã giảm giá đang có hiệu lực.

       \- \`POST /api/order/coupons/preview\`: Truyền \`couponCode\` để xem trước số tiền được giảm trên giỏ hàng hiện tại.

     \- Quản trị (\`ADMIN\` / \`STAFF\`):

       \- \`POST /api/order/admin/coupons\`, \`PUT /api/order/admin/coupons/{id}\`, \`DELETE /api/order/admin/coupons/{id}\`, \`GET /api/order/admin/coupons\` (\`@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')"\`).

\#\#\#\# Task 4.2: Hiện thực hóa Bảng \`Transaction\` & \`OrderStatusHistory\` \+ Nâng cấp Luồng Checkout & Giỏ hàng

\- \*\*Hiện trạng:\*\* Hai bảng \`transactions\` và \`order\_status\_history\` đã có trong \`V1\_\_init\_order\_db.sql\` nhưng chưa có Entity. \`CartController\` thiếu API cập nhật số lượng (\`PUT\`). \`OrderController\` trả thẳng Entity \`Order\` và thiếu API xem danh sách/chi tiết đơn hàng.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Tạo JPA Entity \`Transaction\` (ánh xạ bảng \`transactions\`) và \`OrderStatusHistory\` (ánh xạ bảng \`order\_status\_history\`) cùng 2 Repository tương ứng.

  2\. Bổ sung vào \`CartController\` (\`/api/order/cart\`):

     \- \`PUT /api/order/cart/update\`: Cập nhật số lượng (\`quantity\`) của 1 món hàng (\`productId\`, \`variantId\`). Nếu \`quantity \<= 0\` thì xóa món đó khỏi giỏ.

     \- Khi \`addToCart\`, gọi \`catalogFeignClient\` để lấy giá bán thực tế (\`price\`), \`sku\`, \`productName\`, \`variantName\`, \`productImageUrl\` và kiểm tra tồn kho từ \`catalog-service\` thay vì tin tưởng tuyệt đối vào \`price\` do Frontend gửi lên\!

  3\. Trong \`OrderServiceImpl.checkout()\`:

     \- Tích hợp \`couponCode\` (nếu \`request.getCouponCode()\` có truyền lên): gọi \`couponService\` tính \`discountAmount\`, tăng \`coupon.usedCount\`, lưu bản ghi \`CouponUsage\`.

     \- Lưu \`OrderItem\` với \`sku\` thật lấy từ \`catalog-service\`.

     \- Ghi bản ghi đầu tiên vào \`order\_status\_history\` (\`fromStatus \= null\`, \`toStatus \= savedOrder.getStatus()\`, \`changedByType \= CUSTOMER\`, \`changedById \= userId\`, \`note \= "Khách hàng tạo đơn hàng"\`).

     \- Khởi tạo bản ghi \`Transaction\` (\`type \= PAYMENT\`, \`status \= PENDING\`, \`amount \= totalAmount\`, \`paymentMethod \= request.getPaymentMethod()\`, \`transactionCode \= "TXN-" \+ orderCode\`).

     \- Khi hủy đơn (\`cancelOrder\`), nếu đơn có dùng mã giảm giá thì đánh dấu \`CouponUsage.isRefunded \= true\` và giảm \`coupon.usedCount\`, đồng thời ghi vào \`order\_status\_history\`.

  4\. Tạo \`OrderResponse\` và \`OrderDetailResponse\` (chứa danh sách \`OrderItemDto\`, \`OrderStatusHistoryDto\`, \`TransactionDto\`) và bổ sung vào \`OrderController\`:

     \- \`GET /api/order/orders\`: Lấy danh sách đơn hàng của khách hàng hiện tại (phân trang, lọc theo \`status\`).

     \- \`GET /api/order/orders/{orderCode}\`: Xem chi tiết đơn hàng theo \`orderCode\` (hoặc \`id\`), kèm lịch sử trạng thái.

     \- \`POST /api/order/orders/{orderId}/return-request\`: Khách hàng gửi yêu cầu trả hàng/hoàn tiền (\`RETURN\_REQUESTED\`) khi đơn đang ở trạng thái \`DELIVERED\` kèm lý do.

\#\#\#\# Task 4.3: Xây dựng Quản lý Đơn Hàng cho \`STAFF\` & \`ADMIN\` (\`AdminOrderController\`) \+ Máy Trạng Thái (State Machine)

\- \*\*Hiện trạng:\*\* Chưa có bất kỳ API nào cho \`STAFF\` và \`ADMIN\` quản lý đơn hàng.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Tạo \`AdminOrderController\` (\`/api/order/admin/orders\`) với \`@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")\`:

     \- \`GET /api/order/admin/orders\`: Phân trang & lọc toàn bộ đơn hàng theo \`orderCode\`, \`status\`, \`paymentMethod\`, \`paymentStatus\`, \`fromDate\`, \`toDate\`.

     \- \`GET /api/order/admin/orders/{orderId}\`: Xem chi tiết đơn hàng đầy đủ (bao gồm \`staffNotes\`, \`transactions\`, \`orderStatusHistory\`).

     \- \`PUT /api/order/admin/orders/{orderId}/status\`: Cập nhật trạng thái đơn hàng theo đúng State Machine hợp lệ của \`V1\_\_init\_order\_db.sql\`:

       \- \`PENDING\_PAYMENT\` \-\> \`CONFIRMED\` hoặc \`CANCELLED\`

       \- \`CONFIRMED\` \-\> \`PREPARING\` hoặc \`CANCELLED\`

       \- \`PREPARING\` \-\> \`SHIPPING\` hoặc \`CANCELLED\`

       \- \`SHIPPING\` \-\> \`DELIVERED\`

       \- \`DELIVERED\` \-\> \`COMPLETED\` hoặc \`RETURN\_REQUESTED\`

       \- \`RETURN\_REQUESTED\` \-\> \`RETURNED\`

     \- Khi chuyển sang \`CONFIRMED\`, tự động set \`confirmedAt \= LocalDateTime.now()\`.

     \- Khi chuyển sang \`DELIVERED\`, tự động set \`deliveredAt \= LocalDateTime.now()\`, và nếu \`paymentMethod \== "COD"\` thì cập nhật \`paymentStatus \= "PAID"\` \+ cập nhật \`Transaction.status \= "SUCCESS"\`.

     \- Khi \`STAFF\`/\`ADMIN\` chuyển sang \`CANCELLED\` hoặc \`RETURNED\`, bắt buộc truyền lý do, lưu \`cancelledBy \= STAFF/ADMIN\`, hoàn trả tồn kho qua Kafka (\`order.cancelled\`), hoàn trả lượt dùng Coupon (nếu có), và lưu \`order\_status\_history\`.

     \- Mỗi lần đổi trạng thái, bắn sự kiện Kafka \`order.status.changed\` sang \`notification-service\` để gửi email thông báo cho khách.

     \- \`PUT /api/order/admin/orders/{orderId}/staff-notes\`: Cập nhật ghi chú nội bộ (\`staff\_notes\`) cho đơn hàng.

\#\#\#\# Task 4.4: Xuất Hóa Đơn PDF (\`iText7\`) & Xuất Báo Cáo Doanh Thu Excel (\`Apache POI\`) \+ Thống kê Dashboard

\- \*\*Hiện trạng:\*\* Trong \`order-service/pom.xml\` đã cài sẵn \`com.itextpdf:itext7-core:7.2.5\` và \`org.apache.poi:poi-ooxml:5.2.5\`, nhưng chưa viết code sử dụng\!

\- \*\*Yêu cầu thực thi:\*\*

  1\. Tạo \`OrderExportService\` và các API:

     \- \`GET /api/order/orders/{orderId}/invoice-pdf\`: Xuất file PDF hóa đơn bán hàng Mộc Hương Shop (có mã đơn, ngày đặt, thông tin người nhận, bảng danh sách sản phẩm nhang/tinh dầu/vòng gỗ, tạm tính, phí ship, giảm giá, tổng thanh toán) trả về \`application/pdf\` (cho phép chủ đơn hàng hoặc \`STAFF\`/\`ADMIN\` tải).

     \- \`GET /api/order/admin/orders/export-excel?fromDate=...\&toDate=...\` (\`@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")\`): Dùng \`XSSFWorkbook\` (Apache POI) xuất báo cáo danh sách đơn hàng và tổng hợp doanh thu theo trạng thái ra file \`.xlsx\`.

     \- \`GET /api/order/admin/orders/statistics\` (\`@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")\`): Trả về dữ liệu Dashboard gồm: Tổng doanh thu (\`PAID\`), Tổng số đơn theo từng trạng thái, Doanh thu theo ngày/tháng, và Top sản phẩm bán chạy.

\---

\#\#\# 🟣 PHASE 5: HOÀN THIỆN 100% \`notification-service\` (TEMPLATES, LOGS, IDEMPOTENCY & MULTI-EVENT KAFKA)

\#\#\#\# Task 5.1: Hiện thực hóa Bảng \`NotificationTemplate\` & \`NotificationLog\` theo \`V1\_\_init\_notification\_db.sql\`

\- \*\*Hiện trạng:\*\* Hai bảng \`notification\_templates\` và \`notification\_logs\` đã được tạo trong \`V1\_\_init\_notification\_db.sql\` nhưng hoàn toàn chưa có Entity, Repository, Service\!

\- \*\*Yêu cầu thực thi:\*\*

  1\. Tạo JPA Entity \`NotificationTemplate\` (ánh xạ bảng \`notification\_templates\`) và \`NotificationLog\` (ánh xạ bảng \`notification\_logs\`: \`id, idempotencyKey, eventType, channel \[EMAIL, SMS, PUSH\], recipient, userId, referenceType, referenceId, subject, contentPreview, status \[PENDING, SENT, FAILED, SKIPPED\], errorMessage, retryCount, nextRetryAt, sentAt, createdAt\`).

  2\. Tạo \`NotificationTemplateRepository\` và \`NotificationLogRepository\`.

  3\. Tạo migration \`V2\_\_seed\_notification\_templates.sql\` trong \`notification-service\` để seed sẵn các mẫu email HTML chuyên nghiệp mang phong cách thiền/trầm hương của \*\*Mộc Hương Shop\*\*:

     \- \`ORDER\_CREATED\_EMAIL\`: Xác nhận đặt hàng thành công (hiển thị chi tiết từng món hàng, tổng tiền, địa chỉ giao).

     \- \`ORDER\_CANCELLED\_EMAIL\`: Thông báo hủy đơn hàng kèm lý do.

     \- \`ORDER\_STATUS\_CHANGED\_EMAIL\`: Thông báo cập nhật trạng thái đơn hàng (\`CONFIRMED\`, \`SHIPPING\`, \`DELIVERED\`).

     \- \`VERIFY\_EMAIL\_TEMPLATE\`: Email xác thực tài khoản mới.

     \- \`RESET\_PASSWORD\_TEMPLATE\`: Email khôi phục mật khẩu.

\#\#\#\# Task 5.2: Nâng cấp \`EmailService\` & \`OrderEventListener\` (Lưu Log, Chống Gửi Trùng & Retry Tự Động)

\- \*\*Hiện trạng:\*\* \`OrderEventListener\` chỉ nghe mỗi \`order.created\` và gửi tới email hardcode \`trietleminh7979@gmail.com\`, không lưu \`notification\_logs\`.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Trong \`EmailService\` & \`NotificationDispatcherService\`:

     \- Trước khi gửi email, kiểm tra \`notificationLogRepository.existsByIdempotencyKey(idempotencyKey)\` (ví dụ \`ORDER\_CREATED\_MH-12345678\`). Nếu đã \`SENT\` thì \`SKIPPED\` ngay để chống gửi trùng khi Kafka rebalance\!

     \- Lưu \`NotificationLog\` với trạng thái \`PENDING\`.

     \- Lấy \`recipient\` chính xác từ \`event.userEmail()\` (chỉ fallback về cấu hình \`app.mail.fallback-recipient\` nếu môi trường dev không có email).

     \- Khi gửi thành công qua \`JavaMailSender\`, cập nhật \`status \= SENT\`, \`sentAt \= LocalDateTime.now()\`.

     \- Khi gửi thất bại (lỗi SMTP), cập nhật \`status \= FAILED\`, lưu \`errorMessage\`, tăng \`retryCount\`, đặt \`nextRetryAt \= now \+ 5 phút\`.

  2\. Lắng nghe đầy đủ các Kafka Topics:

     \- \`order.created\` \-\> Gửi email hóa đơn xác nhận đơn hàng.

     \- \`order.cancelled\` \-\> Gửi email thông báo hủy đơn hàng.

     \- \`order.status.changed\` \-\> Gửi email thông báo khi đơn chuyển sang \`SHIPPING\` / \`DELIVERED\`.

     \- \`auth.notification\` \-\> Gửi email xác thực tài khoản (\`VERIFY\_EMAIL\`) và đặt lại mật khẩu (\`RESET\_PASSWORD\`).

  3\. Thêm \`@Scheduled\` job quét các bản ghi \`NotificationLog\` có \`status \= FAILED\` và \`retryCount \< 3\` và \`nextRetryAt \<= now()\` để tự động gửi lại.

\---

\#\#\# 🔵 PHASE 6: ĐỒNG BỘ \`api-gateway\` & BẢO MẬT LIÊN SERVICE

\#\#\#\# Task 6.1: Kiểm toán Routing \`api-gateway\`, CORS & Swagger Aggregator

\- \*\*Hiện trạng:\*\* Các Controller trong từng service đang dùng đường dẫn có tiền tố tên service:

  \- Identity: \`/api/identity/auth/\*\*\`, \`/api/identity/users/\*\*\`, \`/api/identity/admin/\*\*\`

  \- Catalog: \`/api/catalog/products/\*\*\`, \`/api/catalog/categories/\*\*\`, \`/api/catalog/inventory/\*\*\`

  \- Order: \`/api/order/cart/\*\*\`, \`/api/order/orders/\*\*\`, \`/api/order/payment/\*\*\`, \`/api/order/coupons/\*\*\`, \`/api/order/admin/\*\*\`

\- \*\*Yêu cầu thực thi:\*\*

  1\. Kiểm tra và đồng bộ \`api-gateway/src/main/resources/application.yml\` để định tuyến chính xác cả các đường dẫn \`/api/identity/\*\*\`, \`/api/catalog/\*\*\`, \`/api/order/\*\*\` (và hỗ trợ alias \`/api/auth/\*\*\`, \`/api/products/\*\*\`, \`/api/orders/\*\*\` nếu cần) về đúng \`lb://identity-service\`, \`lb://catalog-service\`, \`lb://order-service\`.

  2\. Cấu hình Global CORS trên \`api-gateway\` cho phép Frontend (\`http\://localhost:3000\`, \`http\://localhost:5173\`, \`http\://localhost:4200\`) gọi đầy đủ các method \`GET, POST, PUT, DELETE, PATCH, OPTIONS\` kèm header \`Authorization\`.

  3\. Cấu hình Feign RequestInterceptor trong \`order-service\` và \`catalog-service\` để tự động đính kèm JWT \`Authorization\` header (hoặc \`X-Internal-Secret\`) khi các service gọi nội bộ sang nhau, đồng thời khóa bảo mật cho endpoint \`/api/catalog/inventory/deduct\` không bị gọi trái phép từ bên ngoài.

\---

\#\#\# 🟢 PHASE 7: DOCKERIZE TOÀN HỆ THỐNG, DỮ LIỆU MẪU MỘC HƯƠNG & KIỂM THỬ TỰ ĐỘNG

\#\#\#\# Task 7.1: Tạo Seed Data Chuẩn Nghiệp Vụ Mộc Hương Shop (Nhang, Tinh Dầu, Vòng Gỗ, Chuỗi Hạt)

\- \*\*Yêu cầu thực thi:\*\*

  1\. Tạo migration hoặc \`DataSeeder\` (chỉ chạy ở profile \`dev\`/\`local\` khi bảng trống) khởi tạo:

     \- \*\*3 Tài khoản mẫu\*\* cho 3 Role:

       \- \`admin@mochuong.vn\` (\`Role.ADMIN\`)

       \- \`staff@mochuong.vn\` (\`Role.STAFF\`)

       \- \`customer@mochuong.vn\` (\`Role.CUSTOMER\`)

     \- \*\*5 Danh mục sản phẩm chuẩn Mộc Hương:\*\*

       1\. \*Nhang Trầm Hương Tự Nhiên\* (\`nhang-tram-huong\`)

       2\. \*Trầm Nụ & Hương Vòng\* (\`tram-nu-huong-vong\`)

       3\. \*Tinh Dầu Thiên Nhiên Nguyên Chất\* (\`tinh-dau-thien-nhien\`)

       4\. \*Vòng Gỗ Phong Thủy & Trầm Hương\* (\`vong-go-phong-thuy\`)

       5\. \*Chuỗi Hạt Thiền Định 108 Hạt & Lư Xông\* (\`chuoi-hat-thien-dinh\`)

     \- \*\*10 Sản phẩm mẫu kèm đa biến thể (\`ProductVariant\`) và tồn kho (\`Inventory\`)\*\* (VD: Nhang Trầm Hương Kiến Tự Nhiên Hộp 40 nén / 100 nén; Tinh Dầu Trầm Hương & Đàn Hương 10ml / 30ml; Vòng Tay Trầm Hương Tốc Kiến 108 Hạt 6mm / 8mm; Chuỗi Hạt Gỗ Tử Đàn Ấn Độ...) và \*\*3 Mã giảm giá (\`Coupon\`)\*\* (\`MOCHUONG10\`, \`FREESHIP\`, \`TRAMHUONG50K\`).

\#\#\#\# Task 7.2: Viết Unit Tests & Integration Tests Nghiệm Thu Toàn Bộ Luồng Nghiệp Vụ

\- \*\*Hiện trạng:\*\* Cả 6 service hiện mới chỉ có class \`contextLoads()\` mặc định.

\- \*\*Yêu cầu thực thi:\*\*

  1\. Viết Unit Tests (JUnit 5 \+ Mockito) cho:

     \- \`AuthServiceImplTest\` & \`TokenServiceImplTest\`: Đăng ký, Đăng nhập, Refresh Token Rotation, Quên mật khẩu, Phân quyền 3 Role (\`CUSTOMER\`, \`STAFF\`, \`ADMIN\`).

     \- \`ProductServiceImplTest\` & \`InventoryServiceImplTest\`: Tạo sản phẩm \+ biến thể, Redisson Lock trừ kho, Hoàn kho Saga (\`restoreStock\`), Tính điểm đánh giá \`ReviewServiceImplTest\`.

     \- \`OrderServiceImplTest\` & \`CouponServiceImplTest\`: Kiểm tra áp mã giảm giá (\`PERCENTAGE\`, \`FIXED\_AMOUNT\`, \`FREE\_SHIPPING\`), Checkout COD & VNPay, Chống trùng \`idempotencyKey\`, Hủy đơn hoàn kho & hoàn coupon, Chuyển trạng thái đơn hàng của \`STAFF\`/\`ADMIN\`.

     \- \`EmailServiceTest\`: Kiểm tra lưu \`NotificationLog\` và chống gửi trùng bằng \`idempotencyKey\`.

\#\#\#\# Task 7.3: Hoàn thiện \`Dockerfile\` Multi-Stage & \`docker-compose.yml\` Chạy Toàn Bộ Hệ Thống

\- \*\*Yêu cầu thực thi:\*\*

  1\. Tạo \`Dockerfile\` tối ưu (Multi-stage build với \`eclipse-temurin:21-jdk-alpine\` và \`eclipse-temurin:21-jre-alpine\`) cho từng service.

  2\. Cập nhật \`docker-compose.infra.yml\` và tạo \`docker-compose.services.yml\` kèm hướng dẫn chi tiết trong \`README.md\` để chạy toàn bộ hệ thống bằng 1 lệnh duy nhất.