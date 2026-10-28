# 🌿 MỘC HƯƠNG SHOP — NỀN TẢNG THƯƠNG MẠI ĐIỆN TỬ TRẦM HƯƠNG & KHÔNG GIAN THIỀN TỊNH

> **Dự án cá nhân chuyên sâu**: Hệ thống E-Commerce Microservices kiến trúc phân tán theo phong cách **Đông Phương Thiền Tịnh (Oriental Zen Luxury)**, xây dựng với **Java 21 Spring Boot 3.2.5** và **Next.js 14 App Router (TypeScript, Tailwind CSS)**.

---

## 🏛️ KIẾN TRÚC TOÀN HỆ THỐNG

```
                                  [ KHÁCH HÀNG / QUẢN TRỊ VIÊN ]
                                                │
                                    (Browser: http://localhost:3000)
                                                │
                                  ┌─────────────▼─────────────┐
                                  │   moc-huong-frontend      │  (Next.js 14, Zustand, Tailwind)
                                  └─────────────┬─────────────┘
                                                │ REST API (JSON / Multipart)
                                  ┌─────────────▼─────────────┐
                                  │       API Gateway         │  (Port 8080 - Spring Cloud Gateway)
                                  └───────┬───────┬───────┬───┘
                                          │       │       │
             ┌────────────────────────────┼───────┴───────┼────────────────────────────┐
             │                            │               │                            │
   ┌─────────▼──────────┐       ┌─────────▼──────────┐ ┌──▼───────────────┐ ┌──────────▼──────────┐
   │  identity-service  │       │  catalog-service   │ │  order-service   │ │notification-service│
   │    (Port 8081)     │       │    (Port 8082)     │ │   (Port 8083)    │ │    (Port 8084)     │
   └─────────┬──────────┘       └─────────┬──────────┘ └──┬───────────┬───┘ └──────────┬──────────┘
             │                            │               │           │                │
             │ JWT Authentication         │ Feign Client  │           │ Kafka Events   │
             │                            └───────────────┘           └────────────────┘
             │                                    │                           │
  ┌──────────▼────────────────────────────────────▼───────────────────────────▼──────────┐
  │                           HẠ TẦNG DOCKER COMPOSE                                     │
  │  • MySQL 8.0 (Port 3307 -> 3306)              • Redis Cache & Cart (Port 6379)       │
  │  • Apache Kafka & Zookeeper (Port 9092, 2181) • MailHog SMTP / Web UI (8025 / 1025)  │
  │  • Eureka Discovery Server (Port 8761)                                               │
  └──────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔑 BẢNG TÀI KHOẢN ĐĂNG NHẬP MẪU (3-ROLE AUDIT)

Hệ thống được khởi tạo sẵn dữ liệu mẫu (`DataSeeder`) với 3 vai trò có phân quyền chặt chẽ:

| Vai Trò | Email Đăng Nhập | Mật Khẩu | Quyền Hạn Chính & Giao Diện Được Phép |
| :--- | :--- | :--- | :--- |
| **👑 ADMIN** | `admin@mochuong.vn` | `Admin@123` | Toàn quyền tối cao: Quản trị đơn hàng, Thùng rác sản phẩm (Xóa vĩnh viễn), Tồn kho, Đánh giá, **Quản lý Voucher/Coupons**, **Quản lý Người dùng & Phân quyền**. |
| **💼 STAFF** | `staff@mochuong.vn` | `Staff@123` | Vận hành bán lẻ: Duyệt đơn hàng (State Machine), Gắn mã vận đơn (Tracking & Carrier), Quản lý sản phẩm & tồn kho, Phản hồi đánh giá. *(Bị ẩn & khóa truy cập mục Người dùng, Mã giảm giá, Xóa vĩnh viễn).* |
| **🌸 CUSTOMER**| `customer@mochuong.vn` | `Customer@123` | Trải nghiệm mua sắm: Xem danh mục, Giỏ hàng offline->online, Đặt hàng COD/VNPay, Áp mã giảm giá, Quản lý Sổ địa chỉ, Wishlist, Lịch sử đơn hàng, Đánh giá sản phẩm, Tải hóa đơn PDF. |

---

## 🚀 HƯỚNG DẪN KHỞI CHẠY HỆ THỐNG

### Yêu Cầu Môi Trường
- **Java**: JDK 21 trở lên
- **Node.js**: v18.x hoặc v20.x trở lên (`npm` v9+)
- **Docker & Docker Compose**: Đã cài đặt và đang chạy

---

### BƯỚC 1: Khởi Chạy Hạ Tầng Docker Compose
Từ thư mục gốc dự án, di chuyển vào `aromatics-platform` và kích hoạt các container phụ trợ:

```bash
cd aromatics-platform
docker compose up -d
```

> **Kiểm tra trạng thái container:**
> - MySQL: `localhost:3307` (database: `aromatics_db`, user: `root`, password: `rootpassword`)
> - Redis: `localhost:6379`
> - Kafka: `localhost:9092`
> - MailHog Web UI: `http://localhost:8025`

---

### BƯỚC 2: Khởi Chạy Các Dịch Vụ Backend (Spring Boot 3.2.5)

Bạn có thể chạy toàn bộ 6 dịch vụ thông qua Maven hoặc chạy riêng từng service trên từng terminal:

#### 1. Discovery Server (Khởi động trước tiên)
```bash
cd aromatics-platform/discovery-server
mvn spring-boot:run
# Chờ tới khi console báo Started Eureka Server trên port 8761
```
*Giao diện Eureka Dashboard: [http://localhost:8761](http://localhost:8761)*

#### 2. Khởi chạy 5 Services còn lại:
Mở các cửa sổ terminal riêng cho từng service:
```bash
# Terminal 1: API Gateway
cd aromatics-platform/api-gateway
mvn spring-boot:run

# Terminal 2: Identity Service
cd aromatics-platform/identity-service
mvn spring-boot:run

# Terminal 3: Catalog Service
cd aromatics-platform/catalog-service
mvn spring-boot:run

# Terminal 4: Order Service
cd aromatics-platform/order-service
mvn spring-boot:run

# Terminal 5: Notification Service
cd aromatics-platform/notification-service
mvn spring-boot:run
```

---

### BƯỚC 3: Khởi Chạy Giao Diện Frontend (Next.js 14)

Mở terminal mới tại thư mục gốc:

```bash
cd moc-huong-frontend
npm install
npm run dev
```

Truy cập website tại: **[http://localhost:3000](http://localhost:3000)**

---

## 🧪 KIỂM THỬ BIÊN DỊCH & BẢO ĐẢM CHẤT LƯỢNG (QA)

### 1. Kiểm thử Unit Test & Compile Toàn Bộ Backend:
```bash
cd aromatics-platform
./mvnw clean test
```
*Tất cả 6 modules (`discovery-server`, `api-gateway`, `identity-service`, `catalog-service`, `order-service`, `notification-service`) đạt trạng thái **`BUILD SUCCESS`** với 0 lỗi.*

### 2. Kiểm thử Production Build Toàn Bộ Frontend:
```bash
cd moc-huong-frontend
npm run build
```
*Next.js 14 biên dịch thành công 100% 26 routes tĩnh và động, 0 lỗi TypeScript, 0 lỗi ESLint.*

---

## 📦 CHI TIẾT DANH MỤC CÁC CỔNG VÀ ĐƯỜNG DẪN QUAN TRỌNG

| Thành Phần | Cổng (Port) | Địa Chỉ Truy Cập | Mục Đích |
| :--- | :--- | :--- | :--- |
| **Mộc Hương Web** | `3000` | [http://localhost:3000](http://localhost:3000) | Giao diện khách hàng (Trang chủ, Cửa hàng, Giỏ hàng, Đặt hàng, Tài khoản) |
| **Cổng Quản Trị** | `3000` | [http://localhost:3000/admin](http://localhost:3000/admin) | Cổng vận hành cho `STAFF` và `ADMIN` |
| **API Gateway** | `8080` | [http://localhost:8080](http://localhost:8080) | Cổng tập trung định tuyến toàn bộ REST APIs |
| **Eureka Server** | `8761` | [http://localhost:8761](http://localhost:8761) | Giám sát trạng thái đăng ký của 5 microservices |
| **MailHog UI** | `8025` | [http://localhost:8025](http://localhost:8025) | Hộp thư ảo kiểm tra OTP kích hoạt tài khoản và hóa đơn xác nhận |
| **MySQL Host** | `3307` | `localhost:3307` | Database lưu trữ quan hệ (JPA/Hibernate) |
| **Redis** | `6379` | `localhost:6379` | Cache dữ liệu sản phẩm và Giỏ hàng người dùng |

---

## 🌟 CÁC TÍNH NĂNG TIÊU BIỂU ĐÃ HOÀN THIỆN
1. **Thiết Kế Đông Phương Thiền Tịnh**: Tông màu Mộc Hương Trầm Ấm kết hợp Giấy Dó, Trúc Chỉ, bảng màu HSL tinh tế và hiệu ứng vi chuyển động mượt mà.
2. **Đồng Bộ Giỏ Hàng Thông Minh (Offline -> Online)**: Giỏ hàng khách vãng lai lưu tại `localStorage` tự động đẩy lên Redis và đồng bộ tài khoản ngay khi đăng nhập.
3. **Luồng Thanh Toán Toàn Diện**: Hỗ trợ đồng thời **COD** và **VNPay Sandbox** với tự động tính toán mã giảm giá (Voucher) và tạo hóa đơn PDF tải về máy.
4. **State Machine Đơn Hàng Nghiêm Ngặt**:
   `PENDING_PAYMENT` ➔ `CONFIRMED` ➔ `PREPARING` ➔ `SHIPPING` ➔ `DELIVERED` ➔ `COMPLETED` (hỗ trợ luồng `CANCELLED` và `RETURN_REQUESTED` ➔ `RETURNED`).
5. **Hệ Thống Đánh Giá & Phản Hồi**: Khách hàng đã mua hàng gửi đánh giá kèm số sao; nhân viên kiểm duyệt ẩn/hiện và phản hồi công khai.
6. **Bảo Mật Phân Quyền Đa Tầng**:
   - JWT Auth Filter + Feign `X-Internal-Secret` bảo vệ giao tiếp liên dịch vụ.
   - RBAC phân định rạch ròi giữa `CUSTOMER`, `STAFF` và `ADMIN`.

---
*© 2026 Mộc Hương Shop. Bản quyền thuộc về Le Minh Triet.*