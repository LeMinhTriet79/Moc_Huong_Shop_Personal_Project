# SIÊU PROMPT XÂY DỰNG FRONTEND & NỐI API TOÀN DIỆN — MỘC HƯƠNG (TRẦM HƯƠNG & TINH DẦU)

## 1. BỐI CẢNH & NHIỆM VỤ TỐI THƯỢNG CỦA AI AGENT
Bạn là **Principal Frontend Architect (chuyên gia Next.js 14/15 App Router, React 18, TypeScript, Tailwind CSS, Zustand, TanStack Query)** kiêm **Full-Stack Integrator**.
Dự án của chúng ta có cấu trúc tại thư mục gốc gồm:
1. `aromatics-platform/`: Hệ thống 6 Microservices Backend (Spring Boot 3.2.5, cổng API Gateway `http://localhost:8080`) **ĐÃ HOÀN THIỆN 100%**.
2. `stitch-designs/`: Bộ giao diện HTML/Tailwind chuẩn thẩm mỹ **"Đông Phương Thiền Tịnh" (Zen Oriental Minimalism)** được xuất từ Google Stitch.
3. `DESIGN.md`: Tài liệu quy chuẩn màu sắc, font chữ (`Cormorant Garamond` / `Playfair Display` + `Plus Jakarta Sans` + `JetBrains Mono`), bo góc và cảm xúc thương hiệu **Mộc Hương**.

**Nhiệm vụ của bạn**:
Khởi tạo ứng dụng Frontend hoàn chỉnh tại thư mục `moc-huong-frontend/` (nằm ngang hàng với `aromatics-platform/`), chuyển đổi toàn bộ 12 cảnh giao diện từ `stitch-designs/` thành các trang React/Next.js sống động, và **nối 100% API thực tế** vào Backend qua cổng `http://localhost:8080`.

## 2. QUY TẮC KIẾN TRÚC FRONTEND BẮT BUỘC (KHÔNG ĐƯỢC VI PHẠM)
1. **Giữ nguyên hồn cốt thiết kế Stitch & `DESIGN.md`**:
   - Tuyệt đối không dùng màu trắng xanh công nghiệp (`#FFFFFF`) làm nền chính; sử dụng nền Giấy Dó Trầm Ấm (`#FBF8F3`), Vàng Nắng Sớm (`#F3EDE2`), Nâu Trầm Kỳ Nam (`#2C1E16`), Nâu Đất Nung (`#8C4A27`) và Vàng Đồng Cổ (`#C89D54`).
   - Đọc từng file HTML trong `stitch-designs/`, tách thành các Component tái sử dụng (`Header`, `Footer`, `ProductCard`, `CartDrawer`, `StatusBadge`, `AdminSidebar`, `Modal`) giữ nguyên class Tailwind và bố cục tinh tế của Stitch.
2. **Cấu hình Proxy chống lỗi CORS triệt để**:
   - Trong `moc-huong-frontend/next.config.mjs`, cấu hình `rewrites` để mọi request từ Frontend gọi `/api/:path*` được tự động chuyển tiếp sang `http://localhost:8080/api/:path*` (hoặc biến môi trường `NEXT_PUBLIC_API_URL`).
3. **Chuẩn hóa Wrapper `ApiResponse<T>` & Axios Interceptor**:
   - Toàn bộ API của `aromatics-platform` đều trả về cấu trúc JSON thống nhất:
     ```json
     {
       "success": true,
       "message": "Thông báo tiếng Việt từ Backend",
       "data": { ... },
       "errorCode": null,
       "timestamp": "2026-10-26T..."
     }
     ```
   - Khi đăng nhập thành công (`POST /api/identity/auth/login`), Backend trả về `AuthResponse` bên trong `data` gồm: `accessToken`, `refreshToken`, `tokenType`, `userId`, `email`, `fullName`, `role` (`CUSTOMER`, `STAFF`, `ADMIN`).
   - Lưu `accessToken`, `refreshToken` và thông tin `user` vào Zustand (`useAuthStore` có `persist` vào `localStorage`).
   - Cấu hình Axios Request Interceptor tự động đính kèm header `Authorization: Bearer <accessToken>`.
   - Cấu hình Axios Response Interceptor: Nếu gặp HTTP `401 Unauthorized`, tự động gọi `POST /api/identity/auth/refresh` với body `{ "refreshToken": "..." }` để lấy cặp token mới và thực hiện lại request vừa bị gián đoạn; nếu refresh thất bại mới đăng xuất và chuyển về `/login`.
