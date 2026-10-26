---

## name: moc-huong-microservices-architect description: Bộ luật kiến trúc (AI Agent Rules & Skill) dành riêng cho dự án Mộc Hương Shop (aromatics-platform) sử dụng Java 21, Spring Boot 3.2.5, Spring Cloud 2023.0.1, MySQL 8.0, Redis/Redisson, Kafka, Cloudinary và VNPay.

# 🏛️ BỘ LUẬT KIẾN TRÚC & KỸ NĂNG AI AGENT (AI SKILL / SYSTEM RULES) — DỰ ÁN MỘC HƯƠNG SHOP (`aromatics-platform`)

> **Cách cài đặt cho AI Coding Agent (Antigravity / Cursor / Windsurf / Cline / Copilot):**  
> 

> - **Trên Antigravity / Gemini Agent:** Lưu file này vào `.agent/skills/moc-huong-architect/SKILL.md` hoặc nạp trực tiếp vào phần **Rules / System Instructions** của Workspace.  
> - **Trên Cursor / Windsurf:** Copy nội dung file này vào `.cursorrules` hoặc `.windsurfrules` ở thư mục gốc `aromatics-platform/`.

---

## 1\. BỐI CẢNH NGHIỆP VỤ & KIẾN TRÚC HỆ THỐNG (SYSTEM TOPOLOGY)

Bạn đang làm việc trên hệ thống E-Commerce Microservices **Mộc Hương Shop (`aromatics-platform`)** chuyên bán **Nhang trầm, Hương thảo mộc, Tinh dầu thiên nhiên, Vòng gỗ phong thủy và Chuỗi hạt thiền định**.

### 1.1. Danh sách 6 Microservices & Cổng kết nối cố định

1. **`service-discovery`** (Port `8761`): Netflix Eureka Server. Package gốc: `com.minhtriet.se3979.servicediscovery`.  
2. **`api-gateway`** (Port `8080`): Spring Cloud Gateway (WebFlux Reactive). Package gốc: `com.minhtriet.se3979.apigateway`.  
3. **`identity-service`** (Port `8081`): Quản lý Xác thực (JWT \+ RefreshToken \+ EmailVerification), Người dùng, Sổ địa chỉ, Danh sách yêu thích (Wishlist). Package gốc: `com.minhtriet.se3979.identityservice`. Database: `identity_db`.  
4. **`catalog-service`** (Port `8082`): Quản lý Danh mục đa cấp, Sản phẩm, Đa biến thể (`ProductVariant`), Ảnh Cloudinary (`ProductImage`, `ReviewImage`), Tồn kho (`Inventory` với Redisson Distributed Lock \+ Pessimistic DB Lock), Đánh giá sản phẩm (`Review`), Cache Redis (`ProductCacheService`). Package gốc: `com.minhtriet.se3979.catalogservice`. Database: `catalog_db`.  
5. **`order-service`** (Port `8083`): Quản lý Giỏ hàng Redis (`CartItem`), Đặt hàng (`Order`, `OrderItem`), Mã giảm giá (`Coupon`, `CouponUsage`), Giao dịch thanh toán (`Transaction` \- VNPay/MoMo/COD), Lịch sử trạng thái đơn (`OrderStatusHistory`), Xuất Hóa đơn PDF (`iText7`) & Báo cáo Excel (`Apache POI`). Package gốc: `com.minhtriet.se3979.orderservice`. Database: `order_db`.  
6. **`notification-service`** (Port `8084`): Lắng nghe sự kiện Kafka, quản lý Mẫu thông báo (`NotificationTemplate`), gửi Email HTML (Gmail SMTP \+ Thymeleaf) / SMS (Twilio), ghi nhận Lịch sử thông báo chống trùng lặp (`NotificationLog`). Package gốc: `com.minhtriet.se3979.notificationservice`. Database: `notification_db`.

### 1.2. Tech Stack Chuẩn Xác (KHÔNG ĐƯỢC TỰ Ý THAY ĐỔI)

