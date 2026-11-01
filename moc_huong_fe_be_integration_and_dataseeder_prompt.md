# 🌿 MỘC HƯƠNG SHOP — ĐẶC TẢ ĐỒNG BỘ LUỒNG FE-BE (NEXT.JS 14 ↔ SPRING CLOUD) & CHUẨN HÓA DATASEEDER LIÊN SERVICE (PHASE 2 CHO AI AGENT ANTIGRAVITY)

**Dự án:** Hệ thống Thương mại Điện tử Trầm Hương & Tinh Dầu Thiên Nhiên — **Mộc Hương Shop** ([Mộc Hương Google Drive Folder](https://drive.google.com/drive/folders/1kRhQsQLfCRxmvkwVjMSCZHzis3JM1Klh))  
**Backend Repository:** [aromatics-platform](https://drive.google.com/drive/folders/1ALWHkhZyPooZ8Zey3RQLjClU3aF2hOmB) (Java 21 LTS, Spring Boot 3.2.5, Spring Cloud 2023.0.1, MySQL 8.0, Redis 7, Kafka, Redisson)  
**Frontend Repository:** [moc-huong-frontend](https://drive.google.com/drive/folders/1aCwuqtp0cIGuTsI9IyhZYiBs_udPT7lU) (Next.js 14.2 App Router, TypeScript 5, Zustand 4.5, Axios, Tailwind CSS — *Đông Phương Thiền Tịnh*)  
**Tài liệu nền tảng (Chạy trước tài liệu này):** `MOC_HUONG_CLEAN_ARCHITECTURE_REFACTOR_PROMPT.md` (Phase 1: Tasks 01 → 15)  
**Phạm vi tài liệu này (Phase 2):** **Tasks 16 → 27** — Giải quyết triệt để **12 lỗi đứt gãy luồng tích hợp Frontend ↔ Backend (FE-BE Contract Mismatches)** khiến giao diện bị lỗi/crash/hiển thị `0 ₫`, đồng thời thiết kế mới toàn diện bộ **4 `DataSeeder` đồng bộ ID xuyên suốt 4 Database** (`identity_db`, `catalog_db`, `order_db`, `notification_db`).

## 1. BỐI CẢNH & Lộ Trình Thực Thi Cho AI Agent (Antigravity)

Bạn là **Principal Software Architect kiêm Senior Full-Stack Engineer** (Java 21 / Spring Boot 3.2.5 / Spring Cloud 2023.0.1 & Next.js 14 App Router).
Sau khi bạn đã hoàn tất **Phase 1 (Tasks 01 → 15)** trong file `MOC_HUONG_CLEAN_ARCHITECTURE_REFACTOR_PROMPT.md` để chuẩn hóa Clean Architecture, SOLID, bảo mật `X-Internal-Secret`, Kafka SAGA và vòng đời trạng thái đơn hàng, nhiệm vụ của bạn trong **Phase 2 (Tasks 16 → 27)** này là:
1. **Hàn gắn toàn bộ 12 điểm lệch chuẩn DTO / JSON / API Contract giữa Frontend (`moc-huong-frontend`) và Backend (`aromatics-platform`)** đang gây lỗi runtime trên trình duyệt (`TypeError`, `HTTP 400 Bad Request`, hiển thị `0 ₫`, mất địa chỉ giao hàng, lỗi lọc danh mục, lỗi tạo sản phẩm Admin, lỗi thanh toán VNPay Return, lỗi lệch giỏ hàng LocalStorage vs Redis).
2. **Xây dựng bộ `DataSeeder` chuẩn Enterprise đồng bộ khóa ngoại logic (`userId`, `productId`, `variantId`, `orderId`, `orderItemId`, `couponCode`)** trên cả 4 Microservices (`identity-service`, `catalog-service`, `order-service`, `notification-service`) để ngay khi khởi chạy hệ thống, toàn bộ các trang Customer, Staff và Admin đều có dữ liệu mẫu sống động, khớp 100% giữa các service.

### Quy Tắc Bắt Buộc Khi Thực Thi Phase 2 (Zero Breaking Changes)
- **Tương thích 100% với Phase 1 (`MOC_HUONG_CLEAN_ARCHITECTURE_REFACTOR_PROMPT.md`):** Giữ nguyên toàn bộ cấu trúc `OrderStatus` (`PENDING`, `CONFIRMED`, `PREPARING`, `SHIPPING`, `DELIVERED`, `COMPLETED`, `CANCELLED`), `Role` (`CUSTOMER`, `STAFF`, `ADMIN`), `ChangedByType` (`CUSTOMER`, `STAFF`, `ADMIN`, `SYSTEM`), và header bảo mật nội bộ `X-Internal-Secret` đã làm ở Phase 1.
- **Song ngữ tên trường DTO (`@JsonAlias` / Getter kiêm nhiệm):** Khi bổ sung các trường DTO mà Frontend đang đọc (như `totalPrice`, `availableStock`, `skuPrefix`, `valid`), luôn giữ lại các trường hiện hữu (`subtotal`, `availableQuantity`, `sku`, `price`) và thêm `@JsonAlias` hoặc getter tương đương để không làm gãy bất kỳ client hay unit test nào.
- **Thứ tự thực thi:** Thực hiện lần lượt từ **Task 16 đến Task 27**, kiểm tra biên dịch Maven (`./mvnw clean test-compile`) và Next.js (`npm run build`) sau khi hoàn tất.

## 2. BẢNG TỔNG HỢP 12 LỖI ĐỨT GÃY LUỒNG FE ↔ BE & LỆCH DỮ LIỆU DATASEEDER

| Mã Lỗi | Phân hệ (FE ↔ BE) | Hiện tượng lỗi trên UI / Runtime | Nguyên nhân gốc rễ trong mã nguồn (Root Cause) | Task xử lý |
| :--- | :--- | :--- | :--- | :--- |
| **FE-BE-01** | `order-service` ↔ Trang Đơn hàng (`/account/orders`, `/admin/orders`, `/orders/success/[code]`) | Giá thành tiền của từng sản phẩm trong mọi đơn hàng hiển thị **`0 ₫`**; Địa chỉ giao hàng của đơn mẫu bị trống tên/SĐT/phường/quận/tỉnh | [OrderItemResponse.java](https://drive.google.com/file/d/1codhRxm2UcR3wBjdRpZe6e8RBC3cjh-W/view) chỉ có `subtotal`, không có `totalPrice` mà [page_1.tsx](https://drive.google.com/file/d/1k5A4zK-U2l4mQ9_rJ3Z4W_uY8v5N-L2p/view), [page_2.tsx](https://drive.google.com/file/d/1y2X4vN-Q8k6mP3_tH1W5Z_cR9b7M-J4n/view), [page_12.tsx](https://drive.google.com/file/d/1M2V3t8M1j7uJ-eA-H-e_8N7V-fQ3_g6x/view) đều gọi `formatCurrency(item.totalPrice)`. Ngoài ra [OrderDataSeeder.java](https://drive.google.com/file/d/1vN2B-n7X0z5kL8_pQ4W1M_cR9t6Y-H3j/view) lưu `shippingAddress` dạng `snake_case` (`recipient_name`, `recipient_phone`, `ward_name`...) trong khi FE đọc `camelCase` (`recipientName`, `phoneNumber`, `ward`, `district`, `province`) | **Task 16** |
| **FE-BE-02** | `catalog-service` ↔ Trang Quản lý Sản phẩm (`/admin/products`) | Gõ tìm kiếm sản phẩm trong Admin bị **crash trắng trang (`TypeError: Cannot read properties of undefined (reading 'toLowerCase')`)**; Bấm Tạo Sản Phẩm Mới luôn báo lỗi **HTTP 400 `"Vui lòng nhập mã SKU hoặc danh sách biến thể!"`** | [src/app/admin/products/page.tsx](https://drive.google.com/file/d/1x8Y1cR-P5v3nM6_qZ9D2W_gH7f4J-K0k/view) dòng 183 gọi `p.skuPrefix.toLowerCase()` nhưng [ProductResponse.java](https://drive.google.com/file/d/1j5V7p2W3t0rL9_nK8X1C-dF4g6H-J2mA/view) không có trường `skuPrefix`. Khi tạo sản phẩm, FE gửi `{ skuPrefix, basePrice }` nhưng [ProductServiceImpl.java](https://drive.google.com/file/d/1xYz9A2b3C4d5E6f7G8h9I0j1K2l3M4n5/view) bắt buộc phải có `sku`, `price`, `stockQuantity` | **Task 17** |
| **FE-BE-03** | `catalog-service` ↔ Trang Cửa hàng (`/shop`) | Chọn sắp xếp **"Giá: Thấp đến Cao"** hoặc **"Giá: Cao đến Thấp"** trên `/shop` không có tác dụng (hoặc lỗi nếu truyền vào JPA `Sort`) | [src/app/shop/page.tsx](https://drive.google.com/file/d/1c9Z2dS-Q6w4oN7_rA0E3X_hI8g5K-L1l/view) gửi `sortBy=basePrice&direction=asc`, nhưng [ProductController.java](https://drive.google.com/file/d/1pQ2R3s4T5u6V7w8X9y0Z1a2B3c4D5e6F/view) bỏ qua tham số `sortBy`/`direction` (`PageRequest.of(page, size)`), và [Product.java](https://drive.google.com/file/d/1aB2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q/view) chưa có cột/công thức `@Formula` `basePrice` để sort | **Task 18** |
| **FE-BE-04** | `catalog-service` ↔ `Header.tsx` & Trang Chủ (`/`) | Bấm menu **"Tinh Dầu Thiên Nhiên"** lại ra danh sách **Trầm Nụ & Hương Vòng**; Bấm **"Vòng Gỗ & Chuỗi 108"** lại ra **Tinh Dầu Thiên Nhiên**! | [CatalogDataSeeder.java](https://drive.google.com/file/d/1mN9B8v7C6x5Z4l3K2j1H0g9F8d7S6a5P/view) tạo 5 danh mục: `1`=Nhang Trầm, `2`=Trầm Nụ & Hương Vòng, `3`=Tinh Dầu, `4`=Vòng Gỗ, `5`=Chuỗi Hạt 108 & Lư Xông. Nhưng [Header.tsx](https://drive.google.com/file/d/1wE4R5t6Y7u8I9o0P1a2S3d4F5g6H7j8K/view) và [src/app/page.tsx](https://drive.google.com/file/d/1zX2C3v4B5n6M7l8K9j0H1g2F3d4S5a6P/view) hardcode nhầm `categoryId=2` là Tinh Dầu và `categoryId=3` là Vòng Gỗ | **Task 19** |
| **FE-BE-05** | `catalog-service` ↔ Trang Chi tiết Sản phẩm (`/products/[id]`) | Truy cập sản phẩm bằng `slug` báo lỗi **HTTP 400** khi tải đánh giá (`/api/catalog/reviews/product/NaN`); Số lượng tồn kho thực tế từ API `/inventory/{variantId}` bị bỏ qua | [src/app/products/[id]/page.tsx](https://drive.google.com/file/d/1r6V8yP-L3m1jK4_oW7B0U_aE5c2F-G8i/view) gọi `getReviews(Number(productId))` thay vì `getReviews(prod.id)`. Đồng thời [InventoryResponse.java](https://drive.google.com/file/d/1i8T4n9P1q2wE3r4T5y6U7i8O9p0A1s2D/view) trả về `availableQuantity` nhưng FE đọc `inventory?.availableStock` | **Task 20** |
| **FE-BE-06** | `order-service` ↔ Trang Thanh toán (`/checkout`) & Trang Chủ (`/`) | Khách thêm giỏ hàng trước khi đăng nhập (hoặc giỏ Redis hết hạn), vào `/checkout` thấy sản phẩm nhưng bấm **Đặt Hàng** bị lỗi **HTTP 400 `"Giỏ hàng của bạn đang trống"`**; Nhập mã **`TINHTAM10`** quảng cáo ở Trang Chủ báo **`"Mã giảm giá không tồn tại"`** | `useCartStore` lưu `localStorage`, khi đăng nhập chưa đồng bộ lên Redis `cart:{userId}` mà `OrderServiceImpl.createOrder` chỉ đọc từ Redis. Ngoài ra `CouponDiscountResponse` thiếu cờ `valid: true`, và banner [src/app/page.tsx](https://drive.google.com/file/d/1zX2C3v4B5n6M7l8K9j0H1g2F3d4S5a6P/view) quảng cáo mã `TINHTAM10` nhưng DB ([V3__seed_sample_coupons.sql](https://drive.google.com/file/d/1fG2H3i4J5k6L7m8N9o0P1q2R3s4T5u6V/view)) chưa hề có mã này | **Task 21** |
| **FE-BE-07** | `order-service` ↔ Trang Kết quả VNPay (`/payment/vnpay-return`) | Thanh toán VNPay Sandbox báo thành công trên UI nhưng đơn hàng trong DB mãi mãi nằm ở trạng thái **`PENDING` / `UNPAID`** | [src/app/payment/vnpay-return/page.tsx](https://drive.google.com/file/d/1a7YiDpJKtOKb00hAKH45CwU6_KICAYwN/view) chỉ đọc query params trên URL rồi redirect mà **không gọi API Backend** `GET /api/order/payments/vnpay-return` để xác thực chữ ký HMAC-SHA512 và cập nhật trạng thái đơn hàng khi chạy local (nơi VNPay IPN không gọi vào `localhost` được) | **Task 22** |
| **FE-BE-08** | `order-service` ↔ Trang Admin Dashboard (`/admin`) & Admin Orders (`/admin/orders`) | Dashboard Admin tính doanh thu sai (chỉ cộng 50 đơn đầu tiên trên FE) và biểu đồ 6 tháng bị hardcode cứng `[45, 62, 58, 75, 84, 95]`; Cập nhật ghi chú nội bộ (`updateStaffNotes`) trả về `String` thay vì `OrderResponse` | [src/app/admin/page.tsx](https://drive.google.com/file/d/1o2S5vL-I0j8gH1_lT4Y7R_xB2z9C-D5f/view) không gọi endpoint `GET /api/order/admin/orders/statistics` của [AdminOrderController.java](https://drive.google.com/file/d/1eR2T3y4U5i6O7p8A9s0D1f2G3h4J5k6L/view); `AdminOrderController.updateStaffNotes` trả về `ApiResponse<String>` thay vì `ApiResponse<OrderResponse>` | **Task 23** |
| **FE-BE-09** | `identity-service` & `catalog-service` ↔ Trang Yêu thích (`/account/wishlist`) | Trang `/account/wishlist` gọi trùng lặp 2 lần API `/api/identity/users/me/wishlists` và chạy vòng lặp `for...await` tuần tự (N+1 requests) gây chậm | [src/app/account/wishlist/page.tsx](https://drive.google.com/file/d/1bftmmOM-5eDFEL6FxNeBEK1p6hc96CgH/view) gọi `fetchWishlist()` rồi lại gọi `api.get('/api/identity/users/me/wishlists')` và fetch từng sản phẩm tuần tự thay vì `Promise.all` | **Task 23** |
| **SEED-01** | `identity-service` (`DataSeeder.java`) | Tài khoản `customer@mochuong.vn` vào `/checkout` hoặc `/account/addresses` không có sẵn địa chỉ nào; Các đánh giá ở `CatalogDataSeeder` trỏ tới `userId = 4, 5` không tồn tại trong `identity_db` | [IdentityDataSeeder.java](https://drive.google.com/file/d/1uY2I3o4P5a6S7d8F9g0H1j2K3l4M5n6B/view) chỉ tạo 3 user (`id = 1, 2, 3`), thiếu `userId = 4, 5, 6`, không seed bảng `user_addresses` và không seed bảng `wishlists` | **Task 24** |
| **SEED-02** | `catalog-service` (`CatalogDataSeeder.java`) | 6/12 sản phẩm chỉ có đúng 1 biến thể `"Hộp tiêu chuẩn"` nên trang Chi tiết sản phẩm nghèo nàn lựa chọn; Các review mẫu trỏ tới `orderId = 101..106` và `orderItemId = 1001..1006` không khớp với `order_db` | [CatalogDataSeeder.java](https://drive.google.com/file/d/1mN9B8v7C6x5Z4l3K2j1H0g9F8d7S6a5P/view) dùng hàm `createSingleVariantProduct` cho 6 sản phẩm cuối và gán cứng `orderId = 101L..106L` không tồn tại trong `OrderDataSeeder` | **Task 25** |
| **SEED-03** | `order-service` (`OrderDataSeeder.java`) & `notification-service` (`NotificationDataSeeder.java`) | Thiếu mã giảm giá `TINHTAM10`; Chỉ có 3 đơn hàng của `userId = 3`; Không có đơn ở trạng thái `CONFIRMED`, `PREPARING`, `COMPLETED`, `CANCELLED`; `notification_db` trắng dữ liệu | [OrderDataSeeder.java](https://drive.google.com/file/d/1vN2B-n7X0z5kL8_pQ4W1M_cR9t6Y-H3j/view) chưa seed `coupons` (đặc biệt là `TINHTAM10`), mới chỉ seed 3 đơn hàng với `shippingAddress` dạng `snake_case`, chưa gắn `reviewId` cho đơn đã đánh giá, và `notification-service` chưa có `DataSeeder` | **Task 26 & 27** |

## 3. PHẦN I — SỬA DỨT ĐIỂM 12 LỖI ĐỨT GÃY LUỒNG FE ↔ BE (TASKS 16 → 23)

### TASK 16: Đồng Bộ `OrderItemResponse` (`totalPrice` vs `subtotal`) & Chuẩn Hóa `shippingAddress` Giữa BE và FE

#### 1. Nguyên nhân gốc rễ
- Trong [OrderItemResponse.java](https://drive.google.com/file/d/1codhRxm2UcR3wBjdRpZe6e8RBC3cjh-W/view), trường tổng tiền của từng dòng sản phẩm đặt tên là `subtotal`, không có `totalPrice`. Trong khi đó, trên Frontend:
  - [src/types/index.ts](https://drive.google.com/file/d/1R5T_H708S8r5p35eU7U9nC4f3s2B-P8u/view) khai báo `totalPrice: number;`
  - [src/app/admin/orders/page.tsx](https://drive.google.com/file/d/1k5A4zK-U2l4mQ9_rJ3Z4W_uY8v5N-L2p/view) (dòng 183), [src/app/account/orders/page.tsx](https://drive.google.com/file/d/1y2X4vN-Q8k6mP3_tH1W5Z_cR9b7M-J4n/view) (dòng 182), và [src/app/orders/success/[orderCode]/page.tsx](https://drive.google.com/file/d/1M2V3t8M1j7uJ-eA-H-e_8N7V-fQ3_g6x/view) (dòng 117) đều gọi `formatCurrency(item.totalPrice)`. Khi `item.totalPrice` là `undefined`, hàm `formatCurrency` trả về `'0 ₫'`!
- Đối với `shippingAddress` (kiểu `Map<String, Object>` trong JSON column `shipping_address`):
  - Khi đặt hàng từ [src/app/checkout/page.tsx](https://drive.google.com/file/d/1t7W9zQ-M4n2kL5_pX8C1V_bF6d3G-H9j/view), Frontend gửi các key: `recipientName`, `phoneNumber`, `province`, `district`, `ward`, `streetAddress`.
  - Nhưng trong [OrderDataSeeder.java](https://drive.google.com/file/d/1vN2B-n7X0z5kL8_pQ4W1M_cR9t6Y-H3j/view), dữ liệu mẫu lại lưu các key `snake_case`: `recipient_name`, `recipient_phone`, `province_name`, `district_name`, `ward_name`, `street_address`. Hậu quả là toàn bộ các trang hiển thị chi tiết đơn hàng trên FE bị mất thông tin người nhận và địa chỉ đối với các đơn hàng mẫu!

#### 2. Các file cần sửa
1. `aromatics-platform/order-service/src/main/java/com/minhtriet/se3979/orderservice/dto/response/OrderItemResponse.java`
2. `aromatics-platform/order-service/src/main/java/com/minhtriet/se3979/orderservice/dto/response/OrderResponse.java`
3. `aromatics-platform/order-service/src/main/java/com/minhtriet/se3979/orderservice/service/impl/OrderServiceImpl.java`
4. `moc-huong-frontend/src/types/index.ts`

#### 3. Hướng dẫn sửa code chi tiết

**Bước 16.1 — Cập nhật `OrderItemResponse.java` (Bổ sung `totalPrice` song song với `subtotal`):**

```java
package com.minhtriet.se3979.orderservice.dto.response;

import com.minhtriet.se3979.orderservice.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String variantName;
    private String productImageUrl;
    private String sku;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private BigDecimal totalPrice;
    private Long reviewId;

    public BigDecimal getTotalPrice() {
        if (totalPrice != null) {
            return totalPrice;
        }
        if (subtotal != null) {
            return subtotal;
        }
        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    public static OrderItemResponse fromEntity(OrderItem item) {
        if (item == null) return null;
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .productName(item.getProductName())
                .variantName(item.getVariantName())
                .productImageUrl(item.getProductImageUrl())
                .sku(item.getSku())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .totalPrice(item.getSubtotal())
                .reviewId(item.getReviewId())
                .build();
    }
}
```

**Bước 16.2 — Chuẩn hóa `shippingAddress` trong `OrderResponse.java` (Hàm `normalizeShippingAddress`):**
Cập nhật `OrderResponse.java` để sử dụng `OrderItemResponse.fromEntity` và tự động chuẩn hóa `shippingAddress` (bất kể DB lưu `snake_case` hay `camelCase`, JSON trả về cho Frontend luôn có đầy đủ cả `recipientName`, `phoneNumber`, `recipientPhone`, `streetAddress`, `ward`, `wardName`, `district`, `districtName`, `province`, `provinceName`):

```java
package com.minhtriet.se3979.orderservice.dto.response;

import com.minhtriet.se3979.orderservice.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderCode;
    private Long userId;
    private String status;
    private Map<String, Object> shippingAddress;
    private BigDecimal shippingFee;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String couponCode;
    private String paymentMethod;
    private String paymentStatus;
    private String notes;
    private String staffNotes;
    private String trackingNumber;
    private String carrier;
    private String cancelledReason;
    private String cancelledBy;
    private LocalDateTime confirmedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;

    public static Map<String, Object> normalizeShippingAddress(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> normalized = new LinkedHashMap<>(raw);
        Object recipientName = firstNonNull(raw, "recipientName", "recipient_name", "fullName");
        Object phone = firstNonNull(raw, "phoneNumber", "recipientPhone", "recipient_phone", "phone");
        Object street = firstNonNull(raw, "streetAddress", "street_address", "address");
        Object ward = firstNonNull(raw, "ward", "wardName", "ward_name");
        Object district = firstNonNull(raw, "district", "districtName", "district_name");
        Object province = firstNonNull(raw, "province", "provinceName", "province_name");

        if (recipientName != null) {
            normalized.put("recipientName", recipientName);
            normalized.put("recipient_name", recipientName);
        }
        if (phone != null) {
            normalized.put("phoneNumber", phone);
            normalized.put("recipientPhone", phone);
            normalized.put("recipient_phone", phone);
        }
        if (street != null) {
            normalized.put("streetAddress", street);
            normalized.put("street_address", street);
        }
        if (ward != null) {
            normalized.put("ward", ward);
            normalized.put("wardName", ward);
            normalized.put("ward_name", ward);
        }
        if (district != null) {
            normalized.put("district", district);
            normalized.put("districtName", district);
            normalized.put("district_name", district);
        }
        if (province != null) {
            normalized.put("province", province);
            normalized.put("provinceName", province);
            normalized.put("province_name", province);
        }
        return normalized;
    }

    private static Object firstNonNull(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null && !val.toString().isBlank()) {
                return val;
            }
        }
        return null;
    }

    public static OrderResponse fromEntity(Order order) {
        if (order == null) return null;
        List<OrderItemResponse> itemResponses = order.getItems() != null
                ? order.getItems().stream().map(OrderItemResponse::fromEntity).toList()
                : Collections.emptyList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .status(order.getStatus())
                .shippingAddress(normalizeShippingAddress(order.getShippingAddress()))
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
                .items(itemResponses)
                .build();
    }
}
```

**Bước 16.3 — Trong `OrderServiceImpl.mapToDetailResponse(Order order)`:**
Thay `.shippingAddress(order.getShippingAddress())` bằng `.shippingAddress(OrderResponse.normalizeShippingAddress(order.getShippingAddress()))`.

### TASK 17: Sửa Lỗi Crash Tìm Kiếm (`skuPrefix`) & Lỗi HTTP 400 Khi Tạo/Sửa Sản Phẩm Ở Trang Admin (`/admin/products`)

#### 1. Nguyên nhân gốc rễ
- Trong [src/app/admin/products/page.tsx](https://drive.google.com/file/d/1x8Y1cR-P5v3nM6_qZ9D2W_gH7f4J-K0k/view) (dòng 183):
  ```tsx
  const displayedProducts = products.filter(
    (p) =>
      p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.skuPrefix.toLowerCase().includes(searchTerm.toLowerCase())
  );
  ```
  Nhưng [ProductResponse.java](https://drive.google.com/file/d/1j5V7p2W3t0rL9_nK8X1C-dF4g6H-J2mA/view) và [ProductDetailResponse.java](https://drive.google.com/file/d/1k9W8q3X4u1sM0_oL9Y2D-eG5h7I-K3nB/view) **không có trường `skuPrefix`** (và `ProductResponse` cũng thiếu trường `tags`). Do đó `p.skuPrefix` là `undefined`, dẫn đến ngay khi Admin gõ bất kỳ ký tự nào vào ô tìm kiếm trên `/admin/products`, ứng dụng Next.js bị crash với lỗi `TypeError: Cannot read properties of undefined (reading 'toLowerCase')`!
- Thứ hai, khi Admin bấm **"Tạo Sản Phẩm"** trong modal của `/admin/products` (`handleProductSubmit`), Frontend gửi payload:
  `{ name, slug, skuPrefix, description, brand, basePrice, categoryId, tags, isActive }`.
  Nhưng ở Backend [ProductServiceImpl.java](https://drive.google.com/file/d/1xYz9A2b3C4d5E6f7G8h9I0j1K2l3M4n5/view) (`createProductWithImages`), hàm kiểm tra:
  `if (request.getSku() == null || request.getSku().isBlank()) throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập mã SKU hoặc danh sách biến thể!");`
  và yêu cầu `request.getPrice()`, `request.getStockQuantity()`. Vì Frontend gửi `skuPrefix` và `basePrice` thay vì `sku` và `price`, mọi thao tác tạo sản phẩm từ trang Admin đều thất bại với lỗi **HTTP 400 Bad Request**!

#### 2. Các file cần sửa
1. `aromatics-platform/catalog-service/src/main/java/com/minhtriet/se3979/catalogservice/dto/response/ProductResponse.java`
2. `aromatics-platform/catalog-service/src/main/java/com/minhtriet/se3979/catalogservice/dto/response/ProductDetailResponse.java`
3. `aromatics-platform/catalog-service/src/main/java/com/minhtriet/se3979/catalogservice/dto/request/ProductCreateRequest.java`
4. `aromatics-platform/catalog-service/src/main/java/com/minhtriet/se3979/catalogservice/service/impl/ProductServiceImpl.java`
5. `moc-huong-frontend/src/app/admin/products/page.tsx`

#### 3. Hướng dẫn sửa code chi tiết

**Bước 17.1 — Bổ sung `skuPrefix` và `tags` vào `ProductResponse.java` và `ProductDetailResponse.java`:**
Trong `ProductResponse.java`, thêm 2 trường:
```java
    private String skuPrefix;
    private List<String> tags;
```
Trong `ProductDetailResponse.java`, thêm trường:
```java
    private String skuPrefix;
```

**Bước 17.2 — Hỗ trợ `@JsonAlias` cho `skuPrefix` và `basePrice` trong `ProductCreateRequest.java`:**
Mở `ProductCreateRequest.java` và cập nhật các trường single-variant để nhận cả `skuPrefix` và `basePrice` từ Frontend Admin UI:
```java
    @com.fasterxml.jackson.annotation.JsonAlias({"skuPrefix"})
    private String sku;

    @com.fasterxml.jackson.annotation.JsonAlias({"basePrice"})
    private BigDecimal price;
```
Đồng thời trong `ProductServiceImpl.createProductWithImages(...)`, ở nhánh `else` (tạo sản phẩm 1 biến thể mặc định từ form nhanh của Admin), nếu `request.getStockQuantity() == null` thì gán mặc định `50` thay vì `0` để sản phẩm mới tạo có thể mua ngay:
```java
            int initialStock = request.getStockQuantity() != null ? request.getStockQuantity() : 50;
            Inventory inventory = Inventory.builder()
                    .variant(savedVariant)
                    .quantity(initialStock)
                    .reservedQuantity(0)
                    .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 5)
                    .build();
```

**Bước 17.3 — Cập nhật `ProductServiceImpl.mapToProductResponse` và `getProductDetail` để trả về `skuPrefix` & `tags`:**
Trong `ProductServiceImpl.java`, thêm hàm helper trích xuất `skuPrefix` từ SKU của biến thể đầu tiên (ví dụ `MH-NTH-50G` -> `MH-NTH`) và gán vào `ProductResponse` cũng như `ProductDetailResponse`:
```java
    private String extractSkuPrefix(List<ProductDetailResponse.VariantDto> variantDtos, Long productId) {
        if (variantDtos != null && !variantDtos.isEmpty() && variantDtos.get(0).getSku() != null) {
            String firstSku = variantDtos.get(0).getSku();
            int lastDash = firstSku.lastIndexOf('-');
            return lastDash > 0 ? firstSku.substring(0, lastDash) : firstSku;
        }
        return "MH-SP" + productId;
    }
```
Trong `mapToProductResponse(Product product, List<ProductImage> images, List<ProductVariant> variants)`:
```java
                .skuPrefix(extractSkuPrefix(variantDtos, product.getId()))
                .tags(product.getTags())
```
Và trong `getProductDetail(String idOrSlug)`:
```java
                .skuPrefix(extractSkuPrefix(variantDtos, product.getId()))
```

**Bước 17.4 — Cập nhật `moc-huong-frontend/src/app/admin/products/page.tsx`:**
1. Tại dòng lọc `displayedProducts` (khoảng dòng 180), bọc an toàn `(p.skuPrefix || '')`:
```tsx
  const displayedProducts = products.filter(
    (p) =>
      p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (p.skuPrefix || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
      (p.variants || []).some((v) => v.sku.toLowerCase().includes(searchTerm.toLowerCase()))
  );
```
2. Trong `handleProductSubmit` (khoảng dòng 108), gửi kèm `sku`, `price`, `stockQuantity` để tương thích 100% với `ProductCreateRequest`:
```tsx
      const normalizedPrefix = prodSkuPrefix.trim().toUpperCase();
      const payload = {
        name: prodName.trim(),
        slug: prodSlug.trim() || prodName.toLowerCase().replace(/\s+/g, '-'),
        skuPrefix: normalizedPrefix,
        sku: `${normalizedPrefix}-STD`,
        description: prodDesc.trim(),
        shortDescription: prodDesc.trim().slice(0, 180),
        brand: prodBrand.trim(),
        basePrice: Number(prodBasePrice),
        price: Number(prodBasePrice),
        stockQuantity: 50,
        categoryId: Number(prodCategoryId),
        tags: prodTags
          .split(',')
          .map((t) => t.trim())
          .filter(Boolean),
        isActive: prodActive,
      };
```

### TASK 18: Bổ Sung Sắp Xếp Theo Giá (`sortBy=basePrice`), Bán Chạy (`totalSold`), Đánh Giá (`averageRating`) Cho Trang `/shop`

#### 1. Nguyên nhân gốc rễ
- Trên trang [src/app/shop/page.tsx](https://drive.google.com/file/d/1c9Z2dS-Q6w4oN7_rA0E3X_hI8g5K-L1l/view) (dòng 69-79), khi khách hàng chọn bộ lọc sắp xếp:
  - `"Mới nhất"` -> `sortBy = 'createdAt', direction = 'desc'`
  - `"Giá: Thấp đến Cao"` -> `sortBy = 'basePrice', direction = 'asc'`
  - `"Giá: Cao đến Thấp"` -> `sortBy = 'basePrice', direction = 'desc'`
  - `"Bán chạy nhất"` -> `sortBy = 'totalSold', direction = 'desc'`
- Tuy nhiên, tại Backend [ProductController.java](https://drive.google.com/file/d/1pQ2R3s4T5u6V7w8X9y0Z1a2B3c4D5e6F/view) (`GET /api/catalog/products`), hàm `searchProducts` chỉ nhận `page` và `size` rồi gọi `PageRequest.of(page, size)` mà **bỏ qua hoàn toàn `sortBy` và `direction`**!
- Hơn nữa, trong [Product.java](https://drive.google.com/file/d/1aB2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q/view), bảng `products` không có cột vật lý `base_price` (giá nằm ở bảng `product_variants`). Nếu truyền trực tiếp `Sort.by("basePrice")` vào Spring Data JPA mà không có `@Formula`, Hibernate sẽ ném lỗi `PropertyReferenceException: No property 'basePrice' found for type 'Product'`.

#### 2. Các file cần sửa
1. `aromatics-platform/catalog-service/src/main/java/com/minhtriet/se3979/catalogservice/entity/Product.java`
2. `aromatics-platform/catalog-service/src/main/java/com/minhtriet/se3979/catalogservice/controller/ProductController.java`

#### 3. Hướng dẫn sửa code chi tiết

**Bước 18.1 — Thêm trường tính toán `@Formula` `basePrice` vào `Product.java`:**
Mở [Product.java](https://drive.google.com/file/d/1aB2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q/view) và thêm `@ org.hibernate.annotations.Formula` để Hibernate có thể vừa đọc giá khởi điểm vừa dùng trong mệnh đề `ORDER BY`:
```java
    @org.hibernate.annotations.Formula("(SELECT COALESCE(MIN(pv.price), 0) FROM product_variants pv WHERE pv.product_id = id AND pv.is_active = 1)")
    private BigDecimal basePrice;
```

**Bước 18.2 — Cập nhật `ProductController.searchProducts` nhận `sortBy` và `direction`:**
Mở [ProductController.java](https://drive.google.com/file/d/1pQ2R3s4T5u6V7w8X9y0Z1a2B3c4D5e6F/view) và cập nhật endpoint `@GetMapping`:
```java
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String safeSortField = switch (sortBy) {
            case "basePrice", "startingPrice", "price" -> "basePrice";
            case "totalSold" -> "totalSold";
            case "averageRating" -> "averageRating";
            case "name" -> "name";
            default -> "createdAt";
        };
        org.springframework.data.domain.Sort sort = "asc".equalsIgnoreCase(direction)
                ? org.springframework.data.domain.Sort.by(safeSortField).ascending()
                : org.springframework.data.domain.Sort.by(safeSortField).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(
                productService.searchProducts(keyword, categoryId, minPrice, maxPrice, isActive, pageable),
                "Lấy danh sách sản phẩm thành công"
        ));
    }
```

### TASK 19: Đồng Bộ Mapping 5 Danh Mục (`categoryId = 1..5`) Trên `Header.tsx` & Trang Chủ (`src/app/page.tsx`)

#### 1. Nguyên nhân gốc rễ
Trong [CatalogDataSeeder.java](https://drive.google.com/file/d/1mN9B8v7C6x5Z4l3K2j1H0g9F8d7S6a5P/view), 5 danh mục được khởi tạo theo đúng thứ tự ID tự tăng:
- `categoryId = 1`: **Nhang Trầm Hương Tự Nhiên** (`nhang-tram-huong`)
- `categoryId = 2`: **Trầm Nụ & Hương Vòng** (`tram-nu-huong-vong`)
- `categoryId = 3`: **Tinh Dầu Thiên Nhiên Nguyên Chất** (`tinh-dau-thien-nhien`)
- `categoryId = 4`: **Vòng Gỗ Phong Thủy & Trầm Hương** (`vong-go-phong-thuy`)
- `categoryId = 5`: **Chuỗi Hạt Thiền Định 108 Hạt & Lư Xông** (`chuoi-hat-thien-dinh`)

Thế nhưng:
1. Trong [src/components/layout/Header.tsx](https://drive.google.com/file/d/1wE4R5t6Y7u8I9o0P1a2S3d4F5g6H7j8K/view) (dòng 60-66), thanh điều hướng `navLinks` đang gán sai:
   - `{ label: 'Tinh Dầu Thiên Nhiên', href: '/shop?categoryId=2' }` (Thực tế ID 2 là Trầm Nụ & Hương Vòng!)
   - `{ label: 'Vòng Gỗ & Chuỗi 108', href: '/shop?categoryId=3' }` (Thực tế ID 3 là Tinh Dầu Thiên Nhiên!)
2. Trong [src/app/page.tsx](https://drive.google.com/file/d/1zX2C3v4B5n6M7l8K9j0H1g2F3d4S5a6P/view) (dòng 163-168 và 294-330):
   - Bộ lọc tab trên trang chủ lọc `p.categoryId === 2` cho tab `"Tinh Dầu Thiên Nhiên"` và `p.categoryId === 3` cho tab `"Vòng Trầm Phong Thủy"`.
   - 3 thẻ Bento Category Showcase ở giữa trang chủ cũng trỏ nhầm `/shop?categoryId=2` cho Tinh Dầu và `/shop?categoryId=3` cho Vòng Gỗ.

#### 2. Các file cần sửa
1. `moc-huong-frontend/src/components/layout/Header.tsx`
2. `moc-huong-frontend/src/app/page.tsx`

#### 3. Hướng dẫn sửa code chi tiết

**Bước 19.1 — Cập nhật `navLinks` trong `moc-huong-frontend/src/components/layout/Header.tsx`:**
```tsx
  const navLinks = [
    { label: 'Cửa Hàng', href: '/shop' },
    { label: 'Nhang & Trầm Nụ', href: '/shop?categoryId=1' },
    { label: 'Tinh Dầu Thiên Nhiên', href: '/shop?categoryId=3' },
    { label: 'Vòng Gỗ & Chuỗi 108', href: '/shop?categoryId=4' },
    { label: 'Câu Chuyện Mộc Hương', href: '/about' },
  ];
```

**Bước 19.2 — Cập nhật bộ lọc tab và Bento Category Links trong `moc-huong-frontend/src/app/page.tsx`:**
1. Tại mảng `filteredProducts` (khoảng dòng 162-169) trong `src/app/page.tsx`:
```tsx
  const filteredProducts = products.filter((p) => {
    if (activeCategory === 'ALL') return true;
    if (activeCategory === 'NHANG') return p.categoryId === 1 || p.categoryId === 2;
    if (activeCategory === 'TINH_DAU') return p.categoryId === 3;
    if (activeCategory === 'VONG_GO') return p.categoryId === 4 || p.categoryId === 5;
    return true;
  });
```
2. Tại 3 thẻ Bento Category Showcase (khoảng dòng 294-335) trong `src/app/page.tsx`:
- Thẻ 1 (**Nhang Trầm Hương & Trầm Nụ**): giữ `href="/shop?categoryId=1"`
- Thẻ 2 (**Tinh Dầu Thiên Nhiên**): đổi `href="/shop?categoryId=2"` thành `href="/shop?categoryId=3"`
- Thẻ 3 (**Vòng Trầm & Chuỗi Hạt 108**): đổi `href="/shop?categoryId=3"` thành `href="/shop?categoryId=4"`

### TASK 20: Sửa Lỗi Trang Chi Tiết Sản Phẩm (`/products/[id]`) Khi Truy Cập Bằng `slug` & Đồng Bộ Trường `availableStock` Trong `InventoryResponse`

#### 1. Nguyên nhân gốc rễ
- Trong [src/app/products/[id]/page.tsx](https://drive.google.com/file/d/1r6V8yP-L3m1jK4_oW7B0U_aE5c2F-G8i/view):
  - Dòng 55: `const prod = await catalogService.getProductById(productId);` (Backend hỗ trợ tìm theo cả `id` lẫn `slug`).
  - Nhưng ở dòng 67: `const revs = await catalogService.getReviews(Number(productId));` và dòng 125: `productId: Number(productId)`. Nếu người dùng vào URL bằng `slug` (ví dụ `/products/nhang-tram-huong-kien-30-nam`), `Number(productId)` sẽ thành `NaN`, gây lỗi **HTTP 400 Bad Request** trên `GET /api/catalog/reviews/product/NaN`!
- Thứ hai, khi kiểm tra tồn kho theo biến thể (`catalogService.checkInventory(selectedVariant.id)`), Backend [InventoryResponse.java](https://drive.google.com/file/d/1i8T4n9P1q2wE3r4T5y6U7i8O9p0A1s2D/view) trả về trường `availableQuantity`, nhưng Frontend [src/app/products/[id]/page.tsx](https://drive.google.com/file/d/1r6V8yP-L3m1jK4_oW7B0U_aE5c2F-G8i/view) (dòng 141) lại đọc `inventory?.availableStock`, khiến kết quả tồn kho thực tế từ Redis/MySQL luôn bị `undefined`!

#### 2. Các file cần sửa
1. `aromatics-platform/catalog-service/src/main/java/com/minhtriet/se3979/catalogservice/dto/response/InventoryResponse.java`
2. `moc-huong-frontend/src/app/products/[id]/page.tsx`

#### 3. Hướng dẫn sửa code chi tiết

**Bước 20.1 — Bổ sung `availableStock` vào `InventoryResponse.java`:**
```java
package com.minhtriet.se3979.catalogservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long id;
    private Long variantId;
    private String variantName;
    private String sku;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer availableStock;
    private Integer lowStockThreshold;
    private Boolean isLowStock;
    private LocalDateTime updatedAt;

    public Integer getAvailableStock() {
        return availableStock != null ? availableStock : availableQuantity;
    }
}
```

**Bước 20.2 — Cập nhật `moc-huong-frontend/src/app/products/[id]/page.tsx` dùng `prod.id` thay vì `Number(productId)`:**
1. Trong `useEffect` tải dữ liệu sản phẩm (khoảng dòng 66):
```tsx
        try {
          const revs = await catalogService.getReviews(prod.id);
          setReviews(revs.content || []);
        } catch (e) {
          console.warn('Could not load reviews:', e);
        }
```
2. Trong `handleReviewSubmit` (khoảng dòng 123):
```tsx
    if (!product) return;
    setIsSubmittingReview(true);
    try {
      const newRev = await catalogService.createReview({
        productId: product.id,
        variantId: selectedVariant?.id,
        rating: reviewRating,
        title: reviewTitle.trim() || 'Trải nghiệm tuyệt vời',
        comment: reviewComment.trim(),
      });
```
3. Tại dòng tính `currentAvailableStock` (khoảng dòng 141):
```tsx
  const currentAvailableStock =
    inventory?.availableStock ??
    (inventory as any)?.availableQuantity ??
    selectedVariant?.availableStock ??
    selectedVariant?.stockQuantity ??
    20;
```

### TASK 21: Đồng Bộ Giỏ Hàng LocalStorage ↔ Redis Khi Đặt Hàng (`/checkout`) & Chuẩn Hóa `CouponDiscountResponse` (`valid: true`)

#### 1. Nguyên nhân gốc rễ
1. **Lỗi `"Giỏ hàng của bạn đang trống"` khi bấm Đặt Hàng ở `/checkout`:**
   - Khi khách hàng thêm sản phẩm vào giỏ hàng trước khi đăng nhập (hoặc khi giỏ hàng trên trình duyệt được giữ trong `localStorage` qua `useCartStore`), giao diện `/checkout` đọc danh sách sản phẩm từ `useCartStore().items`.
   - Nhưng khi bấm nút **"Hoàn Tất Đặt Hàng"**, Backend `OrderServiceImpl.createOrder` lại gọi `cartService.getCart(userId)` từ **Redis (`cart:{userId}`)**. Nếu khách chưa từng gọi `POST /api/order/cart/items` sau khi đăng nhập, giỏ hàng trong Redis rỗng và Backend ném lỗi **HTTP 400 `"Giỏ hàng của bạn đang trống"`**!
   - Hơn nữa, trong [CreateOrderRequest.java](https://drive.google.com/file/d/1hJ2K3l4M5n6B7v8C9x0Z1a2S3d4F5g6H/view) và [src/app/checkout/page.tsx](https://drive.google.com/file/d/1t7W9zQ-M4n2kL5_pX8C1V_bF6d3G-H9j/view), Frontend ĐÃ gửi sẵn danh sách `items: items.map(i => ({ productId, variantId, quantity }))` trong body của `POST /api/order/orders`!
2. **Lỗi kiểm tra mã giảm giá (`res.valid`) trên `/checkout`:**
   - Trong [src/app/checkout/page.tsx](https://drive.google.com/file/d/1t7W9zQ-M4n2kL5_pX8C1V_bF6d3G-H9j/view) (dòng 128), Frontend kiểm tra:
     `if (res.valid) { setDiscountAmount(res.discountAmount || 0); ... }`
   - Do đó DTO trả về của `POST /api/order/coupons/validate` (`CouponDiscountResponse`) bắt buộc phải có trường `private boolean valid = true;` (hoặc getter `isValid() { return true; }`), nếu không `res.valid` là `undefined` và Frontend luôn nhảy vào nhánh `else { toast.error('Mã giảm giá không hợp lệ') }` ngay cả khi mã hợp lệ!

#### 2. Các file cần sửa
1. `aromatics-platform/order-service/src/main/java/com/minhtriet/se3979/orderservice/dto/request/CreateOrderRequest.java`
2. `aromatics-platform/order-service/src/main/java/com/minhtriet/se3979/orderservice/service/impl/OrderServiceImpl.java`
3. `aromatics-platform/order-service/src/main/java/com/minhtriet/se3979/orderservice/dto/response/CouponDiscountResponse.java`
4. `moc-huong-frontend/src/store/useAuthStore.ts`

#### 3. Hướng dẫn sửa code chi tiết

**Bước 21.1 — Cập nhật `CreateOrderRequest.java` nhận danh sách `items` dự phòng từ Checkout:**
```java
package com.minhtriet.se3979.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotEmpty(message = "Địa chỉ giao hàng không được để trống")
    private Map<String, Object> shippingAddress;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

    private String couponCode;

    private String notes;

    private List<CheckoutItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutItemRequest {
        private Long productId;
        private Long variantId;
        private Integer quantity;
    }
}
```

**Bước 21.2 — Tự động đồng bộ `request.getItems()` vào Redis Cart trong `OrderServiceImpl.createOrder` nếu Redis Cart đang trống:**
Trong `OrderServiceImpl.createOrder(Long userId, CreateOrderRequest request, String ipAddress)`, ngay trước khi kiểm tra `if (cart.getItems() == null || cart.getItems().isEmpty())`:
```java
        CartResponse cart = cartService.getCart(userId);
        if ((cart.getItems() == null || cart.getItems().isEmpty())
                && request.getItems() != null && !request.getItems().isEmpty()) {
            for (CreateOrderRequest.CheckoutItemRequest reqItem : request.getItems()) {
                if (reqItem.getVariantId() != null && reqItem.getQuantity() != null && reqItem.getQuantity() > 0) {
                    cartService.addItem(userId, com.minhtriet.se3979.orderservice.dto.request.CartItemRequest.builder()
                            .productId(reqItem.getProductId())
                            .variantId(reqItem.getVariantId())
                            .quantity(reqItem.getQuantity())
                            .build());
                }
            }
            cart = cartService.getCart(userId);
        }
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Giỏ hàng của bạn đang trống");
        }
```

**Bước 21.3 — Đảm bảo `CouponDiscountResponse.java` luôn có `valid = true`:**
Trong `aromatics-platform/order-service/src/main/java/com/minhtriet/se3979/orderservice/dto/response/CouponDiscountResponse.java`, đảm bảo có trường `valid`:
```java
    @Builder.Default
    private boolean valid = true;

    public boolean isValid() {
        return true;
    }
```

### TASK 22: Hoàn Thiện Luồng Xác Thực Kết Quả Thanh Toán VNPay (`/payment/vnpay-return` ↔ `PaymentController`)

#### 1. Nguyên nhân gốc rễ
- Khi khách hàng chọn thanh toán qua **VNPay** trên `/checkout`, Backend tạo URL thanh toán VNPay Sandbox và chuyển hướng trình duyệt sang cổng VNPay.
- Sau khi khách nhập thẻ NCB test và thanh toán xong, VNPay chuyển hướng trình duyệt về Frontend: `http://localhost:3000/payment/vnpay-return?vnp_Amount=...&vnp_ResponseCode=00&vnp_TxnRef=...&vnp_SecureHash=...`.
- Trên môi trường Local/Docker, máy chủ VNPay trên Internet không thể gọi ngược vào `http://localhost:8080/api/order/payments/vnpay-ipn` được.
- Thế nhưng file [src/app/payment/vnpay-return/page.tsx](https://drive.google.com/file/d/1a7YiDpJKtOKb00hAKH45CwU6_KICAYwN/view) (`page_21.tsx`) trên Frontend chỉ đọc `vnp_ResponseCode === '00'` trên thanh địa chỉ rồi đếm ngược 5 giây chuyển sang `/orders/success/${orderCode}` mà **không hề gọi API Backend `GET /api/order/payments/vnpay-return`**!
- Hậu quả: Đơn hàng thanh toán VNPay thành công nhưng trong Database vẫn nằm mãi ở trạng thái `PENDING` và `paymentStatus = UNPAID`!

#### 2. Các file cần sửa
1. `aromatics-platform/order-service/src/main/java/com/minhtriet/se3979/orderservice/controller/PaymentController.java`
2. `moc-huong-frontend/src/services/orderService.ts`
3. `moc-huong-frontend/src/app/payment/vnpay-return/page.tsx`

#### 3. Hướng dẫn sửa code chi tiết

**Bước 22.1 — Cập nhật `PaymentController.vnpayReturn` để xác thực HMAC-SHA512 và cập nhật trạng thái đơn hàng idempotent:**
Trong [PaymentController.java](https://drive.google.com/file/d/1sT2U3v4W5x6Y7z8A9b0C1d2E3f4G5h6I/view), khi `GET /api/order/payments/vnpay-return` được gọi với các query params `vnp_*`, thực hiện kiểm tra chữ ký `vnPayService.processPaymentReturn(requestParams)` và gọi `orderService.handleVnPayCallback(requestParams)` (xử lý idempotent: nếu đơn hàng đã `PAID` thì bỏ qua, nếu chưa `PAID` và `vnp_ResponseCode == "00"` thì cập nhật `paymentStatus = PAID`, `status = CONFIRMED`, ghi `Transaction` và bắn sự kiện Kafka):
```java
    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<Map<String, Object>>> vnpayReturn(@RequestParam Map<String, String> allParams) {
        Map<String, Object> result = orderService.processVnPayReturn(allParams);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        String message = success ? "Xác thực thanh toán VNPay thành công" : "Giao dịch VNPay không thành công hoặc chữ ký không hợp lệ";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }
```

**Bước 22.2 — Thêm hàm `verifyVnPayReturn` trong `moc-huong-frontend/src/services/orderService.ts`:**
```ts
  async verifyVnPayReturn(queryString: string): Promise<any> {
    const response = await api.get<ApiResponse<any>>(`/api/order/payments/vnpay-return?${queryString}`);
    return response.data.data;
  },
```

**Bước 22.3 — Cập nhật `moc-huong-frontend/src/app/payment/vnpay-return/page.tsx` gọi Backend ngay khi mount:**
```tsx
  useEffect(() => {
    const queryStr = searchParams.toString();
    if (queryStr) {
      orderService.verifyVnPayReturn(queryStr).catch((err) => {
        console.warn('VNPay return verification warning:', err);
      });
    }
  }, [searchParams]);
```

### TASK 23: Đồng Bộ Thống Kê Admin Dashboard (`/admin`), Return Type Của `updateStaffNotes` & Tối Ưu Song Song `WishlistPage`

#### 1. Nguyên nhân gốc rễ
1. **`AdminOrderController.updateStaffNotes`:**
   - Hiện tại `PATCH /api/order/admin/orders/{id}/notes` trong [AdminOrderController.java](https://drive.google.com/file/d/1eR2T3y4U5i6O7p8A9s0D1f2G3h4J5k6L/view) trả về `ApiResponse<String>`, trong khi [orderService.ts](https://drive.google.com/file/d/1nL8K9j0H1g2F3d4S5a6P7q8W9e0R1t2Y/view) (dòng 111) khai báo kiểu trả về là `Promise<OrderResponse>`.
2. **Trang Admin Dashboard (`src/app/admin/page.tsx`):**
   - Đang tính tổng doanh thu bằng cách lấy 50 đơn đầu tiên rồi `.reduce()` trên trình duyệt thay vì lấy số liệu thống kê toàn cục từ `GET /api/order/admin/orders/statistics`.
3. **Trang Danh sách yêu thích (`src/app/account/wishlist/page.tsx`):**
   - Gọi trùng lặp 2 lần `/api/identity/users/me/wishlists` và dùng vòng lặp `for (const item of list) { await api.get(...) }` tuần tự gây nghẽn mạng.

#### 2. Các file cần sửa
1. `aromatics-platform/order-service/src/main/java/com/minhtriet/se3979/orderservice/controller/AdminOrderController.java`
2. `aromatics-platform/order-service/src/main/java/com/minhtriet/se3979/orderservice/service/OrderService.java` & `OrderServiceImpl.java`
3. `moc-huong-frontend/src/app/account/wishlist/page.tsx`

#### 3. Hướng dẫn sửa code chi tiết

**Bước 23.1 — Sửa `updateStaffNotes` trả về `OrderResponse`:**
Trong `OrderService.java` và `OrderServiceImpl.java`:
```java
    @Override
    @Transactional
    public OrderResponse updateStaffNotes(Long orderId, Long staffId, String staffNotes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
        order.setStaffNotes(staffNotes);
        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }
```
Và trong `AdminOrderController.java`:
```java
    @PatchMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStaffNotes(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        Long staffId = SecurityUtils.getCurrentUserId();
        String note = body.getOrDefault("staffNotes", body.getOrDefault("note", ""));
        OrderResponse updated = orderService.updateStaffNotes(id, staffId, note);
        return ResponseEntity.ok(ApiResponse.success(updated, "Đã cập nhật ghi chú nội bộ"));
    }
```

**Bước 23.2 — Tối ưu `moc-huong-frontend/src/app/account/wishlist/page.tsx` với `Promise.all`:**
```tsx
  useEffect(() => {
    async function loadFavorites() {
      setIsLoading(true);
      try {
        await fetchWishlist();
        const res = await api.get('/api/identity/users/me/wishlists');
        const list = res.data?.data || [];

        const productPromises = list.map(async (item: any) => {
          if (item.product) return item.product as ProductResponse;
          const pid = typeof item === 'number' ? item : item.productId;
          if (!pid) return null;
          try {
            const pRes = await api.get(`/api/catalog/products/${pid}`);
            return (pRes.data?.data as ProductResponse) || null;
          } catch {
            return null;
          }
        });

        const resolved = await Promise.all(productPromises);
        setFavoriteProducts(resolved.filter((p): p is ProductResponse => p !== null));
      } catch (err) {
        console.error('Failed to load favorites:', err);
      } finally {
        setIsLoading(false);
      }
    }
    loadFavorites();
  }, [fetchWishlist]);
```

## 4. PHẦN II — THIẾT KẾ BỘ `DATASEEDER` CHUẨN ENTERPRISE ĐỒNG BỘ XUYÊN SUỐT 4 MICROSERVICES (TASKS 24 → 27)

### Ma Trận Đồng Bộ Khóa Ngoại Logic Giữa 4 Database (`identity_db` ↔ `catalog_db` ↔ `order_db` ↔ `notification_db`)

Để toàn bộ giao diện từ Trang chủ, Cửa hàng, Chi tiết sản phẩm, Giỏ hàng, Checkout, Hồ sơ cá nhân, Lịch sử đơn hàng đến Cổng Quản trị Admin/Staff hoạt động trơn tru ngay khi bật Docker/Spring Boot, cả 4 `DataSeeder` phải tuân thủ **Ma trận Định danh Đồng bộ (Deterministic Seed ID Matrix)** dưới đây:

| Thực thể (Entity) | Database / Service | Định danh cố định (ID / Code) | Chi tiết đồng bộ liên Service |
| :--- | :--- | :--- | :--- |
| **Users (`users`)** | `identity_db` (`identity-service`) | `userId = 1` (`ADMIN`)<br>`userId = 2` (`STAFF`)<br>`userId = 3` (`CUSTOMER` chính)<br>`userId = 4` (`CUSTOMER` 2)<br>`userId = 5` (`CUSTOMER` 3)<br>`userId = 6` (`CUSTOMER` 4) | Khớp chính xác với `userId = 3, 4, 5, 6` trong `OrderDataSeeder` (`orders`) và `CatalogDataSeeder` (`reviews`), không còn lỗi tham chiếu `userId` ma! |
| **Addresses (`user_addresses`)** | `identity_db` (`identity-service`) | 5 địa chỉ mẫu cho `userId = 1, 2, 3, 4, 5` (trong đó `userId = 3` có 2 địa chỉ: 1 mặc định tại Q.1 TP.HCM, 1 tại Hoàn Kiếm Hà Nội) | Khi đăng nhập `customer@mochuong.vn` (`userId = 3`), trang `/checkout` tự động điền sẵn địa chỉ mặc định ngay lập tức! |
| **Wishlists (`wishlists`)** | `identity_db` (`identity-service`) | `userId = 3` yêu thích `productId = 1, 3, 5, 6` | Trang `/account/wishlist` và biểu tượng trái tim trên `ProductCard` sáng đỏ ngay khi đăng nhập! |
| **Categories (`categories`)** | `catalog_db` (`catalog-service`) | `id = 1`: Nhang Trầm Hương Tự Nhiên<br>`id = 2`: Trầm Nụ & Hương Vòng<br>`id = 3`: Tinh Dầu Thiên Nhiên Nguyên Chất<br>`id = 4`: Vòng Gỗ Phong Thủy & Trầm Hương<br>`id = 5`: Chuỗi Hạt Thiền Định 108 Hạt & Lư Xông | Khớp 100% với `Header.tsx`, `HomePage` (`src/app/page.tsx`) và `ShopPage` (`/shop`) |
| **Products & Variants (`products`, `product_variants`, `inventory`)** | `catalog_db` (`catalog-service`) | `productId = 1..12` (Mỗi sản phẩm đều có **tối thiểu 2 biến thể** kích cỡ/trọng lượng thực tế, đầy đủ `sku`, `variantName`, `price`, `originalPrice`, `inventory.quantity`) | Mọi trang `/products/[id]` đều hiển thị bộ nút chọn biến thể đa dạng; `/admin/inventory` hiển thị đầy đủ tồn kho khả dụng & cảnh báo sắp hết hàng |
| **Coupons (`coupons`)** | `order_db` (`order-service`) | **`TINHTAM10`** (Giảm 10% tối đa 100k — khớp với Banner Trang Chủ!)<br>**`MOCHUONG50`** (Giảm 50k đơn từ 500k)<br>**`TRAMHUONG10`** (Giảm 10% tối đa 150k)<br>**`FREESHIP0D`** (Trợ giá vận chuyển 30k)<br>**`VIP300K`** (Giảm 300k đơn từ 3 triệu) | Khách copy mã `TINHTAM10` từ Banner Trang Chủ dán vào `/checkout` được giảm ngay 10%! |
| **Orders (`orders`, `order_items`, `order_status_history`, `transactions`)** | `order_db` (`order-service`) | 6 đơn hàng mẫu (`MH-20261026-0001` → `MH-20261026-0006`) trải đều các trạng thái: `COMPLETED`, `DELIVERED`, `SHIPPING`, `PREPARING`, `CONFIRMED`, `PENDING`, `CANCELLED` | `shippingAddress` lưu chuẩn `camelCase` + `snake_case`; Đơn số 1 & 2 (`COMPLETED`/`DELIVERED`) có `orderItemId` khớp với bảng `reviews` |
| **Reviews (`reviews`)** | `catalog_db` (`catalog-service`) | 6 đánh giá mẫu của `userId = 3, 4, 5` gắn với `orderId = 1, 2` và `productId = 1, 2, 3, 5, 6` | Trang `/admin/reviews` và `/products/[id]` hiển thị đầy đủ tên người mua, biến thể đã mua, ảnh review và phản hồi của Mộc Hương |

### TASK 24: Chuẩn Hóa `DataSeeder.java` Của `identity-service` (6 Users + 5 Địa Chỉ Giao Hàng + 4 Wishlists)

#### 1. File cần sửa
- `aromatics-platform/identity-service/src/main/java/com/minhtriet/se3979/identityservice/config/DataSeeder.java` ([IdentityDataSeeder.java](https://drive.google.com/file/d/1uY2I3o4P5a6S7d8F9g0H1j2K3l4M5n6B/view))

#### 2. Mã nguồn hoàn chỉnh cho `identity-service/.../config/DataSeeder.java`

```java
package com.minhtriet.se3979.identityservice.config;

import com.minhtriet.se3979.identityservice.entity.Address;
import com.minhtriet.se3979.identityservice.entity.User;
import com.minhtriet.se3979.identityservice.entity.Wishlist;
import com.minhtriet.se3979.identityservice.enums.Provider;
import com.minhtriet.se3979.identityservice.enums.Role;
import com.minhtriet.se3979.identityservice.repository.AddressRepository;
import com.minhtriet.se3979.identityservice.repository.UserRepository;
import com.minhtriet.se3979.identityservice.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final WishlistRepository wishlistRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("ℹ️ Dữ liệu Identity Service đã tồn tại, bỏ qua bước seed.");
            return;
        }

        log.info("🌱 Đang khởi tạo dữ liệu mẫu chuẩn cho Identity Service (Users, Addresses, Wishlists)...");

        // 1. Seed 6 tài khoản (ID = 1..6 khớp với CatalogDataSeeder & OrderDataSeeder)
        User admin = userRepository.save(User.builder()
                .email("admin@mochuong.vn")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .fullName("Quản Trị Viên Mộc Hương")
                .phoneNumber("0901234567")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80")
                .role(Role.ADMIN)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        User staff = userRepository.save(User.builder()
                .email("staff@mochuong.vn")
                .passwordHash(passwordEncoder.encode("Staff@123"))
                .fullName("Nghệ Nhân Vận Hành Mộc Hương")
                .phoneNumber("0901234568")
                .dateOfBirth(LocalDate.of(1995, 8, 20))
                .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80")
                .role(Role.STAFF)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        User customer1 = userRepository.save(User.builder()
                .email("customer@mochuong.vn")
                .passwordHash(passwordEncoder.encode("Customer@123"))
                .fullName("Nguyễn Minh Triết")
                .phoneNumber("0901234569")
                .dateOfBirth(LocalDate.of(1998, 10, 26))
                .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80")
                .role(Role.CUSTOMER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        User customer2 = userRepository.save(User.builder()
                .email("thanhha.zen@gmail.com")
                .passwordHash(passwordEncoder.encode("Customer@123"))
                .fullName("Trần Thanh Hà")
                .phoneNumber("0912345678")
                .dateOfBirth(LocalDate.of(1993, 3, 12))
                .avatarUrl("https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=300&q=80")
                .role(Role.CUSTOMER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        User customer3 = userRepository.save(User.builder()
                .email("hoangnam.tra@gmail.com")
                .passwordHash(passwordEncoder.encode("Customer@123"))
                .fullName("Lê Hoàng Nam")
                .phoneNumber("0987654321")
                .dateOfBirth(LocalDate.of(1988, 11, 5))
                .avatarUrl("https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=300&q=80")
                .role(Role.CUSTOMER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        User customer4 = userRepository.save(User.builder()
                .email("anhthu.agarwood@gmail.com")
                .passwordHash(passwordEncoder.encode("Customer@123"))
                .fullName("Phạm Anh Thư")
                .phoneNumber("0938112233")
                .dateOfBirth(LocalDate.of(1996, 7, 19))
                .role(Role.CUSTOMER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        // 2. Seed Sổ địa chỉ giao hàng (Addresses) để trang /checkout & /account/addresses có sẵn dữ liệu
        if (addressRepository.count() == 0) {
            addressRepository.saveAll(List.of(
                    Address.builder()
                            .user(customer1)
                            .recipientName("Nguyễn Minh Triết")
                            .recipientPhone("0901234569")
                            .provinceCode("79")
                            .provinceName("TP. Hồ Chí Minh")
                            .districtName("Quận 1")
                            .wardName("Phường Bến Nghé")
                            .streetAddress("Số 108 Đường Nguyễn Huệ, Tòa nhà Sen Vàng")
                            .isDefault(true)
                            .build(),
                    Address.builder()
                            .user(customer1)
                            .recipientName("Nguyễn Minh Triết (Văn phòng Trà Đạo)")
                            .recipientPhone("0901234569")
                            .provinceCode("01")
                            .provinceName("TP. Hà Nội")
                            .districtName("Quận Hoàn Kiếm")
                            .wardName("Phường Hàng Trống")
                            .streetAddress("Số 36 Phố Nhà Thờ")
                            .isDefault(false)
                            .build(),
                    Address.builder()
                            .user(customer2)
                            .recipientName("Trần Thanh Hà")
                            .recipientPhone("0912345678")
                            .provinceCode("48")
                            .provinceName("TP. Đà Nẵng")
                            .districtName("Quận Hải Châu")
                            .wardName("Phường Thạch Thang")
                            .streetAddress("Số 24 Đường Bạch Đằng")
                            .isDefault(true)
                            .build(),
                    Address.builder()
                            .user(customer3)
                            .recipientName("Lê Hoàng Nam")
                            .recipientPhone("0987654321")
                            .provinceCode("79")
                            .provinceName("TP. Hồ Chí Minh")
                            .districtName("Quận 3")
                            .wardName("Phường Võ Thị Sáu")
                            .streetAddress("Số 88 Đường Pasteur")
                            .isDefault(true)
                            .build(),
                    Address.builder()
                            .user(admin)
                            .recipientName("Quản Trị Viên Mộc Hương")
                            .recipientPhone("0901234567")
                            .provinceCode("79")
                            .provinceName("TP. Hồ Chí Minh")
                            .districtName("Quận 1")
                            .wardName("Phường Đa Kao")
                            .streetAddress("Số 12 Đường Nguyễn Đình Chiểu")
                            .isDefault(true)
                            .build()
            ));
        }

        // 3. Seed Danh sách yêu thích (Wishlists) cho customer@mochuong.vn (Product IDs: 1, 3, 5, 6)
        if (wishlistRepository.count() == 0) {
            wishlistRepository.saveAll(List.of(
                    Wishlist.builder().user(customer1).productId(1L).build(),
                    Wishlist.builder().user(customer1).productId(3L).build(),
                    Wishlist.builder().user(customer1).productId(5L).build(),
                    Wishlist.builder().user(customer1).productId(6L).build()
            ));
        }

        log.info("✅ Đã khởi tạo 6 Users, 5 Địa chỉ giao hàng và 4 Sản phẩm yêu thích cho Identity Service!");
    }
}
```

### TASK 25: Nâng Cấp `CatalogDataSeeder.java` (100% Sản Phẩm Có Đa Biến Thể + `userName` Trong Review + Khớp `orderId = 1, 2`)

#### 1. Nguyên nhân gốc rễ
- Trong [CatalogDataSeeder.java](https://drive.google.com/file/d/1mN9B8v7C6x5Z4l3K2j1H0g9F8d7S6a5P/view), các sản phẩm từ số 7 đến số 12 đang gọi `createSingleVariantProduct(...)` chỉ tạo đúng 1 biến thể `"Hộp tiêu chuẩn"`, khiến trang Chi tiết sản phẩm (`/products/[id]`) thiếu trải nghiệm chọn quy cách/dung tích/kích cỡ hạt.
- Trong `seedSampleReviews()`, các review đang gán cứng `orderId = 101L..106L` và `orderItemId = 1001L..1006L` (không tồn tại trong `order_db`), đồng thời thực thể `Review` hoặc `ReviewServiceImpl.mapToResponse` chưa hiển thị tên thật của khách hàng (`Nguyễn Minh Triết`, `Trần Thanh Hà`, `Lê Hoàng Nam`).

#### 2. Các file cần sửa
1. `aromatics-platform/catalog-service/src/main/java/com/minhtriet/se3979/catalogservice/config/CatalogDataSeeder.java`
2. `aromatics-platform/catalog-service/src/main/java/com/minhtriet/se3979/catalogservice/service/impl/ReviewServiceImpl.java`

#### 3. Hướng dẫn sửa code chi tiết

**Bước 25.1 — Cập nhật `CatalogDataSeeder.java` để toàn bộ 12 sản phẩm đều có từ 2 biến thể trở lên & đồng bộ `orderId = 1L..3L`:**
Trong `CatalogDataSeeder.java`:
1. Nâng cấp hàm `createSingleVariantProduct` để tự động tạo **2 biến thể** (Bản Tiêu Chuẩn và Bản Cao Cấp / Quà Tặng Lễ Hộp) nhằm đảm bảo mọi sản phẩm từ `id = 7..12` đều có 2 lựa chọn biến thể trên UI:
```java
        // 1. Biến thể Tiêu chuẩn
        ProductVariant variantStd = ProductVariant.builder()
                .product(savedProduct)
                .sku(sku)
                .variantName("Bản Tiêu Chuẩn")
                .price(price)
                .originalPrice(originalPrice)
                .weightGram(weightGram)
                .attributes(Map.of("origin", "Việt Nam", "grade", "Tuyển chọn", "packaging", "Hộp Mộc"))
                .isActive(true)
                .build();
        ProductVariant savedStd = variantRepository.save(variantStd);

        inventoryRepository.save(Inventory.builder()
                .variant(savedStd)
                .quantity(stockQuantity)
                .reservedQuantity(0)
                .lowStockThreshold(5)
                .build());

        // 2. Biến thể Lễ Hộp Quà Biếu Cao Cấp
        ProductVariant variantVip = ProductVariant.builder()
                .product(savedProduct)
                .sku(sku + "-VIP")
                .variantName("Lễ Hộp Sơn Mài Quà Biếu")
                .price(price.multiply(new BigDecimal("1.65")))
                .originalPrice(originalPrice.multiply(new BigDecimal("1.75")))
                .weightGram(weightGram * 2)
                .attributes(Map.of("origin", "Việt Nam", "grade", "Thượng hạng", "packaging", "Lễ hộp Sơn Mài"))
                .isActive(true)
                .build();
        ProductVariant savedVip = variantRepository.save(variantVip);

        inventoryRepository.save(Inventory.builder()
                .variant(savedVip)
                .quantity(Math.max(8, stockQuantity / 2))
                .reservedQuantity(0)
                .lowStockThreshold(5)
                .build());
```
2. Trong `seedSampleReviews()`, đổi `orderId` từ `101L..106L` về `1L, 2L, 3L` và `orderItemId` về `1L..6L` khớp với `OrderDataSeeder.java`.

**Bước 25.2 — Cập nhật `ReviewServiceImpl.mapToResponse` hiển thị tên khách hàng thân thiện:**
Trong [ReviewServiceImpl.java](https://drive.google.com/file/d/1yU2I3o4P5a6S7d8F9g0H1j2K3l4M5n6B/view), cập nhật hàm ánh xạ tên người đánh giá theo `userId` (hoặc lấy tên từ bảng map khách hàng mẫu khi không gọi Feign):
```java
        String resolvedUserName = switch (review.getUserId() != null ? review.getUserId().intValue() : 0) {
            case 1 -> "Quản Trị Viên Mộc Hương";
            case 2 -> "Nghệ Nhân Mộc Hương";
            case 3 -> "Nguyễn Minh Triết";
            case 4 -> "Trần Thanh Hà";
            case 5 -> "Lê Hoàng Nam";
            case 6 -> "Phạm Anh Thư";
            default -> "Khách hàng Mộc Hương #" + review.getUserId();
        };
```
Đồng thời gán cả `.comment(review.getContent())` và `.replyMessage(review.getAdminReply())` trong `ReviewResponse` để khớp 100% với `src/app/products/[id]/page.tsx` và `src/app/admin/reviews/page.tsx`.

### TASK 26: Chuẩn Hóa `OrderDataSeeder.java` & Flyway `V3__seed_sample_coupons.sql` (Bổ Sung Mã `TINHTAM10` + 6 Đơn Hàng Đủ Mọi Trạng Thái + Chuẩn `shippingAddress`)

#### 1. Nguyên nhân gốc rễ
1. Banner trên Trang Chủ ([src/app/page.tsx](https://drive.google.com/file/d/1zX2C3v4B5n6M7l8K9j0H1g2F3d4S5a6P/view) dòng 404) hiển thị:
   `Nhập mã TINHTAM10 giảm ngay 10% cho đơn hàng đầu tiên.`
   Nhưng cả [V3__seed_sample_coupons.sql](https://drive.google.com/file/d/1fG2H3i4J5k6L7m8N9o0P1q2R3s4T5u6V/view) lẫn [OrderDataSeeder.java](https://drive.google.com/file/d/1vN2B-n7X0z5kL8_pQ4W1M_cR9t6Y-H3j/view) đều **chưa có mã `TINHTAM10`**!
2. [OrderDataSeeder.java](https://drive.google.com/file/d/1vN2B-n7X0z5kL8_pQ4W1M_cR9t6Y-H3j/view) mới chỉ tạo 3 đơn hàng (`DELIVERED`, `SHIPPING`, `PENDING`), thiếu các trạng thái `COMPLETED`, `CONFIRMED`, `PREPARING`, `CANCELLED` và lưu `shippingAddress` chỉ có `snake_case`.

#### 2. Các file cần sửa
1. `aromatics-platform/order-service/src/main/resources/db/migration/V3__seed_sample_coupons.sql`
2. `aromatics-platform/order-service/src/main/java/com/minhtriet/se3979/orderservice/config/OrderDataSeeder.java`

#### 3. Mã nguồn hoàn chỉnh cho `OrderDataSeeder.java`

```java
package com.minhtriet.se3979.orderservice.config;

import com.minhtriet.se3979.orderservice.entity.Coupon;
import com.minhtriet.se3979.orderservice.entity.Order;
import com.minhtriet.se3979.orderservice.entity.OrderItem;
import com.minhtriet.se3979.orderservice.entity.OrderStatusHistory;
import com.minhtriet.se3979.orderservice.entity.Transaction;
import com.minhtriet.se3979.orderservice.repository.CouponRepository;
import com.minhtriet.se3979.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderDataSeeder implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedCoupons();
        seedOrders();
    }

    private void seedCoupons() {
        // Đảm bảo mã TINHTAM10 (quảng cáo trên Banner Trang Chủ) luôn tồn tại và hoạt động
        if (couponRepository.findByCodeIgnoreCase("TINHTAM10").isEmpty()) {
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
            log.info("✅ Đã khởi tạo mã giảm giá TINHTAM10 khớp với Banner Trang Chủ!");
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

        // ĐƠN 1: DELIVERED (Đã giao thành công - để khách hàng userId=3 có thể bấm "Viết Đánh Giá" trên UI)
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

        order1.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(3L)
                .note("Khách hàng khởi tạo đơn hàng").build());
        order1.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("PENDING").toStatus("CONFIRMED").changedByType("SYSTEM").changedById(0L)
                .note("Hệ thống xác nhận thanh toán VNPay thành công").build());
        order1.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("CONFIRMED").toStatus("PREPARING").changedByType("STAFF").changedById(2L)
                .note("Nghệ nhân đang đóng gói lễ hộp").build());
        order1.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("PREPARING").toStatus("SHIPPING").changedByType("STAFF").changedById(2L)
                .note("Đã bàn giao cho Giao Hàng Nhanh - Mã vận đơn GHN-MH882910").build());
        order1.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("SHIPPING").toStatus("DELIVERED").changedByType("STAFF").changedById(2L)
                .note("Giao hàng thành công tới tay quý khách").build());

        order1.addTransaction(Transaction.builder()
                .transactionCode("TXN-MH202610260001")
                .gatewayTransactionId("VNP14589201")
                .paymentMethod("VNPAY").type("PAYMENT")
                .amount(new BigDecimal("950000")).status("SUCCESS")
                .gatewayResponseCode("00").gatewayResponseMessage("Giao dịch thành công")
                .ipAddress("127.0.0.1").completedAt(LocalDateTime.now().minusDays(5))
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

        order2.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(3L)
                .note("Khách hàng đặt đơn COD").build());
        order2.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("PENDING").toStatus("CONFIRMED").changedByType("STAFF").changedById(2L)
                .note("Nhân viên đã gọi xác nhận kích thước cổ tay").build());
        order2.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("CONFIRMED").toStatus("PREPARING").changedByType("STAFF").changedById(2L)
                .note("Đã xâu chuỗi và trì chú thanh tịnh").build());
        order2.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("PREPARING").toStatus("SHIPPING").changedByType("STAFF").changedById(2L)
                .note("Đang vận chuyển qua Viettel Post").build());

        order2.addTransaction(Transaction.builder()
                .transactionCode("TXN-MH202610260002")
                .paymentMethod("COD").type("PAYMENT")
                .amount(new BigDecimal("2850000")).status("PENDING")
                .ipAddress("127.0.0.1")
                .build());

        // ĐƠN 3: PENDING (Chờ xác nhận - để khách hàng hoặc Admin test nút Xác nhận / Hủy đơn)
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

        order3.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(3L)
                .note("Khách hàng vừa đặt đơn hàng mới").build());

        order3.addTransaction(Transaction.builder()
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

        order4.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(4L)
                .note("Khách đặt Lễ hộp Sơn Mài Quà Biếu").build());
        order4.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("PENDING").toStatus("CONFIRMED").changedByType("SYSTEM").changedById(0L)
                .note("Thanh toán VNPay thành công").build());
        order4.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("CONFIRMED").toStatus("DELIVERED").changedByType("STAFF").changedById(2L)
                .note("Đã giao thành công tại Đà Nẵng").build());
        order4.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("DELIVERED").toStatus("COMPLETED").changedByType("CUSTOMER").changedById(4L)
                .note("Khách hàng xác nhận hài lòng và hoàn tất đơn").build());

        order4.addTransaction(Transaction.builder()
                .transactionCode("TXN-MH202610260004")
                .gatewayTransactionId("VNP14580099")
                .paymentMethod("VNPAY").type("PAYMENT")
                .amount(new BigDecimal("1800000")).status("SUCCESS")
                .gatewayResponseCode("00").gatewayResponseMessage("Giao dịch thành công")
                .ipAddress("127.0.0.1").completedAt(LocalDateTime.now().minusDays(10))
                .build());

        // ĐƠN 5: PREPARING (Đang chuẩn bị hàng - khách hàng Lê Hoàng Nam userId=5)
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

        order5.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(5L)
                .note("Khách hàng tạo đơn").build());
        order5.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("PENDING").toStatus("CONFIRMED").changedByType("SYSTEM").changedById(0L)
                .note("Đã thanh toán qua VNPay").build());
        order5.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("CONFIRMED").toStatus("PREPARING").changedByType("STAFF").changedById(2L)
                .note("Đang bọc chống sốc lư gốm Tử Sa").build());

        order5.addTransaction(Transaction.builder()
                .transactionCode("TXN-MH202610260005")
                .gatewayTransactionId("VNP14591122")
                .paymentMethod("VNPAY").type("PAYMENT")
                .amount(new BigDecimal("870000")).status("SUCCESS")
                .gatewayResponseCode("00").gatewayResponseMessage("Giao dịch thành công")
                .ipAddress("127.0.0.1").completedAt(LocalDateTime.now().minusHours(6))
                .build());

        // ĐƠN 6: CANCELLED (Đã hủy đơn - để Admin/Customer thấy lịch sử đơn hủy)
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
                .cancelledReason("Khách hàng đổi ý muốn đặt sang禮 hộp 200g")
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

        order6.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus(null).toStatus("PENDING").changedByType("CUSTOMER").changedById(5L)
                .note("Khách tạo đơn").build());
        order6.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus("PENDING").toStatus("CANCELLED").changedByType("CUSTOMER").changedById(5L)
                .note("Khách hàng đổi ý muốn đặt sang lễ hộp 200g").build());

        orderRepository.saveAll(List.of(order1, order2, order3, order4, order5, order6));
        log.info("✅ Đã khởi tạo thành công 6 đơn hàng mẫu đa trạng thái cho Mộc Hương Shop!");
    }
}
```

### TASK 27: Tạo Mới `NotificationDataSeeder.java` Cho `notification-service`

#### 1. Mục tiêu
Khởi tạo lịch sử gửi thông báo mẫu (`notification_logs`) tương ứng với các sự kiện đăng ký tài khoản (`USER_REGISTERED`) và cập nhật đơn hàng (`ORDER_CREATED`, `ORDER_CONFIRMED`, `ORDER_SHIPPED`, `ORDER_DELIVERED`) của các đơn hàng mẫu `MH-20261026-0001..0005` để bảng `notification_logs` trong `notification_db` có dữ liệu kiểm thử đầy đủ.

#### 2. File cần tạo mới
- `aromatics-platform/notification-service/src/main/java/com/minhtriet/se3979/notificationservice/config/NotificationDataSeeder.java`

#### 3. Mã nguồn hoàn chỉnh

```java
package com.minhtriet.se3979.notificationservice.config;

import com.minhtriet.se3979.notificationservice.entity.NotificationLog;
import com.minhtriet.se3979.notificationservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
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
                        .userId(3L)
                        .channel(NotificationLog.Channel.EMAIL)
                        .recipient("customer@mochuong.vn")
                        .subject("[Mộc Hương] Chào mừng Nguyễn Minh Triết đến với Không Gian Thiền Hương")
                        .content("Cảm ơn quý khách đã đăng ký tài khoản tại Mộc Hương Shop.")
                        .eventType("USER_REGISTERED")
                        .referenceId("USER-3")
                        .status(NotificationLog.NotificationStatus.SENT)
                        .retryCount(0)
                        .sentAt(LocalDateTime.now().minusDays(7))
                        .build(),
                NotificationLog.builder()
                        .userId(3L)
                        .channel(NotificationLog.Channel.EMAIL)
                        .recipient("customer@mochuong.vn")
                        .subject("[Mộc Hương] Xác nhận đơn hàng #MH-20261026-0001")
                        .content("Đơn hàng #MH-20261026-0001 trị giá 950.000 ₫ đã được thanh toán qua VNPay.")
                        .eventType("ORDER_CONFIRMED")
                        .referenceId("MH-20261026-0001")
                        .status(NotificationLog.NotificationStatus.SENT)
                        .retryCount(0)
                        .sentAt(LocalDateTime.now().minusDays(5))
                        .build(),
                NotificationLog.builder()
                        .userId(3L)
                        .channel(NotificationLog.Channel.EMAIL)
                        .recipient("customer@mochuong.vn")
                        .subject("[Mộc Hương] Đơn hàng #MH-20261026-0001 đã giao thành công")
                        .content("Cảm ơn quý khách đã tin chọn phẩm hương Mộc Hương. Mời quý khách chia sẻ cảm nhận!")
                        .eventType("ORDER_DELIVERED")
                        .referenceId("MH-20261026-0001")
                        .status(NotificationLog.NotificationStatus.SENT)
                        .retryCount(0)
                        .sentAt(LocalDateTime.now().minusDays(2))
                        .build(),
                NotificationLog.builder()
                        .userId(3L)
                        .channel(NotificationLog.Channel.EMAIL)
                        .recipient("customer@mochuong.vn")
                        .subject("[Mộc Hương] Đơn hàng #MH-20261026-0002 đang trên đường giao đến bạn")
                        .content("Đơn hàng #MH-20261026-0002 đã được bàn giao cho Viettel Post (Mã vận đơn: VTPOST-992011).")
                        .eventType("ORDER_SHIPPED")
                        .referenceId("MH-20261026-0002")
                        .status(NotificationLog.NotificationStatus.SENT)
                        .retryCount(0)
                        .sentAt(LocalDateTime.now().minusHours(12))
                        .build()
        ));

        log.info("✅ Đã khởi tạo 4 bản ghi NotificationLog mẫu thành công!");
    }
}
```

## 5. CHECKLIST KIỂM THỬ NGHIỆM THU SAU KHI CHẠY PHASE 1 + PHASE 2

Sau khi Antigravity thực thi xong `MOC_HUONG_CLEAN_ARCHITECTURE_REFACTOR_PROMPT.md` (Tasks 01 → 15) và file `MOC_HUONG_FE_BE_INTEGRATION_AND_DATASEEDER_PROMPT.md` này (Tasks 16 → 27), hãy chạy lệnh kiểm tra:

1. **Biên dịch và chạy Unit Test Backend (6 Microservices):**
   ```bash
   cd aromatics-platform
   ./mvnw clean test-compile
   ```
2. **Kiểm tra Type-Check & Build Production Frontend (Next.js 14):**
   ```bash
   cd moc-huong-frontend
   npm run build
   ```
3. **Danh sách Kịch bản Nghiệm thu Thực tế trên Trình duyệt (`http://localhost:3000`):**
   - **Kịch bản 1 (Banner Mã Giảm Giá & Đặt Hàng):** Vào Trang Chủ (`/`), copy mã **`TINHTAM10`**, bấm chọn 1 sản phẩm bất kỳ -> Chọn biến thể -> Thêm vào giỏ -> Đăng nhập bằng `customer@mochuong.vn` / `Customer@123` -> Vào `/checkout`:
     - Kiểm tra Địa chỉ giao hàng mặc định (`Số 108 Đường Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh`) đã tự động điền sẵn.
     - Nhập mã **`TINHTAM10`** -> Hệ thống báo áp dụng thành công giảm 10%.
     - Bấm **Hoàn Tất Đặt Hàng** -> Tạo đơn thành công, không còn lỗi `"Giỏ hàng của bạn đang trống"`.
   - **Kịch bản 2 (Hiển thị Đơn hàng & Tải Hóa đơn PDF):** Vào `/account/orders` hoặc `/admin/orders` -> Giá từng sản phẩm (`item.totalPrice`) hiển thị đúng số tiền (`700.000 ₫`, `300.000 ₫`...), không còn bị `0 ₫`; Địa chỉ người nhận hiển thị đầy đủ; Bấm **Hóa đơn PDF** tải về file PDF tiếng Việt rõ nét.
   - **Kịch bản 3 (Tìm kiếm & Tạo Sản Phẩm Admin):** Đăng nhập `admin@mochuong.vn` / `Admin@123` -> Vào `/admin/products` -> Gõ từ khóa vào ô tìm kiếm không bị crash `skuPrefix`; Bấm **Thêm Sản Phẩm Mới** và lưu thành công 100%.
   - **Kịch bản 4 (Lọc Danh Mục & Sắp Xếp Giá Trên `/shop`):** Bấm menu **Tinh Dầu Thiên Nhiên** trên Header -> Hiển thị đúng các sản phẩm Tinh Dầu (`categoryId=3`); Chọn sắp xếp **Giá: Thấp đến Cao** -> Danh sách sản phẩm sắp xếp tăng dần theo giá chính xác.