4. **Phân quyền Route Guard (`CUSTOMER`, `STAFF`, `ADMIN`)**:
   - Khách vãng lai (`GUEST`): Xem trang chủ, danh mục, chi tiết sản phẩm, đánh giá, tra cứu.
   - Khách hàng (`CUSTOMER`): Giỏ hàng, Thanh toán, Áp mã giảm giá, Lịch sử đơn hàng, Hủy đơn (`PENDING`/`CONFIRMED`), Yêu cầu trả hàng (`DELIVERED`), Tải hóa đơn PDF, Sổ địa chỉ, Wishlist, Viết đánh giá.
   - Nhân viên (`STAFF`): Truy cập `/admin/orders`, `/admin/products`, `/admin/categories`, `/admin/inventory`, `/admin/reviews` (xem thống kê đơn hàng, đổi trạng thái đơn, ghi chú nội bộ, xuất Excel, quản lý sản phẩm/biến thể/tồn kho, trả lời và ẩn/hiện đánh giá).
   - Quản trị viên (`ADMIN`): Toàn quyền của `STAFF` cộng thêm `/admin/users` (phân quyền Role, khóa/mở khóa tài khoản), `/admin/coupons` (tạo/sửa/xóa mã giảm giá), và xóa vĩnh viễn sản phẩm (`force delete`).

## 3. BẢNG ĐẶC TẢ ÁNH XẠ API THỰC TẾ TỪ BACKEND (`aromatics-platform`)

### 3.1. Phân hệ Identity Service (`/api/identity/...`)
| Chức năng | HTTP Method & Endpoint | Request Body / Query Params | Response `data` |
| :--- | :--- | :--- | :--- |
| Đăng ký | `POST /api/identity/auth/register` | `{ email, password, fullName }` | `UserResponse` |
| Đăng nhập | `POST /api/identity/auth/login` | `{ email, password }` | `AuthResponse` (`accessToken`, `refreshToken`, `userId`, `email`, `fullName`, `role`) |
| Làm mới Token | `POST /api/identity/auth/refresh` | `{ refreshToken }` | `AuthResponse` |
| Đăng xuất | `POST /api/identity/auth/logout` | `{ refreshToken }` | `Void` |
| Xác thực Email | `GET /api/identity/auth/verify-email?token=...` | Query `token` | `Void` |
| Quên mật khẩu | `POST /api/identity/auth/forgot-password` | `{ email }` | `Void` |
| Đặt lại mật khẩu | `POST /api/identity/auth/reset-password` | `{ token, newPassword }` | `Void` |
| Đăng nhập Google | `POST /api/identity/auth/google` | `{ idToken }` (hoặc thông tin Google) | `AuthResponse` |
| Xem hồ sơ cá nhân | `GET /api/identity/users/me` | Header `Authorization` | `UserResponse` (`id`, `email`, `fullName`, `phoneNumber`, `avatarUrl`, `dateOfBirth`, `role`) |
| Cập nhật hồ sơ | `PUT /api/identity/users/me` | `{ fullName, phoneNumber, dateOfBirth, avatarUrl }` | `UserResponse` |
| Đổi mật khẩu | `PUT /api/identity/users/me/password` | `{ oldPassword, newPassword }` | `Void` |
| Danh sách địa chỉ | `GET /api/identity/users/me/addresses` | Header `Authorization` | `List<AddressResponse>` |
| Thêm địa chỉ mới | `POST /api/identity/users/me/addresses` | `{ recipientName, phoneNumber, province, district, ward, streetAddress, isDefault }` | `AddressResponse` |
| Sửa địa chỉ | `PUT /api/identity/users/me/addresses/{id}` | `{ recipientName, phoneNumber, province, district, ward, streetAddress, isDefault }` | `AddressResponse` |
| Đặt địa chỉ mặc định | `PUT /api/identity/users/me/addresses/{id}/default` | Path `id` | `AddressResponse` |
| Xóa địa chỉ | `DELETE /api/identity/users/me/addresses/{id}` | Path `id` | `Void` |
| Xem Wishlist | `GET /api/identity/users/me/wishlists` | Header `Authorization` | `List<WishlistResponse>` |
| Thêm vào Wishlist | `POST /api/identity/users/me/wishlists/{productId}` | Path `productId` | `WishlistResponse` |
| Xóa khỏi Wishlist | `DELETE /api/identity/users/me/wishlists/{productId}` | Path `productId` | `Void` |
| Kiểm tra đã yêu thích | `GET /api/identity/users/me/wishlists/{productId}/check` | Path `productId` | `Boolean` |
| **[ADMIN]** DS Người dùng | `GET /api/identity/admin/users` | `?keyword=&role=&isActive=&page=0&size=10&sortBy=createdAt&direction=desc` | `Page<UserResponse>` |
| **[ADMIN]** Chi tiết User | `GET /api/identity/admin/users/{id}` | Path `id` | `UserResponse` (kèm `addresses`) |
| **[ADMIN]** Đổi Role User | `PUT /api/identity/admin/users/{id}/role` | `{ role: "CUSTOMER" \| "STAFF" \| "ADMIN" }` | `UserResponse` |
| **[ADMIN]** Khóa/Mở khóa User | `PUT /api/identity/admin/users/{id}/status` | `{ isActive: true \| false }` | `UserResponse` |