- **Java Version:** Java 21\.  
- **Spring Boot Version:** `3.2.5` (Spring Cloud `2023.0.1`).  
- **Database:** **MySQL 8.0** (`com.mysql:mysql-connector-j`, `org.flywaydb:flyway-mysql`).  
  - ⚠️ **CẢNH BÁO QUAN TRỌNG:** Toàn bộ các bảng trong Flyway (`V1__init_*.sql`) sử dụng `BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY` và kiểu `DATETIME(3)`. Trong Java Entity, mọi khóa chính (`id`) và khóa ngoại tham chiếu (`userId`, `productId`, `variantId`, `orderId`, `orderItemId`) **BẮT BUỘC dùng kiểu `Long`** (`@GeneratedValue(strategy = GenerationType.IDENTITY)`). Tuyệt đối **KHÔNG** dùng `UUID` cho `@Id` của Entity\!

---

## 2\. LUẬT PHÂN QUYỀN & VAI TRÒ TRONG HỆ THỐNG (RBAC RULES)

Hệ thống có chính xác **3 User Roles** (khai báo trong `com.minhtriet.se3979.identityservice.enums.Role` và cột `users.role` trong MySQL) và **1 System Actor**:

### 2.1. Ba User Roles (`CUSTOMER`, `STAFF`, `ADMIN`)

- **`CUSTOMER`** (Mặc định khi đăng ký tài khoản):  
  - Trong JWT Claim `"role"` lưu giá trị `"CUSTOMER"`, khi nạp vào Spring Security `SimpleGrantedAuthority` sẽ là `"ROLE_CUSTOMER"`.  
  - Quyền hạn: Xem danh mục & sản phẩm (Public), quản lý thông tin cá nhân (`/me`), sổ địa chỉ (`/me/addresses`), danh sách yêu thích (`/me/wishlists`), giỏ hàng (`/api/order/cart/**`), áp mã giảm giá, đặt hàng (`checkout`), thanh toán VNPay, hủy đơn của chính mình khi đang `PENDING_PAYMENT`, yêu cầu trả hàng khi `DELIVERED`, và viết/sửa/xóa đánh giá sản phẩm của chính mình.  
  - ⚠️ **Cấm dùng `"ROLE_USER"`**: Trong dự án này không có role `USER`. Luôn sử dụng `CUSTOMER`.  
- **`STAFF`** (Nhân viên vận hành):  
  - Trong Spring Security là `"ROLE_STAFF"`.  
  - Quyền hạn: Được truy cập các API quản lý vận hành hàng ngày với `@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")`, bao gồm: CRUD \+ Xóa mềm \+ Khôi phục Danh mục & Sản phẩm & Biến thể, Cập nhật/Nhập kho (`Inventory`), Xem và chuyển trạng thái Đơn hàng (`CONFIRMED` \-\> `PREPARING` \-\> `SHIPPING` \-\> `DELIVERED` \-\> `COMPLETED`), ghi `staff_notes`, trả lời đánh giá (`adminReply`) và ẩn/hiện đánh giá (`toggleVisibility`).  
- **`ADMIN`** (Quản trị viên cao nhất):  
  - Trong Spring Security là `"ROLE_ADMIN"`.  
  - Quyền hạn: Kế thừa mọi quyền của `STAFF`, và độc quyền (`@PreAuthorize("hasRole('ADMIN')")`) đối với: Quản lý người dùng (`/api/identity/admin/users/**` \- khóa/mở khóa user, phân quyền `STAFF`), Xóa vĩnh viễn sản phẩm (`DELETE /api/catalog/products/{id}/force`), Quản lý cấu hình hệ thống & mẫu thông báo.

### 2.2. Một System Actor (`SYSTEM`)

- Được định nghĩa trong cột `cancelled_by` và `changed_by_type` (`ENUM('CUSTOMER','STAFF','ADMIN','SYSTEM')`) của bảng `orders` và `order_status_history`.  
- Dùng khi hệ thống tự động thực hiện hành động (ví dụ: Scheduled Job tự động hủy đơn VNPay quá hạn 15 phút chưa thanh toán, hoặc hệ thống tự động huỷ đơn khi VNPay callback báo giao dịch thất bại).

---

## 3\. LUẬT THIẾT KẾ DATABASE, FLYWAY & JPA ENTITY (DATA LAYER RULES)

1. **Bất khả xâm phạm Flyway Migrations cũ:**  
   - Tuyệt đối KHÔNG sửa nội dung hoặc đổi tên các file SQL đã tồn tại (`V1__init_identity_db.sql`, `V1__init_catalog_db.sql`, `V2__add_cloudinary_id.sql`, `V3__add_is_active_to_products.sql`, `V4__update_review_for_images.sql`, `V1__init_order_db.sql`, `V1__init_notification_db.sql`) để tránh lỗi Flyway Checksum Mismatch.  
   - Mọi thay đổi schema mới phải tạo file migration version tiếp theo (`V2__...` hoặc `V5__...`).  
