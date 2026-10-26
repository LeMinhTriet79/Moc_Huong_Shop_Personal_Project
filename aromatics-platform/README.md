# 🌿 Mộc Hương Shop — Backend Microservices Platform (`aromatics-platform`)

> **Hệ thống thương mại điện tử chuyên biệt cho sản phẩm hương tự nhiên, trầm hương, nụ trầm, tinh dầu và phụ kiện thưởng trầm.**  
> Được xây dựng theo kiến trúc Microservices phân tán với Spring Boot 3.2.5, Spring Cloud 2023.0.1, Java 21 LTS, MySQL 8.0, Redis 7, Apache Kafka và Docker.

---

## 🏛️ 1. Kiến Trúc Tổng Quan (System Architecture)

```
                            [ Web & Mobile Client ]
                                       │
                                       ▼ (Port 8080)
┌─────────────────────────────────────────────────────────────────────────────┐
│                           API GATEWAY (Spring Cloud)                         │
│   - Global CORS & Rate Limiting                                             │
│   - JWT Bearer Header Routing                                               │
│   - OpenAPI 3.0 / Swagger Aggregator (/swagger-ui.html)                     │
└──────┬───────────────┬─────────────────┬─────────────────┬──────────────────┘
       │               │                 │                 │
       ▼               ▼                 ▼                 ▼
┌──────────────┐┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐
│  IDENTITY    ││   CATALOG    │  │    ORDER     │  │     NOTIFICATION     │
│   SERVICE    ││   SERVICE    │  │   SERVICE    │  │       SERVICE        │
│ (Port 8081)  ││ (Port 8082)  │  │ (Port 8083)  │  │     (Port 8084)      │
└──────┬───────┘└──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘
       │               │                 │                     │
       ▼               ▼                 ▼                     ▼
┌──────────────┐┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐
│ identity_db  ││  catalog_db  │  │   order_db   │  │   notification_db    │
│  (MySQL 8)   ││  (MySQL 8)   │  │  (MySQL 8)   │  │      (MySQL 8)       │
└──────────────┘└──────┬───────┘  └──────┬───────┘  └──────────────────────┘
                       │                 │
                       ▼                 ▼
               ┌───────────────┐ ┌───────────────┐
               │  Redis Cache  │ │  Redis Cart   │
               │ & Distributed │ │ & Idempotency │
               │     Locks     │ │               │
               └───────────────┘ └───────────────┘
                                ▲
                                │
    ════════════════════════════╪═══════════════════════════════════
               APACHE KAFKA EVENT BUS (order.created, order.cancelled, ...)
```

---

## 🚀 2. Danh Sách Dịch Vụ & Cổng Lắng Nghe

| Service | Port nội bộ | Port qua Gateway | Trách nhiệm chính |
| :--- | :---: | :---: | :--- |
| **Service Discovery** | `8761` | — | Eureka Service Registry quản lý service instances |
| **API Gateway** | `8080` | `8080` | Điểm vào duy nhất (Single Entry Point), routing, CORS |
| **Identity Service** | `8081` | `/api/v1/auth/**`, `/api/auth/**`, `/api/users/**` | Xác thực JWT, Refresh Token rotation, Phân quyền, Hồ sơ, Sổ địa chỉ, Yêu thích |
| **Catalog Service** | `8082` | `/api/v1/categories/**`, `/api/v1/products/**`, `/api/products/**`, `/api/reviews/**` | Danh mục, Sản phẩm đa biến thể, Đánh giá, Quản lý tồn kho (Distributed Lock với Redisson), Redis Cache |
| **Order Service** | `8083` | `/api/v1/cart/**`, `/api/v1/orders/**`, `/api/orders/**`, `/api/v1/coupons/**`, `/api/v1/payment/**` | Giỏ hàng Redis, Coupon Engine, Checkout Saga, Quản lý đơn hàng, Thanh toán VNPay, Xuất hóa đơn PDF & Excel |
| **Notification Service**| `8084` | `/api/v1/notifications/**` | Tiêu thụ Kafka event, Email HTML qua JavaMailSender, Mẫu thông báo Flyway, Retry Scheduler |

---

## 🔒 3. Ma Trận Phân Quyền (Role Matrix)

Hệ thống tuân thủ nghiêm ngặt quy tắc phân quyền:
* **`ROLE_CUSTOMER`**: Xem danh mục/sản phẩm, quản lý giỏ hàng, đặt hàng, áp mã giảm giá, theo dõi đơn hàng của chính mình, đánh giá sản phẩm đã mua, quản lý sổ địa chỉ, đổi mật khẩu.
* **`ROLE_STAFF`**: Quản lý kho hàng (nhập kho, xem cảnh báo low-stock), quản lý đơn hàng (chuyển trạng thái, cập nhật ghi chú), quản lý mã giảm giá, xuất file Excel báo cáo.
* **`ROLE_ADMIN`**: Toàn quyền hệ thống, quản lý tài khoản người dùng, phân quyền (CUSTOMER / STAFF / ADMIN), khóa/mở tài khoản, xem thống kê doanh thu nâng cao, quản lý mẫu thông báo email.
* **`ROLE_SYSTEM`**: Dành riêng cho giao tiếp Feign nội bộ liên-service (`X-Internal-Secret: MocHuongSecretKey2026InternalAuth`), cho phép trừ kho đồng bộ và truy vấn thông tin biến thể.