### 3.2. Phân hệ Catalog Service (`/api/catalog/...`)
| Chức năng | HTTP Method & Endpoint | Request Body / Query Params | Response `data` |
| :--- | :--- | :--- | :--- |
| Lọc & Tìm kiếm SP | `GET /api/catalog/products` | `?keyword=&categoryId=&brand=&minPrice=&maxPrice=&page=0&size=12&sortBy=createdAt&direction=desc` | `Page<ProductResponse>` |
| Top 8 SP nổi bật | `GET /api/catalog/products/featured` | Không có | `List<ProductResponse>` |
| Chi tiết sản phẩm | `GET /api/catalog/products/{id}` | Path `id` | `ProductResponse` (kèm `variants`, `images`, `attributes`, `averageRating`, `reviewCount`) |
| Danh sách Danh mục | `GET /api/catalog/categories` | Không có | `List<CategoryResponse>` |
| DS Biến thể của SP | `GET /api/catalog/products/{productId}/variants` | Path `productId` | `List<ProductVariantResponse>` |
| Xem tồn kho Biến thể | `GET /api/catalog/inventory/variant/{variantId}` | Path `variantId` | `InventoryResponse` (`quantity`, `reservedQuantity`, `availableStock`) |
| Xem đánh giá của SP | `GET /api/catalog/reviews/product/{productId}` | `?page=0&size=10` | `Page<ReviewResponse>` |
| Gửi đánh giá SP | `POST /api/catalog/reviews` | `{ productId, orderId, rating, comment }` | `ReviewResponse` |
| **[STAFF/ADMIN]** SP đã ẩn | `GET /api/catalog/products/inactive` | `?page=0&size=10` | `Page<ProductResponse>` |
| **[STAFF/ADMIN]** Tạo SP | `POST /api/catalog/products` | `multipart/form-data`: part `product` (JSON String `ProductRequest`) + part `images` (`MultipartFile[]`) | `ProductResponse` |
| **[STAFF/ADMIN]** Sửa SP | `PUT /api/catalog/products/{id}` | `multipart/form-data`: part `product` (JSON String) + part `images` (optional) | `ProductResponse` |
| **[STAFF/ADMIN]** Ẩn SP | `DELETE /api/catalog/products/{id}` | Path `id` | `Void` |
| **[STAFF/ADMIN]** Khôi phục SP | `PUT /api/catalog/products/{id}/restore` | Path `id` | `ProductResponse` |
| **[ADMIN]** Xóa vĩnh viễn SP | `DELETE /api/catalog/products/{id}/force` | Path `id` | `Void` |
| **[STAFF/ADMIN]** Thêm Biến thể | `POST /api/catalog/products/{productId}/variants` | `{ sku, variantName, price, weightGrams, initialStock, attributes }` | `ProductVariantResponse` |
| **[STAFF/ADMIN]** Sửa Biến thể | `PUT /api/catalog/products/{productId}/variants/{variantId}` | `{ sku, variantName, price, weightGrams, isActive, attributes }` | `ProductVariantResponse` |
| **[STAFF/ADMIN]** Ẩn Biến thể | `DELETE /api/catalog/products/{productId}/variants/{variantId}` | Path `productId`, `variantId` | `Void` |
| **[STAFF/ADMIN]** Cập nhật Kho | `PUT /api/catalog/inventory/variant/{variantId}` | `{ quantity, lowStockThreshold }` | `InventoryResponse` |
| **[STAFF/ADMIN]** SP sắp hết | `GET /api/catalog/inventory/low-stock` | Không có | `List<InventoryResponse>` |
| **[STAFF/ADMIN]** Quản lý Review | `GET /api/catalog/reviews/admin/reviews` | `?productId=&rating=&isVisible=&page=0&size=10` | `Page<ReviewResponse>` |
| **[STAFF/ADMIN]** Phản hồi Review | `PUT /api/catalog/reviews/{reviewId}/reply` | `{ replyMessage }` | `ReviewResponse` |
| **[STAFF/ADMIN]** Ẩn/Hiện Review | `PUT /api/catalog/reviews/{reviewId}/toggle-visibility` | Path `reviewId` | `ReviewResponse` |

