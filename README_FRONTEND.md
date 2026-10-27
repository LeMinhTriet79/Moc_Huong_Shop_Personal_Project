# Mộc Hương Shop — Frontend Kiến Trúc & Hướng Dẫn Vận Hành

Dự án Frontend thương mại điện tử Mộc Hương được xây dựng trên nền tảng **Next.js 14 (App Router, TypeScript, Tailwind CSS)**, kế thừa trọn vẹn 100% tinh thần thẩm mỹ **Đông Phương Thiền Tịnh (Zen Oriental Minimalism & Quiet Luxury)** từ bộ bản vẽ Stitch Designs và tích hợp sâu sắc với hệ sinh thái Microservices Backend **Spring Boot 3.2.5** (`aromatics-platform`).

---

## 1. Tổng Quan Kiến Trúc (Architecture Overview)

### 1.1. Công Nghệ Nền Tảng
- **Framework**: Next.js 14.2.3 (App Router, Server & Client Components)
- **Language**: TypeScript 5.x
- **Styling**: Tailwind CSS + Custom Design Tokens (Đông Phương Thiền Tịnh)
- **State Management**:
  - `zustand` với middleware `persist`: Quản lý `useAuthStore` (Auth Token, User, Role), `useCartStore` (Giỏ hàng Offline + Online Sync), `useWishlistStore` (Danh sách ước nguyện).
  - `@tanstack/react-query`: Server state caching, background invalidation, và optimistic updates.
- **HTTP Client**: Axios với interceptors tự động đính kèm `Bearer Token` và cơ chế **Queue-based 401 Refresh Token Rotation** không gián đoạn trải nghiệm người dùng.
- **Icons**: Lucide React + Material Symbols Outlined (Zen icons: spa, local_fire_department, meditation, etc.).
- **Feedback**: `react-hot-toast` tinh chỉnh tông màu Mộc Hương (Giấy xuyến, Trầm hương, Hổ phách, Chu sa).

---

## 2. Hệ Thống Design System & Bảng Màu Đông Phương

Dựa trên quy chuẩn `DESIGN.md`:
| Token Name | Hex Code | Ý Nghĩa / Ứng Dụng |
| :--- | :--- | :--- |
| `bg-rice-paper` | `#FAF7F2` | Màu nền chủ đạo (Giấy xuyến cổ truyền), thanh sạch, dịu mắt |
| `surface-sand` | `#F2ECE1` | Nền thứ cấp (Cát ấm sa mạc / Gỗ bồ đề), dùng cho card, hero, drawer |
| `bg-agarwood-dark` | `#1E1815` | Màu đen gỗ Trầm hương đại ngàn, thanh tao, trang trọng |
| `secondary-fixed` | `#C89B57` | Màu vàng Hổ phách trầm mặc, điểm nhấn sang trọng hoàng gia |
| `secondary-container`| `#B38746` | Hổ phách đậm khi hover, active |
| `status-emerald` | `#3B5E4C` | Xanh Ngọc Bích / Lá sen, biểu trưng cho sự tĩnh tại và an toàn |
| `status-cinnabar` | `#A8382B` | Đỏ Chu sa, dùng cho thông báo giảm giá, thẻ cháy hàng, lỗi |
| `border-bamboo` | `#E5DEC9` | Màu sợi tre trúc, viền phân cách mỏng nhẹ 1px |

### Phông Chữ (Typography):
- **Phông Tiêu Đề (Editorial Heading)**: `Playfair Display` (Serif cổ điển, vương giả).
- **Phông Nội Dung (Body & UI)**: `Be Vietnam Pro` (Không chân, tối ưu hiển thị tiếng Việt hoàn hảo trên mọi kích thước màn hình).
- **Phông Mã & Giá Tiền**: `JetBrains Mono` (Dành cho mã đơn hàng, SKU, tiền tệ VND).

---

## 3. Bản Đồ Màn Hình & Tính Năng (Screen & Feature Mapping)