---

## 🌱 4. Dữ Liệu Khởi Tạo Tự Động (Data Seeder)

Khi khởi động lần đầu, hệ thống tự động seed dữ liệu hoàn chỉnh thông qua Spring Boot `CommandLineRunner`:

### 4.1. Tài khoản mẫu (`identity-service`)
* **Admin**: `admin@mochuong.vn` / Mật khẩu: `Admin@123`
* **Staff**: `staff@mochuong.vn` / Mật khẩu: `Staff@123`
* **Customer**: `customer@mochuong.vn` / Mật khẩu: `Customer@123`

### 4.2. Danh mục & Sản phẩm mẫu (`catalog-service`)
* **5 Danh mục**: Trầm Hương Đốt, Nụ Trầm & Nhang Vòng, Tinh Dầu Tự Nhiên, Lư Đốt & Phụ Kiện, Quà Tặng Hương Trầm.
* **10 Sản phẩm đại diện**: Kèm đầy đủ các biến thể (variants: khối lượng, kích cỡ, phân loại) và số lượng tồn kho ban đầu (50 sản phẩm mỗi loại).

### 4.3. Mã giảm giá (`order-service`)
* `MOCHUONG10`: Giảm 10% (tối đa 50.000đ) cho đơn từ 200.000đ.
* `FREESHIP`: Miễn phí vận chuyển (giảm 30.000đ ship) cho đơn từ 300.000đ.
* `TRAMHUONG50K`: Giảm trực tiếp 50.000đ cho đơn từ 500.000đ.

### 4.4. Mẫu Email HTML (`notification-service`)
* `ORDER_CREATED_EMAIL`: Thông báo đặt hàng thành công kèm chi tiết mã đơn và tổng tiền.
* `ORDER_CANCELLED_EMAIL`: Thông báo đơn hàng đã bị hủy.
* `ORDER_STATUS_CHANGED_EMAIL`: Cập nhật trạng thái giao hàng mới nhất.
* `VERIFY_EMAIL_TEMPLATE`: Kích hoạt tài khoản mới.
* `RESET_PASSWORD_TEMPLATE`: Khôi phục mật khẩu bảo mật.

---

## 🛠️ 5. Hướng Dẫn Cài Đặt & Chạy Hệ Thống

### 5.1. Yêu cầu môi trường
* **JDK 21** (Eclipse Temurin hoặc Oracle JDK 21)
* **Maven 3.9+** (hoặc dùng `mvnw` đi kèm)
* **Docker & Docker Compose** (hoặc MySQL 8 + Redis 7 + Kafka cục bộ)

### 5.2. Khởi động hạ tầng với Docker Compose
Chạy MySQL, Redis, Kafka, Zookeeper, Zipkin:
```bash
docker compose -f docker-compose.infra.yml up -d
```
*Kiểm tra trạng thái:*
* MySQL: `localhost:3306` (user: `root`, pass: `rootpass`)
* Redis: `localhost:6379`
* Kafka: `localhost:9092`
* Zipkin Dashboard: `http://localhost:9411`

### 5.3. Build toàn bộ dự án
```bash
# Trên Windows PowerShell / CMD:
mvn clean test-compile

# Hoặc đóng gói JAR:
mvn clean package -DskipTests
```

### 5.4. Chạy kiểm thử tự động toàn diện (Automated Unit Tests)
```bash
mvn test
```
Tất cả các bài kiểm thử tự động (`AuthServiceImplTest`, `InventoryServiceImplTest`, `CouponServiceImplTest`, `OrderServiceImplTest`, `NotificationDispatcherServiceTest`) sẽ được thực thi và xác thực kết quả `BUILD SUCCESS`.

### 5.5. Khởi chạy toàn bộ hệ thống bằng Docker Compose
```bash
# Khởi chạy toàn bộ 6 microservices và hạ tầng:
docker compose up -d --build
```

---

## 📖 6. Tài Liệu API & Swagger
Sau khi khởi chạy Gateway và các dịch vụ:
* **Eureka Service Registry**: `http://localhost:8761`
* **Swagger OpenAPI Gateway Aggregator**: `http://localhost:8080/swagger-ui.html`
* **OpenAPI Docs Identity Service**: `http://localhost:8080/v3/api-docs/identity-service`
* **OpenAPI Docs Catalog Service**: `http://localhost:8080/v3/api-docs/catalog-service`
* **OpenAPI Docs Order Service**: `http://localhost:8080/v3/api-docs/order-service`