### 3.3. Phân hệ Order Service (`/api/order/...`)
| Chức năng | HTTP Method & Endpoint | Request Body / Query Params | Response `data` |
| :--- | :--- | :--- | :--- |
| Xem giỏ hàng | `GET /api/order/cart` | Header `Authorization` | `List<CartItem>` |
| Thêm vào giỏ hàng | `POST /api/order/cart/add` | `{ productId, variantId, productName, variantName, productImageUrl, price, quantity }` | `Void` |
| Cập nhật số lượng giỏ | `PUT /api/order/cart/update` | `{ productId, variantId, quantity }` | `Void` |
| Xóa 1 món khỏi giỏ | `DELETE /api/order/cart/remove/{productId}/{variantId}` | Path `productId`, `variantId` | `Void` |
| Xóa sạch giỏ hàng | `DELETE /api/order/cart/clear` | Header `Authorization` | `Void` |
| DS Mã giảm giá đang mở | `GET /api/order/coupons/active` | Không có | `List<CouponResponse>` |
| Xem trước tiền giảm | `POST /api/order/coupons/preview` | `{ couponCode, subtotal, shippingFee }` | `CouponCalculationResult` (`valid`, `discountAmount`, `finalTotal`, `message`) |
| Đặt hàng (Checkout) | `POST /api/order/orders/checkout` | `{ shippingAddress: { recipientName, phoneNumber, streetAddress, ward, district, province }, paymentMethod: "COD" \| "VNPAY", couponCode, notes, idempotencyKey }` | `OrderResponse` |
| Lịch sử đơn của tôi | `GET /api/order/orders` | `?status=&page=0&size=10` | `Page<OrderResponse>` |
| Chi tiết đơn theo mã | `GET /api/order/orders/{orderCode}` | Path `orderCode` | `OrderResponse` (kèm `items`, `statusHistory`, `transactions`) |
| Hủy đơn hàng | `PUT /api/order/orders/{orderId}/cancel` | `{ reason }` | `OrderResponse` |
| Yêu cầu hoàn trả | `POST /api/order/orders/{orderId}/return-request` | `{ reason }` | `OrderResponse` |
| Tải hóa đơn PDF | `GET /api/order/orders/{orderId}/invoice-pdf` | `responseType: 'blob'` | Binary `application/pdf` |
| Tạo link VNPay | `GET /api/order/payment/create-payment` | `?amount=...&orderInfo=...` | `{ paymentUrl }` |
| **[STAFF/ADMIN]** DS Đơn hàng | `GET /api/order/admin/orders` | `?keyword=&status=&paymentStatus=&paymentMethod=&startDate=&endDate=&page=0&size=10` | `Page<OrderResponse>` |
| **[STAFF/ADMIN]** Chi tiết Đơn | `GET /api/order/admin/orders/{orderId}` | Path `orderId` | `OrderResponse` |
| **[STAFF/ADMIN]** Đổi TT Đơn | `PUT /api/order/admin/orders/{orderId}/status` | `{ newStatus, trackingNumber, carrier, note }` | `OrderResponse` |
| **[STAFF/ADMIN]** Ghi chú Staff | `PUT /api/order/admin/orders/{orderId}/staff-notes` | `{ staffNotes }` | `OrderResponse` |
| **[STAFF/ADMIN]** Xuất Excel | `GET /api/order/admin/orders/export-excel` | `?startDate=&endDate=&status=` (`responseType: 'blob'`) | Binary `.xlsx` |
| **[STAFF/ADMIN]** Thống kê KPI | `GET /api/order/admin/orders/statistics` | `?startDate=&endDate=` | `DashboardStatsResponse` |
| **[ADMIN]** Quản lý Coupon | `GET/POST/PUT/DELETE /api/order/admin/coupons` | Full CRUD `CouponRequest` | `Page<CouponResponse>` / `CouponResponse` |