2. **Quy tắc Cross-Service Reference (Database-per-Service):**  
   - Mỗi Microservice sở hữu 1 Database MySQL độc lập (`identity_db`, `catalog_db`, `order_db`, `notification_db`).  
   - Khi tham chiếu thực thể của Service khác (ví dụ `Order` lưu `userId`, `OrderItem` lưu `productId` và `variantId`, `Review` lưu `userId` và `orderItemId`, `Wishlist` lưu `productId`), **CHỈ lưu cột ID kiểu `Long`** (`@Column(name = "..._id", nullable = false)`), tuyệt đối KHÔNG dùng `@ManyToOne` nối sang Entity của service khác.  
3. **Quy tắc Ánh xạ Cột JSON trong MySQL 8.0:**  
   - Với các cột kiểu `JSON` trong MySQL (`products.tags`, `product_variants.attributes`, `orders.shipping_address`, `coupons.applicable_product_ids`, `coupons.applicable_category_ids`, `transactions.raw_response`, `notification_templates.variables_schema`), sử dụng `@JdbcTypeCode(SqlTypes.JSON)` của Hibernate 6\.  
4. **Quy tắc Quản lý Ảnh Cloudinary & Compensating Cleanup (Giỏ Rác Tạm):**  
   - Mọi luồng upload ảnh lên Cloudinary (như `ProductServiceImpl.createProductWithImages` hoặc `ReviewServiceImpl.createReview`) **BẮT BUỘC** phải lưu `public_id` vào danh sách tạm `List<String> uploadedCloudinaryIds`.  
   - Trong khối `catch (Exception e)`, nếu quá trình lưu Database bị lỗi và rollback, phải lặp qua `uploadedCloudinaryIds` gọi `cloudinaryService.deleteImage(publicId)` để dọn sạch ảnh rác trên mây trước khi ném ngoại lệ.  
   - Khi xóa vĩnh viễn (`hardDelete`) hoặc thay ảnh mới (`updateCategory`, `updateProduct`, `deleteReview`), phải xóa ảnh cũ trên Cloudinary dựa vào `cloudinaryPublicId`.

---

## 4\. LUẬT GIAO TIẾP ĐỒNG BỘ (FEIGN) & BẤT ĐỒNG BỘ (KAFKA & SAGA)

### 4.1. Luật Khóa Phân Tán Tồn Kho (Redisson \+ Pessimistic Lock)

- Mọi thao tác trừ kho (`deductStock`) hoặc hoàn kho (`restoreStock`) trên bảng `inventory` trong `catalog-service` **BẮT BUỘC** phải tuân thủ mẫu khóa 2 lớp:  
  1. Lấy khóa phân tán Redisson: `RLock lock = redissonClient.getLock("lock:inventory:variant:" + variantId);` với `lock.tryLock(3, 10, TimeUnit.SECONDS)`.  
  2. Luôn nhả khóa trong khối `finally`:  
       
     finally {  
       
         if (lock.isHeldByCurrentThread()) {  
       
             lock.unlock();  
       
         }  
       
     }

### 4.2. Luật Saga Đặt Hàng & Tránh Lỗi Trừ Kho Kép (No Dual-Deduction)

- **Khi đặt hàng (`checkout`):**  
  - Kiểm tra `idempotencyKey` trước tiên để chống bấm đúp tạo 2 đơn hàng.  
  - Gọi đồng bộ `catalogFeignClient.deductInventory(...)` để giữ/trừ tồn kho ngay lập tức (Fail-fast nếu hết hàng). Nếu bất kỳ món nào trong giỏ hàng bị thiếu tồn kho, phải hoàn trả tồn kho cho các món đã trừ trước đó trong cùng phiên hoặc thực hiện trừ kho theo lô (`deductBatch`) có tính nguyên tử (`@Transactional`) bên phía `catalog-service`.  
  - **QUY TẮC SỐNG CÒN:** Vì tồn kho đã được trừ đồng bộ qua Feign lúc tạo đơn (`checkout`), sự kiện Kafka `order.created` **CHỈ** dùng để `notification-service` gửi email xác nhận đơn hàng (và cập nhật thống kê), **`catalog-service` KHÔNG ĐƯỢC trừ kho lần thứ 2 khi nhận `order.created`\!**  