### Phân Hệ Khách Hàng (Customer & Public Sanctuary)
1. **Trang Chủ (`/`) [Scene 1]**:
   - Hero Banner biên tập đậm chất thiền định với slogan và nút khám phá.
   - Thanh ưu đãi mã giảm giá cuộn mượt mà (Active Coupons ticker).
   - 5 Trụ cột sản phẩm (Ngũ Hương An Định: Nhang Trầm, Tinh Dầu, Trầm Nụ, Vòng Gỗ, Trà & Dụng Cụ).
   - Danh sách sản phẩm tiêu biểu với bộ lọc phân loại nhanh và phân trang.
   - Triết lý Mộc Hương & Tiêu chuẩn thủ công 100% tự nhiên.
2. **Cửa Hàng / Bộ Sưu Tập (`/shop`) [Scene 2]**:
   - Bộ lọc đa chiều: Tìm kiếm từ khóa theo thời gian thực, Cây danh mục, Khoảng giá, Sắp xếp (Mới nhất, Giá tăng/giảm, Bán chạy).
   - Lưới sản phẩm Responsive (1-4 cột), thẻ sản phẩm tích hợp Quick Add giỏ hàng & Lưu ước nguyện.
3. **Chi Tiết Sản Phẩm (`/products/[id]`) [Scene 3]**:
   - Gallery đa ảnh với phóng to tương tác và thumbnail.
   - Bộ chọn phân loại (Variant Selector: gram, đốt, kích thước) nối trực tiếp API tồn kho thời gian thực `GET /api/catalog/inventory/variant/{id}`.
   - Thẻ báo tồn kho trực quan (Cảnh báo khi tồn kho dưới ngưỡng an toàn).
   - Hệ thống đánh giá sao và gửi bình luận có kiểm duyệt (`POST /api/catalog/reviews`).
4. **Ngăn Kéo Giỏ Hàng (`CartDrawer`) [Scene 4]**:
   - Ngăn kéo trượt sang trọng (Slide-over) từ phải sang.
   - Thanh tiến trình Miễn phí vận chuyển thông minh (Mục tiêu 500.000đ).
   - Tăng/giảm số lượng, xóa sản phẩm, đồng bộ giỏ hàng với Backend khi đăng nhập.
5. **Thanh Toán (`/checkout`) [Scene 5]**:
   - Chọn nhanh địa chỉ giao hàng đã lưu từ sổ địa chỉ `identity-service` hoặc nhập địa chỉ mới.
   - Áp dụng mã giảm giá với Live Preview (`POST /api/order/coupons/preview`).
   - Chọn phương thức: **COD (Thanh toán khi nhận hàng)** hoặc **VNPay (Cổng thanh toán quốc gia)**.
   - Cơ chế khóa Idempotency Key bảo vệ đơn hàng khỏi nhấp đúp trùng lặp.
6. **Xác Nhận Đơn Hàng & Hóa Đơn (`/orders/success/[orderCode]`) [Scene 6]**:
   - Thông tin biên nhận đơn hàng thiền vị.
   - Tải trực tiếp hóa đơn PDF từ Backend: `GET /api/order/orders/{id}/invoice/pdf`.
7. **Cổng Trả Về VNPay (`/checkout/vnpay-return`)**:
   - Tiếp nhận tham số `vnp_ResponseCode` từ VNPay Sandbox và hiển thị kết quả minh bạch.
8. **Đăng Nhập / Đăng Ký / Khôi Phục (`/(auth)/*`) [Scene 7]**:
   - Bố cục Split-screen biên tập nghệ thuật.
   - Đăng nhập Email/Password hoặc Google OAuth2.
   - Luồng Quên mật khẩu, Đặt lại mật khẩu qua token email, Kích hoạt tài khoản.
9. **Tài Khoản Cá Nhân (`/account/*`) [Scene 8 & 9]**:
   - Thanh tiến trình cấp bậc thành viên (Khách Mộc -> Trầm Hương Tinh Hoa).
   - Cập nhật hồ sơ, đổi mật khẩu bảo mật.
   - Sổ địa chỉ giao hàng (CRUD & Chọn địa chỉ mặc định).
   - Danh sách ước nguyện (Wishlist).
   - Quản lý Lịch sử Đơn hàng: Lọc theo trạng thái, Xem dòng thời gian chi tiết, Hủy đơn khi còn PENDING/CONFIRMED, Gửi yêu cầu hoàn hàng đổi trả, Viết đánh giá sản phẩm.