## 4. LỘ TRÌNH 6 PHASE THỰC THI CHO ANTIGRAVITY

### PHASE 1: KHỞI TẠO DỰ ÁN & HỆ THỐNG DESIGN SYSTEM MỘC HƯƠNG
- **Task 1.1**: Khởi tạo Next.js 14+ (App Router, TypeScript, Tailwind CSS) trong `moc-huong-frontend/`. Cài đặt `axios`, `zustand`, `@tanstack/react-query`, `lucide-react`, `react-hot-toast` (hoặc `sonner`), `clsx`, `tailwind-merge`.
- **Task 1.2**: Đọc `DESIGN.md` và `stitch-designs/` để cấu hình `tailwind.config.ts` với đúng bảng màu Mộc Hương (`bg-primary: #FBF8F3`, `bg-secondary: #F3EDE2`, `ink-primary: #2C1E16`, `terracotta: #8C4A27`, `antique-gold: #C89D54`, `sage-green: #2E5A44`) và Google Fonts (`Cormorant Garamond`, `Plus Jakarta Sans`, `JetBrains Mono`).
- **Task 1.3**: Thiết lập `src/lib/axios.ts` (kèm JWT & Refresh Token Interceptor), `src/store/useAuthStore.ts`, `src/store/useCartStore.ts` và `next.config.mjs` proxy `/api` về `http://localhost:8080/api`.

### PHASE 2: CHUYỂN ĐỔI & NỐI API CÁC MÀN HÌNH KHÁM PHÁ SẢN PHẨM (CẢNH 1, 2, 3 TRONG STITCH)
- **Task 2.1 (Trang chủ `/` - Cảnh 1)**: Chuyển đổi giao diện Trang chủ từ `stitch-designs/`, nối API `GET /api/catalog/products/featured`, `GET /api/catalog/categories` và `GET /api/order/coupons/active` để hiển thị Bát hương, Trầm nụ, Vòng gỗ Trầm hương và các Voucher đang mở.
- **Task 2.2 (Trang Cửa hàng `/shop` - Cảnh 2)**: Chuyển đổi giao diện Danh mục & Bộ lọc từ `stitch-designs/`, nối API `GET /api/catalog/products` với bộ lọc đa tầng (Danh mục, Thương hiệu, Khoảng giá, Từ khóa, Sắp xếp và Phân trang).
- **Task 2.3 (Trang Chi tiết Sản phẩm `/products/[id]` - Cảnh 3)**:
  - Nối `GET /api/catalog/products/{id}` và `GET /api/catalog/inventory/variant/{variantId}`.
  - Khi người dùng bấm chọn các viên thuốc Biến thể (ví dụ: *Hộp 30 nụ*, *Hộp gỗ sơn mài 50 nụ*, *Vòng 108 hạt 6mm*), cập nhật ngay lập tức **Giá tiền**, **Mã SKU**, **Trạng thái tồn kho** và **Thuộc tính JSON (`attributes`)**.
  - Nối nút Yêu thích (`POST/DELETE /api/identity/users/me/wishlists/{productId}`) và danh sách Đánh giá (`GET /api/catalog/reviews/product/{productId}`).

### PHASE 3: GIỎ HÀNG SLIDE-OVER, MÃ GIẢM GIÁ & THANH TOÁN (CẢNH 4, 5, 6 TRONG STITCH)
- **Task 3.1 (Ngăn kéo Giỏ hàng `CartDrawer` - Cảnh 4)**: Đồng bộ giỏ hàng với `GET /api/order/cart`, `POST /api/order/cart/add`, `PUT /api/order/cart/update`, `DELETE /api/order/cart/remove/{productId}/{variantId}`. Hiển thị thanh tiến trình miễn phí vận chuyển.
- **Task 3.2 (Trang Thanh toán `/checkout` - Cảnh 5)**:
  - Tự động tải danh sách địa chỉ từ `GET /api/identity/users/me/addresses` để người dùng chọn nhanh 1 chạm hoặc nhập mới.
  - Nối ô nhập Mã giảm giá với `POST /api/order/coupons/preview` để hiển thị số tiền được giảm ngay lập tức.
  - Khi bấm **Đặt Hàng**, sinh `idempotencyKey = crypto.randomUUID()`, gọi `POST /api/order/orders/checkout`. Nếu chọn `VNPAY`, gọi tiếp `GET /api/order/payment/create-payment` để chuyển hướng sang cổng thanh toán.
