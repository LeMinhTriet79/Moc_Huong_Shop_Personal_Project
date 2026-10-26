# 🪵 MỘC HƯƠNG SHOP — SIÊU PROMPT THIẾT KẾ UI/UX & BẢN ĐẶC TẢ HỆ THỐNG GIAO DIỆN (`DESIGN.md`) DÀNH CHO STITCH AI

> **Hướng dẫn sử dụng với Google Stitch (hoặc v0 / Bolt / Lovable / Antigravity Frontend):**  
> 

> 1. **Cách 1 (Nạp toàn bộ Design System):** Copy toàn bộ nội dung trong khối **PHẦN 1 & PHẦN 2** dán vào Stitch để khởi tạo bộ quy chuẩn màu sắc, font chữ, hiệu ứng khói trầm và Layout khung sườn.  
> 2. **Cách 2 (Sinh từng màn hình siêu nét trên Stitch):** Ở **PHẦN 3**, thầy đã chia sẵn **12 Prompt chuyên biệt cho 12 Màn hình (Screens)** bám sát 100% các trường dữ liệu thật từ Database MySQL ([V1\_\_init\_catalog\_db.sql](https://drive.google.com/file/d/1yFnJRQzziezFY4vZ6J2CukcrWWc4hc94/view?usp=drivesdk), [V1\_\_init\_order\_db.sql](https://drive.google.com/file/d/1MfC8bzg8f5N-2_sDE1sFAU5f1O-4g6wi/view?usp=drivesdk), [V1\_\_init\_identity\_db.sql](https://drive.google.com/file/d/1qL60OaS2NVrZIR10vk0w-IIC_3Ebytpr/view?usp=drivesdk)) và phân quyền 3 Roles (`CUSTOMER`, `STAFF`, `ADMIN` trong [Role.java](https://drive.google.com/file/d/1gArpdfPzyfKmKq6j0YejQhGpU-wk1vnP/view?usp=drivesdk)). Em chỉ cần copy từng đoạn Prompt màn hình dán vào Stitch để nó vẽ ra giao diện hoàn hảo từng pixel\!

---

## PHẦN 1: TRIẾT LÝ THẨM MỸ & HỆ THỐNG DESIGN TOKENS "MỘC HƯƠNG THIỀN ĐỊNH" (ART DIRECTION & DESIGN SYSTEM)

### 1.1. Concept Chủ Đạo: *"Tĩnh Lặng Giữa Đời Thường — Zen Luxury & Indochine Wabi-Sabi"*

Website **Mộc Hương Shop (`aromatics-platform`)** không được phép mang giao diện thương mại điện tử công nghiệp đại trà (như Shopee/Lazada với nền trắng lạnh và màu cam/đỏ chói mắt). Thay vào đó, giao diện phải tạo cảm giác bước vào một **Không gian Trà đạo, Thiền định & Trầm hương cao cấp** (lấy cảm hứng từ sự giao thoa giữa **Aesop**, **Le Labo**, và **Kiến trúc Gỗ Á Đông / Wabi-Sabi**):

- **Cảm xúc thị giác (Mood):** Tĩnh tại, ấm áp, mộc mạc nhưng sang trọng (Quiet Luxury), thoang thoảng làn khói trầm mỏng nhẹ, bề mặt có chiều sâu như giấy Dó truyền thống và vân gỗ Trầm hương / Tử đàn lâu năm.  
- **Chất liệu thị giác (Visual Textures):**  
  - Nền web màu kem giấy Dó ấm (`#FAF7F2`) kết hợp hiệu ứng hạt giấy mịn (`subtle paper grain noise 2% opacity`).  
  - Các khung hình ảnh bo góc mềm mại (`border-radius: 12px` hoặc vòm Á Đông `arch-top` cho các banner danh mục).  
  - Đường viền mảnh tinh tế (`1px solid #E5DEC9`) gợi liên tưởng đến các nan gỗ hoặc khung cửa trượt Shoji / Á Đông.

### 1.2. Bảng Màu Chuẩn "Mộc Hương" (Color Palette & CSS Variables)

| Tên Token | Mã Hex | Vai trò trong Giao diện (UI Usage) | Ý nghĩa phong thủy & chất liệu |
| :---- | :---- | :---- | :---- |
| **`--bg-rice-paper`** | `#FAF7F2` | Nền chính toàn trang (Primary Background) | Màu giấy Dó / Lụa tơ tằm tự nhiên, dịu mắt khi đọc lâu |
| **`--bg-warm-sand`** | `#F2ECE1` | Nền phụ cho Card sản phẩm, Sidebar bộ lọc, Khu vực Review | Màu cát trầm ấm & gỗ đàn hương sáng |
| **`--bg-agarwood-dark`** | `#1E1815` | Nền Footer, Banner Hero Thiền định, và Giao diện Dark Mode | Màu gỗ Trầm Hương Kỳ Nam / Gỗ Mun đen huyền bí |
| **`--text-ink-black`** | `#231C18` | Chữ tiêu đề & nội dung chính (Primary Text) | Màu mực Tàu pha nâu trầm (độ tương phản AAA \> 12:1) |
| **`--text-wood-muted`** | `#6E5D53` | Chữ mô tả phụ, SKU, ngày tháng (Secondary Text) | Màu vỏ cây quế / gỗ thông khô |
| **`--brand-agarwood`** | `#4A3222` | Màu chủ đạo (Primary Brand / Nút bấm chính / Header) | Màu gỗ Trầm Hương Tốc Kiến tự nhiên |
| **`--accent-amber-gold`** | `#C89B57` | Màu nhấn cao cấp (Hover, Badge Best-Seller, Ngôi sao 5 sao, Giá tiền) | Màu nhựa Trầm óng ánh & Đồng thau lư xông |
| **`--accent-lotus-jade`** | `#3B5E4C` | Nhãn "100% Tự Nhiên / Organic", Trạng thái Còn hàng & Thành công | Màu xanh ngọc bích / Lá sen tĩnh tâm |
| **`--status-cinnabar`** | `#A8382B` | Nhãn Giảm giá (`Coupon`), Cảnh báo sắp hết kho (`low_stock_threshold`), Nút Hủy | Màu đỏ Chu Sa (Cinnabar) truyền thống Á Đông |
| **`--border-bamboo`** | `#E5DEC9` | Viền chia cắt mỏng nhẹ giữa các khối (Dividers & Card Borders) | Màu sợi mây / tre đan tự nhiên |

### 1.3. Hệ Thống Chữ Viết (Typography — Hỗ trợ Tiếng Việt 100%)

- **Font Tiêu đề (Heading Font):** **`Cormorant Garamond`** hoặc **`Playfair Display`** (Serif thanh lịch, nét thanh nét đậm mang hơi hướng thư pháp & tạp chí nghệ thuật cao cấp).  
  - `H1 (Hero Title)`: `48px` (`3rem`), `font-weight: 600`, `line-height: 1.18`, `letter-spacing: -0.01em`.  
  - `H2 (Section Title)`: `34px` (`2.125rem`), `font-weight: 600`, kèm họa tiết đường kẻ mảnh hoặc biểu tượng hoa sen/làn khói nhỏ bên dưới.  
  - `H3 (Product Card Title)`: `20px` (`1.25rem`), `font-weight: 600`, giới hạn 2 dòng (`line-clamp-2`).  
- **Font Nội dung & UI (Body & Controls Font):** **`Be Vietnam Pro`** hoặc **`Plus Jakarta Sans`** (Sans-serif hiện đại, thiết kế chuyên biệt cho dấu Tiếng Việt, cực kỳ rõ ràng ở kích thước nhỏ).  
  - `Body Regular`: `16px` (`1rem`), `line-height: 1.65`.  
  - `Caption / Metadata / Badge`: `13px` (`0.8125rem`), `letter-spacing: 0.04em`.  
- **Font Mã đơn / Giá tiền / SKU (Monospace Accent):** **`JetBrains Mono`** cho mã `order_code` (`MH-8A9F2C1B`), mã `sku` (`NHANG-TRAM-40`), và mã `coupon_code` (`MOCHUONG10`).

### 1.4. Hệ Thống Grid, Khoảng Trắng & Vi Tương Tác (Layout & Micro-Interactions)

- **Container chuẩn:** Max-width `1360px`, padding hai bên `24px` (Mobile) / `48px` (Desktop), 12-column CSS Grid (`gap: 24px`).  
- **Hiệu ứng đặc trưng Mộc Hương:**  
  - **Product Card Hover:** Khi di chuột vào thẻ sản phẩm, ảnh chính zoom nhẹ `scale(1.04)` mượt mà trong `500ms cubic-bezier(0.22, 1, 0.36, 1)`, viền thẻ chuyển sang màu đồng thau `--accent-amber-gold` (`#C89B57`), và nút *"Thêm vào giỏ"* trượt nhẹ từ dưới lên.  
  - **Variant Pill Selector:** Khi khách chọn biến thể (`ProductVariant` — ví dụ: *Hộp 40 nén* vs *Hộp 100 nén*, hoặc *Vòng 108 hạt 6mm* vs *8mm*), pill đang chọn có nền nâu trầm `#4A3222`, chữ vàng kem `#FAF7F2`, kèm chấm tròn báo trạng thái tồn kho (`stockQuantity` từ bảng `inventory`).

---

## PHẦN 2: SIÊU PROMPT TỔNG ĐỂ DÁN TRỰC TIẾP VÀO GOOGLE STITCH (MASTER PROMPT)

> **Copy nguyên khối dưới đây dán vào ô Prompt của Google Stitch để tạo bộ giao diện tổng thể:**

Design a luxury, serene, Zen-inspired Vietnamese E-Commerce web application named "MỘC HƯƠNG SHOP" (Aromatics & Meditation Sanctuary) specializing in natural agarwood incense (Nhang trầm hương), herbal incense cones (Trầm nụ), pure essential oils (Tinh dầu thiên nhiên), feng-shui wooden bracelets (Vòng gỗ phong thủy), and 108-bead meditation malas (Chuỗi hạt thiền định).

AESTHETIC & DESIGN DIRECTION:

\- Style: "Indochine Wabi-Sabi & Quiet Luxury" (inspired by Aesop, Le Labo, and traditional Vietnamese Zen tea houses).

\- Color Palette:

  \- Primary Background: Warm Rice Paper Cream (\#FAF7F2) with subtle organic warmth.

  \- Secondary Card/Surface Background: Warm Sandalwood Sand (\#F2ECE1).

  \- Dark Contrast Surface (Hero Banner / Footer): Deep Agarwood Charcoal (\#1E1815).

  \- Primary Brand Accent: Rich Agarwood Brown (\#4A3222).

  \- Secondary Luxury Accent: Burnished Amber Brass / Gold (\#C89B57) for active states, star ratings, and pricing highlights.

  \- Botanical / Stock Status Accent: Lotus Jade Green (\#3B5E4C) for "100% Tự Nhiên" badges and in-stock indicators.

  \- Sale / Alert Accent: Traditional Cinnabar Red (\#A8382B) for discount tags and low-stock alerts.

  \- Primary Text: Deep Ink Brown-Black (\#231C18); Muted Text: Warm Bark (\#6E5D53); Border: Bamboo Fiber (\#E5DEC9).

\- Typography:

  \- Headings: "Cormorant Garamond" or "Playfair Display" (elegant editorial serif).

  \- Body & UI Controls: "Be Vietnam Pro" or "Plus Jakarta Sans" (clean Vietnamese-friendly sans-serif).

  \- Codes & SKUs: Monospace for SKU and Order Codes (e.g., MH-8F92A1B0).

CORE NAVIGATION & LAYOUT STRUCTURE:

1\. Top Announcement Bar (Deep Agarwood \#1E1815, Gold text \#C89B57): "🌿 Miễn phí vận chuyển cho đơn hàng từ 500.000đ \| Nhập mã MOCHUONG10 giảm ngay 10% cho khách hàng mới".

2\. Sticky Header (Rice Paper \#FAF7F2 with bottom border \#E5DEC9):

   \- Left: Brand Logo "MỘC HƯƠNG" with a minimalist lotus/incense smoke emblem and subtitle "Hương Từ Tâm • Mộc Từ Đất".

   \- Center Navigation: Trang Chủ \| Nhang Trầm & Nụ \| Tinh Dầu Thiên Nhiên \| Vòng Gỗ & Chuỗi Hạt 108 \| Không Gian Thiền \| Câu Chuyện Mộc Hương.

   \- Right Actions: Search bar with keyword auto-suggest, Wishlist Heart icon with counter badge, Shopping Cart Bag icon with counter badge (triggers slide-over Redis Cart Drawer), and User Profile Avatar dropdown (supporting 3 roles: CUSTOMER, STAFF, ADMIN).

3\. Hero Section: Split asymmetric editorial layout. Left side features poetic Vietnamese typography ("Tìm Lại Khoảnh Khắc Tĩnh Lặng Giữa Đời Thường"), trust badges ("100% Trầm Thật Tự Nhiên", "Không Hóa Chất Tạo Mùi", "Chế Tác Thủ Công"), and two CTAs ("Khám Phá Bộ Sưu Tập" in \#4A3222 and "Chọn Hương Hợp Mệnh"). Right side displays an arch-framed high-resolution photograph of burning agarwood incense on a brass burner beside a 108-bead wooden mala bracelet.

4\. Category Tree Showcase (5 Curated Pillars from \`categories\` table): Arch-shaped cards with hover zoom for: Nhang Trầm Hương, Trầm Nụ & Thác Khói, Tinh Dầu Nguyên Chất, Vòng Gỗ Phong Thủy, Chuỗi Hạt Thiền 108\.

5\. Featured & Best-Selling Products Grid (4 columns desktop, 2 columns tablet, 1 column mobile):

   \- Each Product Card displays: Primary Cloudinary image (\`primaryImageUrl\`), organic tag pill (\`tags\`: "Best Seller", "Trầm Kiến Tự Nhiên"), Brand name (\`brand\`), Product Title (\`name\`), Star rating (\`averageRating\` 4.9 ★ with \`totalReviews\` count and \`totalSold\` "Đã bán 1.2k"), Starting Price (\`startingPrice\` in VNĐ format e.g. "350.000 ₫"), and quick action buttons (Wishlist toggle \+ Add to Cart).

---

## PHẦN 3: BỘ 12 PROMPT CHI TIẾT TỪNG MÀN HÌNH ĐỂ CHẠY TRÊN STITCH (SCREEN-BY-SCREEN PROMPTS)

Để giao diện khớp **100% với các API và Database** mà em đã lập trình trong 4 Microservices (`identity-service`, `catalog-service`, `order-service`, `notification-service`), em hãy dùng lần lượt các prompt dưới đây trên Stitch cho từng trang:

---

### 🌿 MÀN HÌNH 1: Trang Chủ & Không Gian Trải Nghiệm (Home & Zen Discovery Page)

**Ánh xạ API:** `GET /api/catalog/categories`, `GET /api/catalog/products?page=0&size=12`, `GET /api/order/coupons/available` **Prompt cho Stitch:**

Create the Desktop & Mobile Homepage for "MỘC HƯƠNG SHOP" using the Indochine Wabi-Sabi palette (\#FAF7F2 background, \#F2ECE1 cards, \#4A3222 primary brown, \#C89B57 brass gold, \#3B5E4C jade green).

Include the following sections in vertical order:

1\. Sticky Header with Logo, 5 Category links, Search input ("Tìm nhang trầm, tinh dầu, vòng gỗ..."), Wishlist icon, Cart Drawer trigger, and User menu.

2\. Editorial Hero Banner: Dark Agarwood background (\#1E1815) with warm gold/cream text, showcasing "Tinh Hoa Trầm Việt & Vật Phẩm Thiền Định", CTA button, and 3 key metrics ("10+ Năm Chế Tác", "100% Nguyên Liệu Sạch", "15.000+ Khách Hàng An Tâm").

3\. Category Tree Carousel (\`categories\`): 5 arched image cards with Vietnamese titles and item counts:

   \- Nhang Trầm Không Tăm

   \- Trầm Nụ & Hương Vòng

   \- Tinh Dầu Thiên Nhiên

   \- Vòng Tay Trầm Hương

   \- Chuỗi Hạt 108 & Lư Xông

4\. "Tuyệt Phẩm Bán Chạy" (Best Sellers Product Grid): 8 product cards showing \`primaryImageUrl\`, \`brand\` ("Mộc Hương Quảng Nam"), \`name\` ("Nhang Trầm Hương Kiến Tự Nhiên Thượng Hạng"), \`averageRating\` (4.9 ★), \`totalReviews\` (128 đánh giá), \`totalSold\` (Đã bán 840), and \`startingPrice\` ("285.000 ₫").

5\. "Góc Thưởng Hương & Chọn Vòng Hợp Mệnh" (Interactive Feature Banner): A 2-column zen guide helping customers choose incense scents for sleep/meditation or wooden bracelet bead sizes (6mm, 8mm, 10mm, 108 beads).

6\. Coupon Voucher Strip (\`coupons\` table): 3 horizontal traditional-ticket-styled discount cards:

   \- Mã "MOCHUONG10": Giảm 10% tối đa 50.000đ cho đơn từ 300.000đ \[Nút: Lưu Mã\]

   \- Mã "FREESHIP": Miễn phí vận chuyển toàn quốc cho đơn từ 500.000đ \[Nút: Lưu Mã\]

   \- Mã "TRAMHUONG50K": Giảm 50.000đ khi mua Vòng Gỗ & Chuỗi Hạt từ 1.000.000đ \[Nút: Lưu Mã\]

7\. Zen Footer (\#1E1815): Brand story, store address, hotline 1900 8888, VNPay/MoMo/COD payment badges, and newsletter signup.

---

### 🔍 MÀN HÌNH 2: Trang Danh Mục & Bộ Lọc Sản Phẩm Đa Tầng (Catalog & Advanced Filter Page)

**Ánh xạ API:** `GET /api/catalog/products?keyword=...&categoryId=...&minPrice=...&maxPrice=...&page=0&size=12` **Prompt cho Stitch:**

Create the Product Catalog & Filter Page for "MỘC HƯƠNG SHOP" (\#FAF7F2 background, \#4A3222 primary).

Layout:

1\. Breadcrumb & Category Header Banner: "Trang chủ / Vòng Gỗ Phong Thủy & Chuỗi Hạt 108" with a short poetic category description (\`category.description\`) and category cover image (\`category.imageUrl\`).

2\. Left Filter Sidebar (3 columns wide, sticky on desktop, slide-over drawer on mobile):

   \- Tìm kiếm từ khóa (\`keyword\` search input).

   \- Danh mục phân cấp Cha-Con (\`categories\` tree with expandable \`subCategories\`):

     \- Nhang & Trầm Đốt (Nhang có tăm, Nhang không tăm, Nụ trầm, Hương vòng)

     \- Tinh Dầu & Khuếch Tán (Tinh dầu đơn hương, Tinh dầu phối hương, Máy khuếch tán)

     \- Vòng Gỗ & Chuỗi Hạt (Trầm hương tự nhiên, Gỗ Tử Đàn, Gỗ Hoàng Đàn, Chuỗi 108 hạt)

   \- Khoảng giá (\`minPrice\` \- \`maxPrice\` dual slider & quick checkboxes: Dưới 200.000đ, 200.000đ \- 500.000đ, 500.000đ \- 2.000.000đ, Trên 2.000.000đ).

   \- Thương hiệu / Xuất xứ (\`brand\`): Trầm Hương Khánh Hòa, Trầm Quảng Nam, Tinh Dầu Huế, Mộc Hương Thủ Công.

   \- Tags đặc tính (\`tags\` JSON): "Organic", "Best Seller", "Thiền định", "Quà tặng cao cấp".

3\. Right Main Content (9 columns wide):

   \- Top Toolbar: Showing "Hiển thị 1–12 trong 48 sản phẩm", active filter chips with "Xóa bộ lọc", and Sort dropdown (Bán chạy nhất \`totalSold DESC\`, Đánh giá cao nhất \`averageRating DESC\`, Giá thấp đến cao, Mới nhất).

   \- 3x4 Product Card Grid with hover quick-add, Wishlist heart toggle, and clear pagination bar at the bottom.

---

### 📿 MÀN HÌNH 3: Trang Chi Tiết Sản Phẩm Đa Biến Thể & Đánh Giá Có Ảnh (Product Detail & Reviews Page)

**Ánh xạ API:** `GET /api/catalog/products/{slug}` (`ProductDetailResponse`), `GET /api/catalog/products/{productId}/reviews` (`ReviewResponse`), `POST /api/order/cart/add` **Prompt cho Stitch:**

Create the Product Detail Page (PDP) for "MỘC HƯƠNG SHOP" mapped to \`ProductDetailResponse\` and \`ReviewResponse\`.

1\. Top 2-Column Product Hero:

   \- Left Column (Gallery \- \`images\[\]\`): Large main square image with rounded corners (12px) showing "Vòng Tay Trầm Hương Tốc Kiến Tự Nhiên 108 Hạt", plus a horizontal thumbnail strip below (\`isPrimary\` highlighted with \#C89B57 border).

   \- Right Column (Product Info & Variant Selector):

     \- Brand & Tags row: Badge "Mộc Hương Quảng Nam" (\`brand\`) \+ Jade Green badge "100% Trầm Tự Nhiên" (\`tags\`).

     \- Product Title (\`name\`): "Vòng Tay Trầm Hương Tốc Kiến Tự Nhiên 108 Hạt" in Cormorant Garamond 36px.

     \- Rating & Sold summary: "4.9 ★★★★★ (86 đánh giá) \| Đã bán 420 \| Mã SKU: MH-VG-108-06 (\`variant.sku\`)".

     \- Dynamic Price Block: Current Variant Price \`1.450.000 ₫\` (\`variant.price\` in 28px bold \#4A3222) \+ strikethrough Original Price \`1.800.000 ₫\` (\`originalPrice\`) \+ Cinnabar Red badge "-19%".

     \- Short Description (\`shortDescription\`): Warm, calming summary of the scent, wood grain, and meditation benefits.

     \- Variant Selector (\`variants\[\]\` from \`product\_variants\` table): Interactive pill buttons showing \`variantName\` and price:

       \- \[Hạt 6mm \- 108 Hạt (Nữ) • 1.450.000đ\] (Active selected state)

       \- \[Hạt 8mm \- 108 Hạt (Nam/Nữ) • 1.850.000đ\]

       \- \[Hộp Quà Sơn Mài Cao Cấp \+ Nụ Trầm • 2.200.000đ\]

     \- Real-time Inventory Indicator (\`stockQuantity\` from \`inventory\` table): Green dot "Còn 18 sản phẩm trong kho" (or low-stock alert "Chỉ còn 3 sản phẩm cuối cùng\!").

     \- Quantity Stepper (\`-\` \`1\` \`+\`) \+ Primary CTA "Thêm Vào Giỏ Hàng" (\#4A3222) \+ Secondary CTA "Mua Ngay" (\#C89B57) \+ Wishlist Heart button.

     \- 3 Assurance Icons: Cam kết trầm thật trọn đời \| Đổi trả trong 7 ngày \| Giao hàng hỏa tốc.

2\. Middle Section: Tabbed Editorial Details (\`description\`, \`weightGram\`, \`attributes\` JSON):

   \- Tab 1: Câu chuyện & Công dụng sản phẩm (\`description\` rich text).

   \- Tab 2: Thông số kỹ thuật (\`attributes\`: Xuất xứ Tiên Phước \- Quảng Nam, Tuổi trầm 12-15 năm, Khối lượng \`weight\_gram\`, Mùi hương ngọt dịu thanh mát).

   \- Tab 3: Hướng dẫn bảo quản & Thưởng hương.

3\. Bottom Section: Customer Reviews & Admin Replies (\`reviews\` & \`review\_images\` tables):

   \- Left Summary Box: Overall score \`4.9 / 5.0\`, star distribution bars (5 sao 92%, 4 sao 8%), filter pills ("Tất cả", "Có hình ảnh", "5 Sao").

   \- Right Review Feed (\`ReviewResponse\`):

     \- Each review item shows: Customer avatar & name, Verified Purchase badge ("Đã mua hàng tại Mộc Hương"), Star rating (\`rating\`), Variant purchased (\`variantName\`: "Phân loại: Hạt 6mm \- 108 Hạt"), Review Title (\`title\`), Review Content (\`content\`), a row of uploaded Cloudinary review photos (\`images\[\]\` with lightbox preview), \`createdAt\` timestamp, Edit/Delete actions (if current user owns the review), and an indented warm sand box (\`\#F2ECE1\` with left gold border \`\#C89B57\`) showing \*\*"Phản hồi từ Mộc Hương Shop (\`adminReply\`)"\*\*\!

---

### 🛍️ MÀN HÌNH 4: Ngăn Kéo Giỏ Hàng Redis & Áp Mã Giảm Giá (Slide-Over Redis Cart Drawer & Full Cart Page)

**Ánh xạ API:** `GET /api/order/cart`, `PUT /api/order/cart/update`, `DELETE /api/order/cart/remove/{productId}/{variantId}`, `POST /api/order/coupons/preview` **Prompt cho Stitch:**

Create a Slide-Over Shopping Cart Drawer AND a Full Shopping Cart Page for "MỘC HƯƠNG SHOP" mapped to Redis \`CartItem\` (\`productId, variantId, productName, variantName, productImageUrl, price, quantity\`).

Features:

1\. Header: "Giỏ Hàng Của Bạn (3 vật phẩm)" with a Free Shipping progress bar ("Mua thêm 120.000 ₫ để được Miễn Phí Vận Chuyển\!").

2\. Cart Item List:

   \- Each row shows: Thumbnail (\`productImageUrl\`), Product Name (\`productName\`), Selected Variant Badge (\`variantName\`: "Hộp 100 nén \- 200g"), Unit Price (\`price\`), Quantity Stepper (\`-\` \`2\` \`+\`), Line Subtotal (\`price \* quantity\`), and a trash icon to remove item.

3\. Coupon Code Box (\`coupons\`):

   \- Input field "Nhập mã ưu đãi Mộc Hương (VD: MOCHUONG10)" \+ "Áp Dụng" button \+ "Chọn mã giảm giá có sẵn" modal trigger showing eligible coupons (\`PERCENTAGE\`, \`FIXED\_AMOUNT\`, \`FREE\_SHIPPING\`).

4\. Order Summary Footer:

   \- Tạm tính (\`subtotal\`): \`850.000 ₫\`

   \- Giảm giá (\`discountAmount\` \- Mã MOCHUONG10): \`-85.000 ₫\`

   \- Phí vận chuyển dự kiến (\`shippingFee\`): \`30.000 ₫\`

   \- Tổng thanh toán (\`totalAmount\`): \`795.000 ₫\`

   \- Full-width CTA button: "Tiến Hành Đặt Hàng →" (\#4A3222).

---

### 💳 MÀN HÌNH 5: Trang Thanh Toán Đặt Hàng & Cổng VNPay (Checkout & VNPay Return Page)

**Ánh xạ API:** `GET /api/identity/users/me/addresses`, `POST /api/order/orders/checkout` (`CheckoutRequest`), `GET /api/order/payment/create-url/{orderId}`, `GET /api/order/payment/vnpay-return` **Prompt cho Stitch:**

Create the 2-Column Checkout Page and Payment Result Modal for "MỘC HƯƠNG SHOP" mapped to \`CheckoutRequest\` (\`shippingAddress\`, \`paymentMethod\`, \`couponCode\`, \`notes\`, \`idempotencyKey\`).

1\. Left Column (Shipping & Payment Configuration \- 7 columns):

   \- Step 1: Địa chỉ nhận hàng (\`shippingAddress\` JSON from \`user\_addresses\`):

     \- Saved Address Selector Cards: Radio cards showing \`recipientName\` ("Lê Minh Triết"), \`recipientPhone\` ("0909 123 456"), full address (\`streetAddress, wardName, districtName, provinceName\`), and "Mặc định" (\`isDefault\`) badge, plus "+ Thêm địa chỉ giao hàng mới" button.

   \- Step 2: Ghi chú đơn hàng (\`notes\`): Textarea "Lời nhắn cho Mộc Hương (VD: Gói quà tặng lịch sự, giao giờ hành chính...)".

   \- Step 3: Phương thức thanh toán (\`paymentMethod\` ENUM: \`COD\`, \`VNPAY\`, \`MOMO\`):

     \- Card 1 (\`COD\`): "Thanh toán khi nhận hàng (COD) — Kiểm tra hàng trước khi thanh toán".

     \- Card 2 (\`VNPAY\`): "Thanh toán trực tuyến qua cổng VNPay (QR Ngân hàng / Thẻ ATM / Visa)" with official VNPay logo and badge "Khuyên dùng • Xác nhận tức thì".

     \- Card 3 (\`MOMO\`): "Ví điện tử MoMo".

2\. Right Column (Sticky Order Summary \- 5 columns):

   \- Itemized list of \`CartItem\` with quantities and prices, Coupon selector, Subtotal, Shipping Fee (\`30.000 ₫\`), Discount Amount, Total Amount (\`totalAmount\`), and primary button "Hoàn Tất Đặt Hàng" (protected against double-click via \`idempotencyKey\`).

3\. Include a companion "Kết Quả Thanh Toán VNPay (\`vnpay-return\`)" screen showing:

   \- Success State (\`vnp\_ResponseCode \= 00\`): Lotus Jade Green checkmark, Order Code \`MH-8A9F2C1B\`, Transaction Code, Amount Paid, and buttons "Xem Chi Tiết Đơn Hàng" / "Tải Hóa Đơn PDF".

---

### 📦 MÀN HÌNH 6: Trang Lịch Sử Đơn Hàng, Timeline Trạng Thái & Viết Đánh Giá (Customer Orders & Order Tracking Page)

**Ánh xạ API:** `GET /api/order/orders`, `GET /api/order/orders/{orderCode}`, `PUT /api/order/orders/{orderId}/cancel`, `POST /api/catalog/products/{productId}/reviews` (Multipart) **Prompt cho Stitch:**

Create the Customer "Đơn Hàng Của Tôi" & Order Detail Tracking Screen for "MỘC HƯƠNG SHOP" mapped to \`orders\`, \`order\_items\`, \`order\_status\_history\`, and \`transactions\` tables.

1\. Status Filter Tabs (matching exact MySQL ENUM \`orders.status\`):

   \- Tất cả \| Chờ thanh toán (\`PENDING\_PAYMENT\`) \| Đã xác nhận (\`CONFIRMED\`) \| Đang đóng gói (\`PREPARING\`) \| Đang giao (\`SHIPPING\`) \| Đã giao (\`DELIVERED\`) \| Hoàn tất (\`COMPLETED\`) \| Đã hủy (\`CANCELLED\`) \| Trả hàng (\`RETURN\_REQUESTED\` / \`RETURNED\`).

2\. Order Detail View:

   \- Header bar: Mã đơn \`MH-8A9F2C1B\` \| Ngày đặt \`26/10/2026 14:30\` \| Trạng thái thanh toán \`PAID\` (VNPay) \| Nút "📄 Xuất Hóa Đơn PDF".

   \- Visual 5-Step Horizontal Stepper \+ Vertical Audit Timeline (\`order\_status\_history\`): Shows \`fromStatus \-\> toStatus\`, timestamp \`createdAt\`, actor (\`changedByType\`), and \`note\`.

   \- Shipping Address Snapshot Card (\`shipping\_address\` JSON) & Payment Transaction Card (\`transactions\`).

   \- Order Items Table (\`order\_items\`): Shows \`productImageUrl\`, \`productName\`, \`variantName\`, \`sku\`, \`unitPrice\`, \`quantity\`, \`subtotal\`.

   \- Contextual Action Buttons based on Status:

     \- If \`status \== PENDING\_PAYMENT\`: Show "Thanh Toán Lại Qua VNPay" and "Hủy Đơn Hàng" (\`PUT /cancel\` with reason modal).

     \- If \`status \== DELIVERED\` or \`COMPLETED\`: Show "⭐ Viết Đánh Giá" button next to each \`OrderItem\` (opens a Modal allowing 1–5 star rating, \`title\`, \`content\`, and drag-and-drop upload of up to 5 real product photos to Cloudinary\!) and "Yêu Cầu Trả Hàng".

---

### 👤 MÀN HÌNH 7: Không Gian Cá Nhân: Hồ Sơ, Sổ Địa Chỉ & Danh Sách Yêu Thích (User Profile, Address Book & Wishlist)

**Ánh xạ API:** `GET/PUT /api/identity/users/me`, `GET/POST/PUT/DELETE /api/identity/users/me/addresses`, `GET/POST/DELETE /api/identity/users/me/wishlists` **Prompt cho Stitch:**

Create the Customer Account Dashboard for "MỘC HƯƠNG SHOP" with a 3-column Left Sidebar Navigation and 9-column Content Area:

1\. Sidebar Menu: User Avatar (\`avatarUrl\`), Full Name (\`fullName\`), Role Badge (\`CUSTOMER\`), Email Verification Badge (\`isEmailVerified\`: "Đã xác thực Email"), and links: Thông Tin Cá Nhân \| Sổ Địa Chỉ Giao Hàng \| Sản Phẩm Yêu Thích \| Lịch Sử Đơn Hàng \| Đổi Mật Khẩu.

2\. View A \- Thông Tin Cá Nhân (\`users\` table): Form editing \`fullName\`, \`email\` (read-only), \`phoneNumber\`, \`dateOfBirth\`, and Avatar upload preview.

3\. View B \- Sổ Địa Chỉ (\`user\_addresses\` table): List of saved addresses showing \`recipientName\`, \`recipientPhone\`, \`streetAddress\`, \`wardName\`, \`districtName\`, \`provinceName\`, badge "Mặc định" (\`isDefault\`), and actions "Đặt làm mặc định", "Chỉnh sửa", "Xóa", plus an "Thêm Địa Chỉ Mới" modal with 3 cascading dropdowns (Tỉnh/Thành \-\> Quận/Huyện \-\> Phường/Xã).

4\. View C \- Danh Sách Yêu Thích (\`wishlists\` table): Grid of saved favorite incense & wooden bracelet products with one-click "Chuyển vào giỏ hàng" and remove buttons.

---

### 🔐 MÀN HÌNH 8: Bộ Màn Hình Xác Thực: Đăng Nhập, Đăng Ký, Xác Thực Email & Quên Mật Khẩu (Auth Suite)

**Ánh xạ API:** `POST /api/identity/auth/login`, `POST /api/identity/auth/register`, `GET /api/identity/auth/verify-email`, `POST /api/identity/auth/forgot-password`, `POST /api/identity/auth/reset-password` **Prompt cho Stitch:**

Create a Split-Screen Authentication Suite for "MỘC HƯƠNG SHOP":

\- Left Half (Visual Sanctuary): Full-height artistic photography of a peaceful Vietnamese wooden tea table with curling agarwood incense smoke, overlaid with a quote in Cormorant Garamond: "Một nén tâm hương, vạn dặm bình an."

\- Right Half (Clean Form Card on \#FAF7F2 Rice Paper background):

  \- Tabbed switcher between "Đăng Nhập" (\`email\`, \`password\`, "Ghi nhớ đăng nhập", "Quên mật khẩu?") and "Đăng Ký Tài Khoản" (\`fullName\`, \`email\`, \`password\`, confirm password).

  \- Divider "Hoặc tiếp tục với" \+ Button "Đăng nhập bằng Google" (\`Provider.GOOGLE\` with Google icon).

  \- Include companion states/modals for:

    1\. "Xác Thực Email (\`VERIFY\_EMAIL\`)" — 6-digit OTP / verification link confirmation screen.

    2\. "Quên Mật Khẩu & Đặt Lại Mật Khẩu (\`RESET\_PASSWORD\`)" — Email input and new password confirmation form.

---

### 🛠️ MÀN HÌNH 9: Cổng Quản Trị `STAFF` & `ADMIN` — Dashboard Doanh Thu & Cảnh Báo Kho (Admin & Staff Portal Overview)

**Ánh xạ Role:** `@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")` **Ánh xạ API:** `GET /api/order/admin/orders/statistics`, `GET /api/catalog/inventory/low-stock`, `GET /api/order/admin/orders/export-excel` **Prompt cho Stitch:**

Create the Admin & Staff Back-Office Dashboard for "MỘC HƯƠNG SHOP" tailored for \`STAFF\` and \`ADMIN\` roles.

Design Style: Warm Executive Dashboard — sidebar in Deep Agarwood (\`\#1E1815\`) with Gold (\`\#C89B57\`) active indicators, main workspace in clean \`\#FAF7F2\` with crisp data cards and \`\#E5DEC9\` borders.

Include:

1\. Left Navigation Sidebar (Role-aware):

   \- For both \`STAFF\` & \`ADMIN\`: Tổng Quan \| Quản Lý Đơn Hàng \| Sản Phẩm & Biến Thể \| Danh Mục \| Quản Lý Tồn Kho (\`Inventory\`) \| Kiểm Duyệt Đánh Giá (\`Reviews\`) \| Mã Giảm Giá (\`Coupons\`).

   \- Exclusive section for \`ADMIN\` only (with lock/shield icon): Quản Lý Người Dùng & Phân Quyền (\`Users & Roles\`) \| Mẫu Thông Báo & Nhật Ký Email (\`Notification Logs\`).

2\. Top KPI Metric Cards (4 cards):

   \- Tổng Doanh Thu Tháng (\`totalAmount\` PAID): \`128.450.000 ₫\` (+18.4% so với tháng trước)

   \- Đơn Hàng Cần Xử Lý (\`PENDING\_PAYMENT\` / \`CONFIRMED\`): \`14 đơn\`

   \- Tỷ Lệ Giao Thành Công (\`DELIVERED\` / \`COMPLETED\`): \`96.2%\`

   \- Cảnh Báo Sắp Hết Kho (\`quantity \<= low\_stock\_threshold\`): \`5 biến thể\` (Cinnabar Red alert)

3\. Middle Charts & Action Row:

   \- Left (8 cols): Revenue & Order Volume Area Chart by day/month with date-range picker and button "📊 Xuất Báo Cáo Excel (\`.xlsx\`)" (\`poi-ooxml\`).

   \- Right (4 cols): "Cảnh Báo Tồn Kho Thấp (\`low\_stock\_threshold\`)" table listing variants running out of stock (e.g., "Nhang Trầm Hộp 100 nén \- Còn 4 hộp", "Tinh dầu Đàn Hương 10ml \- Còn 2 lọ") with a quick "Nhập Kho Ngay" button.

---

### 📦 MÀN HÌNH 10: Quản Lý Sản Phẩm, Đa Biến Thể, Thùng Rác & Tồn Kho Redisson (`STAFF` & `ADMIN`)

**Ánh xạ API:** `POST/PUT/DELETE /api/catalog/products`, `GET /api/catalog/products/inactive`, `PUT /api/catalog/products/{id}/restore`, `DELETE /api/catalog/products/{id}/force`, `PUT /api/catalog/inventory/variant/{variantId}` **Prompt cho Stitch:**

Create the Product, Multi-Variant & Inventory Management Screen for \`STAFF\` and \`ADMIN\` in "MỘC HƯƠNG SHOP":

1\. Top Tabs:

   \- "Sản Phẩm Đang Bán (\`isActive \= true\`)"

   \- "Thùng Rác Sản Phẩm (\`GET /inactive\` \- \`isActive \= false\`)" with badge count, featuring two action buttons per row: "♻️ Khôi Phục (\`PUT /restore\`)" and "🔥 Xóa Vĩnh Viễn & Dọn Ảnh Cloudinary (\`DELETE /force\` \- Admin only)".

   \- "Quản Lý Tồn Kho (\`Inventory\`)".

2\. Product Creation / Edit Modal (Multipart Form):

   \- Basic Info: Tên sản phẩm (\`name\`), Danh mục cha/con (\`categoryId\`), Thương hiệu (\`brand\`), Mô tả ngắn (\`shortDescription\`), Mô tả chi tiết (\`description\`), Tags JSON (\`tags\`), Trạng thái hiển thị (\`isPublished\`).

   \- Cloudinary Multi-Image Uploader: Drag-and-drop zone showing uploaded images (\`ProductImage\`), radio button to pick "Ảnh đại diện chính (\`isPrimary\`)", drag handle for \`sortOrder\`, and trash button to mark old images for deletion (\`deletedImageIds\[\]\`).

   \- Dynamic Multi-Variant Table (\`product\_variants\` \+ \`inventory\`): Add/edit rows for each variant:

     \- Mã SKU (\`sku\`), Tên biến thể (\`variantName\` e.g. "Hộp 40 nén", "Hạt 8mm"), Giá bán (\`price\`), Giá gốc (\`originalPrice\`), Khối lượng (\`weightGram\`), Thuộc tính JSON (\`attributes\`), Tồn kho hiện tại (\`quantity\`), Đang giữ chỗ (\`reservedQuantity\`), và Ngưỡng cảnh báo hết hàng (\`lowStockThreshold\`).

---

### 🚚 MÀN HÌNH 11: Quản Lý Đơn Hàng, Duyệt Trạng Thái & Quản Lý Mã Giảm Giá (`STAFF` & `ADMIN`)

**Ánh xạ API:** `GET /api/order/admin/orders`, `PUT /api/order/admin/orders/{id}/status`, `POST/PUT/DELETE /api/order/admin/coupons` **Prompt cho Stitch:**

Create the Admin/Staff Order Processing & Coupon Management Screen for "MỘC HƯƠNG SHOP":

1\. Order Management Table (\`orders\` table):

   \- Filters: Search by \`orderCode\` (\`MH-...\`), Filter by \`status\` (9 states), \`paymentMethod\` (\`COD\`, \`VNPAY\`, \`MOMO\`), \`paymentStatus\` (\`UNPAID\`, \`PAID\`, \`REFUNDED\`), and Date Range.

   \- Row Columns: Mã đơn (\`orderCode\`), Khách hàng, Tổng tiền (\`totalAmount\`), Phương thức & TT Thanh toán, Trạng thái hiện tại (\`status\` badge), Ngày đặt (\`createdAt\`), và nút "Xử Lý Đơn".

2\. Order Processing Drawer (Slide-over):

   \- State Machine Action Bar: Allows Staff/Admin to advance order to the next valid state (\`PENDING\_PAYMENT\` \-\> \`CONFIRMED\` \-\> \`PREPARING\` \-\> \`SHIPPING\` \-\> \`DELIVERED\` \-\> \`COMPLETED\`, or \`CANCELLED\` / \`RETURNED\` with required \`cancelledReason\`).

   \- Internal Staff Notes (\`staff\_notes\` textarea) vs Customer Notes (\`notes\`).

   \- Button "🖨️ In Hóa Đơn PDF (\`iText7\`)" for packing slip.

3\. Coupon Management Sub-Tab (\`coupons\` & \`coupon\_usages\` tables):

   \- Create/Edit Coupon Modal with fields: Mã code (\`code\`), Tên chương trình (\`name\`), Loại giảm giá (\`type\`: \`PERCENTAGE\`, \`FIXED\_AMOUNT\`, \`FREE\_SHIPPING\`), Giá trị (\`value\`), Giảm tối đa (\`maxDiscountAmount\`), Đơn tối thiểu (\`minOrderValue\`), Tổng lượt dùng tối đa (\`maxUses\`), Giới hạn mỗi khách (\`maxUsesPerUser\`), Ngày bắt đầu (\`startsAt\`), Ngày kết thúc (\`expiresAt\`), và tiến độ đã dùng (\`usedCount / maxUses\`).

---

### 🛡️ MÀN HÌNH 12: Quản Trị Người Dùng (`ADMIN`), Kiểm Duyệt Đánh Giá & Nhật Ký Thông Báo (`Notification Logs`)

**Ánh xạ API:** `GET/PUT /api/identity/admin/users/**`, `GET /api/catalog/products/{productId}/admin/reviews`, `PUT /api/catalog/products/reviews/{reviewId}/reply`, `PUT /api/catalog/products/reviews/{reviewId}/toggle-visibility` **Prompt cho Stitch:**

Create the Admin Governance Screen for "MỘC HƯƠNG SHOP" combining 3 management modules:

1\. Tab 1 — Quản Lý Người Dùng & Phân Quyền (\`ADMIN\` only \- \`users\` table):

   \- Table showing: ID, Avatar, Họ tên (\`fullName\`), Email (\`email\`), SĐT (\`phoneNumber\`), Phương thức ĐN (\`provider\`: \`LOCAL\` / \`GOOGLE\`), Vai trò (\`role\`: dropdown to switch between \`CUSTOMER\`, \`STAFF\`, \`ADMIN\`), Trạng thái (\`isActive\`: toggle switch "Đang hoạt động" / "Đã khóa" which revokes all Refresh Tokens), và Ngày tham gia.

2\. Tab 2 — Kiểm Duyệt & Phản Hồi Đánh Giá (\`STAFF\` & \`ADMIN\` \- \`reviews\` table):

   \- Shows all reviews including hidden ones (\`isVisible \= false\`).

   \- Each review card has: Customer rating & photos, Visibility Toggle Switch ("Đang hiển thị" / "Đã ẩn khỏi trang sản phẩm" — \`PUT /toggle-visibility\`), and an inline reply box ("Nhập phản hồi từ Mộc Hương Shop..." — \`PUT /reply\`) that saves \`adminReply\` and \`adminRepliedAt\`.

3\. Tab 3 — Nhật Ký Hệ Thống Thông Báo Email/SMS (\`notification\_logs\` & \`notification\_templates\` tables):

   \- Audit table showing: \`idempotencyKey\`, Sự kiện (\`eventType\`: \`ORDER\_CREATED\`, \`ORDER\_CANCELLED\`, \`VERIFY\_EMAIL\`), Kênh (\`EMAIL\`), Người nhận (\`recipient\`), Tiêu đề (\`subject\`), Trạng thái gửi (\`status\`: \`SENT\` green, \`PENDING\` amber, \`FAILED\` red with \`errorMessage\`), Số lần thử lại (\`retryCount\`), và Thời gian gửi (\`sentAt\`).

---

## PHẦN 4: CẤU TRÚC THƯ MỤC FRONTEND KHUYẾN NGHỊ SAU KHI XUẤT CODE TỪ STITCH

Khi em dùng Stitch xuất ra code HTML/Tailwind hoặc React/Next.js, em hãy bảo Antigravity tổ chức theo cấu trúc thư mục dưới đây để nối thẳng vào `api-gateway` (`http://localhost:8080`):

moc-huong-frontend/

├── public/

│   └── textures/                  \# Ảnh nền giấy Dó nhẹ, icon hoa sen, logo Mộc Hương

├── src/

│   ├── api/                       \# Axios clients nối vào API Gateway (:8080)

│   │   ├── axiosClient.ts         \# Tự động đính kèm Bearer accessToken & gọi /api/identity/auth/refresh khi lỗi 401

│   │   ├── authApi.ts             \# /api/identity/auth/\*\* & /api/identity/users/\*\*

│   │   ├── catalogApi.ts          \# /api/catalog/products/\*\*, /categories/\*\*, /inventory/\*\*

│   │   └── orderApi.ts            \# /api/order/cart/\*\*, /orders/\*\*, /payment/\*\*, /coupons/\*\*

│   ├── components/

│   │   ├── layout/                \# Header, ZenFooter, CartDrawer, RoleBasedGuard (CUSTOMER, STAFF, ADMIN)

│   │   ├── product/               \# ProductCard, VariantSelector, ReviewListWithImages

│   │   └── checkout/              \# AddressSelector, CouponModal, VNPayButton

│   ├── pages/

│   │   ├── storefront/            \# Home, Catalog, ProductDetail, Checkout, VNPayReturn

│   │   ├── customer/              \# Profile, AddressBook, Wishlist, MyOrders

│   │   └── admin/                 \# Dashboard, AdminProducts, AdminInventory, AdminOrders, AdminCoupons, AdminUsers, AdminReviews

│   └── styles/

│       └── design-tokens.css      \# Chứa bộ biến màu \--bg-rice-paper, \--brand-agarwood, \--accent-amber-gold