10. **Câu Chuyện Thương Hiệu (`/about`)**:
    - Hành trình tìm trầm tại Quảng Nam, 4 bước chế tác thủ công truyền thống, thông tin showroom thưởng trà.

---

### Phân Hệ Quản Trị & Vận Hành (Admin & Staff Sanctuary)
Truy cập qua URL: `/admin` (Yêu cầu đăng nhập tài khoản có quyền `STAFF` hoặc `ADMIN`).
1. **Bảng Điều Khiển Tổng Quan (`/admin`) [Scene 10]**:
   - 4 Thẻ KPI thời gian thực: Doanh thu thuần, Tổng số đơn hàng, Giá trị trung bình/đơn (AOV), Cảnh báo sắp hết hàng.
   - Nút xuất dữ liệu báo cáo sang Excel (`orderService.exportOrdersExcel`).
   - Bảng cảnh báo tồn kho thấp dưới ngưỡng.
   - Bảng giám sát trạng thái sức khỏe (Health status) của toàn bộ 6 Microservices.
2. **Quản Lý Đơn Hàng (`/admin/orders`) [Scene 11]**:
   - Tra cứu tìm kiếm theo mã đơn, tên khách hàng, số điện thoại, trạng thái.
   - **State Machine Modal**: Chuyển trạng thái đơn hợp lệ theo quy tắc nghiệp vụ (`CONFIRMED` -> `PROCESSING` -> `SHIPPED` với Mã vận đơn & Đơn vị vận chuyển -> `DELIVERED`).
   - Ghi chú nội bộ cho nhân viên kho & chăm sóc khách hàng.
   - Xuất danh sách đơn hàng sang file `.xlsx`.
3. **Quản Lý Sản Phẩm (`/admin/products`) [Scene 12]**:
   - Danh sách sản phẩm kèm thumbnail, danh mục, số biến thể, trạng thái.
   - Modal Thêm / Chỉnh sửa sản phẩm dạng Multipart Form (Tải ảnh sản phẩm lên MinIO / Cloudinary).
   - Tab Thùng rác (Recycle Bin): Hỗ trợ Xóa mềm (Soft Delete), Khôi phục (Restore), và Xóa vĩnh viễn (Force Delete).
   - Ngăn kéo quản lý biến thể (Variants Drawer) với SKU, giá, trọng lượng.
4. **Quản Trị Tồn Kho (`/admin/inventory`)**:
   - Theo dõi tồn kho thực tế của từng phân loại sản phẩm.
   - Modal điều chỉnh số lượng nhập kho và ngưỡng cảnh báo an toàn.
5. **Quản Lý Mã Giảm Giá (`/admin/coupons`)**:
   - Danh sách mã khuyến mãi với ngày hiệu lực, lượt sử dụng, trạng thái kích hoạt.
   - Modal tạo mới mã coupon: Giảm theo % (kèm mức giảm tối đa), Giảm tiền cố định, hoặc Miễn phí vận chuyển.
6. **Kiểm Duyệt Đánh Giá (`/admin/reviews`)**:
   - Duyệt và quản lý bình luận khách hàng.
   - Ẩn/hiện bình luận xấu, gửi phản hồi chính thức từ ban quản trị Mộc Hương.
7. **Quản Trị Người Dùng & Phân Quyền (`/admin/users`)**:
   - Tra cứu thành viên và khách hàng.
   - Nâng cấp / Thay đổi vai trò (`CUSTOMER`, `STAFF`, `ADMIN`).
   - Khóa / Mở khóa tài khoản vi phạm.

---