- **Khi hủy đơn (`cancelOrder`) hoặc Thanh toán VNPay thất bại / hết hạn:**  
  - `order-service` chuyển trạng thái đơn sang `CANCELLED`, ghi lịch sử vào `order_status_history`, hoàn lại lượt dùng mã giảm giá (`CouponUsage.isRefunded = true`), và phát sự kiện Kafka vào topic `order.cancelled`.  
  - `catalog-service` lắng nghe topic `order.cancelled` (`groupId = "catalog-group"`) và gọi `inventoryService.restoreStock(variantId, quantity)` để cộng trả lại hàng vào kho (Compensating Transaction).

### 4.3. Luật Kafka Payload & Idempotency

- **Cấm gửi JPA Entity qua Kafka:** Tuyệt đối không truyền trực tiếp `Order` hay `User` entity vào `kafkaTemplate.send(...)`. Bắt buộc map sang DTO phẳng (`OrderCreatedEvent`, `OrderCancelledEvent`, `OrderStatusChangedEvent`, `AuthNotificationEvent`) có chứa đầy đủ `userEmail` và `List<OrderItemEventDto>`.  
- **Chống gửi trùng thông báo:** Trong `notification-service`, mọi sự kiện nhận từ Kafka phải sinh mã `idempotencyKey` (VD: `"ORDER_CREATED_" + orderCode`) và kiểm tra/lưu vào bảng `notification_logs` trước khi gửi mail qua `JavaMailSender`.

---

## 5\. LUẬT CODE STYLE, DTO, VALIDATION & BẢO MẬT

1. **Cấu trúc Package chuẩn trong mỗi Microservice:**  
     
   com.minhtriet.se3979.\<servicename\>  
     
   ├── config/          (OpenApiConfig, SecurityConfig, RedissonConfig, CloudinaryConfig, FeignConfig)  
     
   ├── controller/      (REST Controllers \- chỉ điều phối Request/Response, không viết business logic)  
     
   ├── dto/  
     
   │   ├── request/     (Các class Request có gắn @NotBlank, @NotNull, @Min, @Max, @Email)  
     
   │   ├── response/    (ApiResponse, ErrorResponse và các Response DTO)  
     
   │   └── event/       (Các Kafka Event DTO)  
     
   ├── entity/          (JPA Entities ánh xạ 1-1 với bảng MySQL trong Flyway)  
     
   ├── enums/           (Các Enum trạng thái và phân loại)  
     
   ├── exception/       (AppException, GlobalExceptionHandler)  
     
   ├── feign/           (Các interface @FeignClient)  
     
   ├── kafka/           (Event Publishers & @KafkaListener Consumers)  
     
   ├── redis/           (CartItem, Cache Services)  
     
   ├── repository/      (Spring Data JPA Repositories)  
     
   ├── security/        (JwtUtil, JwtAuthFilter, SecurityUtils)  
     
   └── service/  
     
       ├── \<Name\>Service.java  
     
       └── impl/\<Name\>ServiceImpl.java  
     
2. **Quy tắc Viết Code sạch & An toàn:**  
   - Sử dụng `@RequiredArgsConstructor` của Lombok để Constructor Injection (`private final ...`), không dùng `@Autowired` trên field.  
   - Trên JPA Entity, sử dụng `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`, **KHÔNG dùng `@Data`** trên JPA Entity có quan hệ `@OneToMany` / `@ManyToOne` để tránh lỗi `StackOverflowError` trong `toString()` / `hashCode()`.  
   - Mọi hàm thay đổi dữ liệu nhiều bảng phải gắn `@Transactional` (của `org.springframework.transaction.annotation.Transactional`).  
   - Mọi số tiền (`price`, `subtotal`, `discountAmount`, `shippingFee`, `totalAmount`) bắt buộc dùng `BigDecimal`, tuyệt đối không dùng `Double` hay `Float` để tính toán tiền tệ.  
   - Sau khi hoàn thành bất kỳ Task nào, Agent phải tự kiểm tra biên dịch và chạy test bằng lệnh `mvn clean test` để đảm bảo 0 lỗi biên dịch và 100% test xanh.