- **Task 3.3 (Trang Đặt hàng thành công & Kết quả VNPay `/orders/success/[orderCode]` - Cảnh 6)**: Hiển thị thông tin đơn hàng từ `GET /api/order/orders/{orderCode}` và nút **Tải Hóa Đơn PDF** (`GET /api/order/orders/{orderId}/invoice-pdf` xử lý tải file Blob `.pdf`).

### PHASE 4: XÁC THỰC & KHÔNG GIAN KHÁCH HÀNG (CẢNH 7, 8, 9 TRONG STITCH)
- **Task 4.1 (Đăng nhập / Đăng ký / Quên mật khẩu `/login`, `/register`, `/forgot-password` - Cảnh 7)**: Chuyển đổi giao diện Split-screen từ `stitch-designs/`, nối đầy đủ các API trong `AuthController`.
- **Task 4.2 (Hồ sơ, Sổ địa chỉ & Wishlist `/account/*` - Cảnh 8)**: Nối đầy đủ API cập nhật hồ sơ, đổi mật khẩu, thêm/sửa/xóa/đặt mặc định địa chỉ giao hàng và danh sách sản phẩm yêu thích.
- **Task 4.3 (Lịch sử Đơn hàng, Timeline Trạng thái & Đánh giá `/account/orders` - Cảnh 9)**:
  - Hiển thị danh sách đơn theo tab trạng thái (`ALL`, `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
  - Hiển thị Timeline lịch sử trạng thái (`statusHistory`), nút **Hủy đơn hàng** (khi đơn ở `PENDING` hoặc `CONFIRMED`), nút **Yêu cầu hoàn trả** (khi `DELIVERED`), nút **Tải Hóa Đơn PDF** và Modal **Viết đánh giá sản phẩm** (`POST /api/catalog/reviews`).

### PHASE 5: CỔNG QUẢN TRỊ ADMIN & STAFF (CẢNH 10, 11, 12 TRONG STITCH)
- **Task 5.1 (Dashboard Thống kê & Cảnh báo Kho `/admin` - Cảnh 10)**: Nối `GET /api/order/admin/orders/statistics` và `GET /api/catalog/inventory/low-stock` để hiển thị 4 thẻ KPI, biểu đồ doanh thu theo ngày, phân bố trạng thái đơn hàng và bảng cảnh báo Trầm hương sắp hết hàng.
- **Task 5.2 (Quản lý Đơn hàng & Xuất Excel `/admin/orders` - Cảnh 11)**: Nối `GET /api/order/admin/orders`, Modal cập nhật trạng thái đơn hàng theo State Machine (`PUT /api/order/admin/orders/{orderId}/status` kèm mã vận đơn `trackingNumber` & đơn vị giao hàng `carrier`), ghi chú nội bộ (`staff-notes`), xuất báo cáo Excel (`GET /api/order/admin/orders/export-excel`) và in hóa đơn PDF.
- **Task 5.3 (Quản lý Sản phẩm, Biến thể, Kho, Coupon, Review & User `/admin/*` - Cảnh 12)**:
  - Quản lý Sản phẩm (`MultipartFormData` tạo/sửa SP kèm upload nhiều ảnh, thùng rác khôi phục/xóa vĩnh viễn) & Drawer quản lý Biến thể + cập nhật Tồn kho.
  - Quản lý Mã giảm giá (`/admin/coupons`), Quản lý Đánh giá (`/admin/reviews` - phản hồi khách hàng & ẩn/hiện) và Quản lý Người dùng (`/admin/users` - đổi Role & khóa tài khoản).

### PHASE 6: KIỂM TRA BUILD & NGHIỆM THU
- Chạy `npm run build` (hoặc `pnpm build`) trong `moc-huong-frontend/` để đảm bảo 0 lỗi TypeScript, 0 lỗi ESLint và tạo file `README_FRONTEND.md` hướng dẫn chạy đồng thời Backend + Frontend.