## 4. Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### 4.1. Khởi Động Hạ Tầng & Microservices Backend
Trước khi khởi động Frontend, đảm bảo cụm Backend đang hoạt động:
```powershell
# 1. Khởi động Docker Containers (PostgreSQL, Kafka, Redis, MinIO)
cd "c:\Users\Minh Triet\Desktop\Moc_Huong_Shop_Personal_Project\aromatics-platform"
docker compose up -d

# 2. Khởi động các Microservices (hoặc mở từng service trong IDE và Run):
# - discovery-service (Port 8761)
# - api-gateway (Port 8080)
# - identity-service (Port 8081)
# - catalog-service (Port 8082)
# - order-service (Port 8083)
# - notification-service (Port 8084)
```

### 4.2. Khởi Động Frontend Next.js 14
```powershell
cd "c:\Users\Minh Triet\Desktop\Moc_Huong_Shop_Personal_Project\moc-huong-frontend"

# Cài đặt thư viện phụ thuộc (đã được cài đặt sẵn)
npm install

# Khởi chạy máy chủ phát triển (Development Server)
npm run dev
```
Mở trình duyệt tại: [http://localhost:3000](http://localhost:3000)

*Mẹo*: Next.js được cấu hình tự động Rewrite toàn bộ các truy vấn `/api/:path*` sang API Gateway tại `http://localhost:8080/api/:path*`, loại bỏ hoàn toàn lỗi CORS khi chạy cục bộ.

---

## 5. Tài Khoản Kiểm Thử Nghiệp Vụ (Test Credentials)

| Phân Quyền | Tài Khoản (Email) | Mật Khẩu | Quyền Hạn Truy Cập |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@mochuong.vn` | `Admin@123456` | Toàn quyền Hệ thống, Dashboard, Quản trị Users, Doanh thu, Coupons, Sản phẩm |
| **STAFF** | `staff@mochuong.vn` | `Staff@123456` | Vận hành Đơn hàng, Kho vận, Sản phẩm, Kiểm duyệt Đánh giá |
| **CUSTOMER** | `khachhang@mochuong.vn` | `User@123456` | Mua sắm, Đặt hàng, Xem lịch sử đơn, Quản lý địa chỉ & ước nguyện |

*(Hoặc bạn có thể bấm vào nút **Đăng Ký** tại `/register` để tạo tài khoản Khách hàng mới bất kỳ).*

---

## 6. Kịch Bản Kiểm Thử Hoàn Chỉnh (End-to-End Walkthrough)

1. **Khách hàng Mua sắm**:
   - Vào `/shop`, tìm kiếm "Trầm", lọc giá từ 200.000đ đến 1.000.000đ.
   - Bấm vào sản phẩm bất kỳ -> Chọn biến thể (Hộp 50 nụ hoặc 100 nụ) -> Thấy giá và tồn kho nhảy theo biến thể.
   - Bấm "Thêm Vào Giỏ Hàng" -> Ngăn kéo `CartDrawer` trượt ra, thanh miễn phí vận chuyển tự động tính toán.
2. **Khách hàng Thanh toán**:
   - Từ `CartDrawer`, bấm "Tiến Hành Thanh Toán" -> Chuyển đến `/checkout`.
   - Nhập thông tin người nhận hoặc chọn địa chỉ mặc định có sẵn.
   - Nhập mã giảm giá (ví dụ: `TRIANMOCHUONG`) -> Bấm "Áp Dụng" -> Hệ thống trừ tiền trực tiếp.
   - Chọn phương thức COD hoặc VNPay -> Bấm "Hoàn Tất Đặt Hàng" -> Chuyển đến `/orders/success/[orderCode]`.
   - Tại trang thành công, bấm "Tải Hóa Đơn PDF" để tải hóa đơn VAT điện tử.
3. **Nhân viên Quản lý & Vận hành**:
   - Đăng nhập tài khoản `admin@mochuong.vn` -> Vào `/admin`.
   - Xem thống kê KPI, biểu đồ và cảnh báo tồn kho.
   - Vào `/admin/orders` -> Mở chi tiết đơn hàng vừa tạo -> Bấm "Cập Nhật Trạng Thái" -> Chọn "Xác Nhận Đơn" -> "Đang Xử Lý" -> "Đang Giao Hàng" (nhập mã GHN/GHTK) -> "Đã Giao Thành Công".
   - Vào `/admin/products` -> Thêm mới nụ trầm hương đặc biệt với ảnh đại diện.
