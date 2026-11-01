package com.minhtriet.se3979.catalogservice.config;

import com.minhtriet.se3979.catalogservice.entity.Category;
import com.minhtriet.se3979.catalogservice.entity.Inventory;
import com.minhtriet.se3979.catalogservice.entity.Product;
import com.minhtriet.se3979.catalogservice.entity.ProductImage;
import com.minhtriet.se3979.catalogservice.entity.ProductVariant;
import com.minhtriet.se3979.catalogservice.entity.Review;
import com.minhtriet.se3979.catalogservice.repository.CategoryRepository;
import com.minhtriet.se3979.catalogservice.repository.InventoryRepository;
import com.minhtriet.se3979.catalogservice.repository.ProductImageRepository;
import com.minhtriet.se3979.catalogservice.repository.ProductRepository;
import com.minhtriet.se3979.catalogservice.repository.ProductVariantRepository;
import com.minhtriet.se3979.catalogservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            log.info("🌿 Đang khởi tạo 5 Danh mục chuẩn Mộc Hương Shop...");

            Category c1 = categoryRepository.save(Category.builder()
                    .name("Nhang Trầm Hương Tự Nhiên")
                    .slug("nhang-tram-huong")
                    .description("Nhang trầm hương sạch, tự nhiên 100%, không hóa chất độc hại")
                    .isActive(true)
                    .build());

            Category c2 = categoryRepository.save(Category.builder()
                    .name("Trầm Nụ & Hương Vòng")
                    .slug("tram-nu-huong-vong")
                    .description("Nụ trầm chảy ngược thác khói và hương vòng thư giãn tinh thần")
                    .isActive(true)
                    .build());

            Category c3 = categoryRepository.save(Category.builder()
                    .name("Tinh Dầu Thiên Nhiên Nguyên Chất")
                    .slug("tinh-dau-thien-nhien")
                    .description("Tinh dầu thiên nhiên nguyên chất ép lạnh và chưng cất hơi nước")
                    .isActive(true)
                    .build());

            Category c4 = categoryRepository.save(Category.builder()
                    .name("Vòng Gỗ Phong Thủy & Trầm Hương")
                    .slug("vong-go-phong-thuy")
                    .description("Vòng tay gỗ trầm hương, tử đàn, bách xanh phong thủy tài lộc")
                    .isActive(true)
                    .build());

            Category c5 = categoryRepository.save(Category.builder()
                    .name("Chuỗi Hạt Thiền Định 108 Hạt & Lư Xông")
                    .slug("chuoi-hat-thien-dinh")
                    .description("Chuỗi hạt niệm Phật thiền định và các loại lư xông đồng gốm cao cấp")
                    .isActive(true)
                    .build());

            log.info("📦 Đang khởi tạo 12 Sản phẩm mẫu kèm đa biến thể và kho hàng...");

            // 1. Nhang Trầm Kiến (Khớp với OrderDataSeeder Đơn 1, 4, 6)
            Product p1 = createProductWithVariants(c1, "Nhang Trầm Hương Kiến Tự Nhiên Thượng Hạng (30 Năm Tích Trầm)",
                    "nhang-tram-huong-kien-30-nam",
                    "Nhang trầm hương kiến tự nhiên 30 năm tích trầm, khói thanh ngọt sâu lắng", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-NTH-50G", "Hộp 50g (~65 nén)", new BigDecimal("350000"), new BigDecimal("420000"), 100, 50),
                            new VariantSeed("MH-NTH-100G", "Hộp 100g (~130 nén)", new BigDecimal("680000"), new BigDecimal("800000"), 200, 40),
                            new VariantSeed("MH-NTH-200G-VIP", "Ống Sơn Mài Quà Biếu 200g", new BigDecimal("1950000"), new BigDecimal("2200000"), 400, 15)
                    ));

            // 2. Nhang Vòng Trầm Hương (Khớp với OrderDataSeeder Đơn 5)
            Product p2 = createProductWithVariants(c2, "Nhang Vòng Trầm Hương Quảng Nam 24 Giờ Tịnh Tâm",
                    "nhang-vong-tram-huong-quang-nam-24h",
                    "Hương vòng trầm hương Quảng Nam cháy liên tục 24 giờ thư thái không gian am thiền", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-NV24H-10V", "Hộp 10 vòng lớn", new BigDecimal("450000"), new BigDecimal("520000"), 200, 35),
                            new VariantSeed("MH-NV24H-20V", "Hộp 20 vòng lớn", new BigDecimal("850000"), new BigDecimal("990000"), 400, 20)
                    ));

            // 3. Trầm Nụ Thác Khói (Khớp với OrderDataSeeder Đơn 1)
            Product p3 = createProductWithVariants(c2, "Nụ Trầm Hương Khói Ngược Thác Liên Hoa (45 Nụ Cao Cấp)",
                    "nu-tram-huong-khoi-nguoc-thac-lien-hoa",
                    "Nụ trầm hương khói chảy ngược bồng bềnh như mây bồng bềnh non nước", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-NT-45V", "Hộp 45 nụ", new BigDecimal("300000"), new BigDecimal("360000"), 90, 60),
                            new VariantSeed("MH-NT-90V", "Hộp 90 nụ (Tặng đế đốt)", new BigDecimal("550000"), new BigDecimal("680000"), 180, 45)
                    ));

            // 4. Lư Xông Trầm Tử Sa (Khớp với OrderDataSeeder Đơn 5)
            Product p4 = createProductWithVariants(c5, "Lư Xông Trầm Gốm Tử Sa Nghi Hưng Thủ Công Nắp Đồng Chạm Hoa Sen",
                    "lu-xong-tram-gom-tu-sa-nghi-hung",
                    "Lư gốm tử sa Nghi Hưng chế tác thủ công tinh xảo xông nụ xông vòng thanh khiết", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-LX-TUSA-01", "Bộ lư tiêu chuẩn", new BigDecimal("420000"), new BigDecimal("500000"), 350, 30),
                            new VariantSeed("MH-LX-TUSA-VIP", "Lễ Hộp Sơn Mài Kèm Lư & Nụ", new BigDecimal("790000"), new BigDecimal("950000"), 650, 15)
                    ));

            // 5. Tinh Dầu Trầm Hương (Khớp với OrderDataSeeder Đơn 3)
            Product p5 = createProductWithVariants(c3, "Tinh Dầu Trầm Hương Nguyên Chất Thượng Hạng (Agarwood Essential Oil)",
                    "tinh-dau-tram-huong-nguyen-chat-thuong-hang",
                    "Tinh dầu chưng cất hơi nước từ trầm hương tự nhiên hương thơm lưu giữ trường tồn", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-TD-TH-10ML", "Lọ 10ml", new BigDecimal("270000"), new BigDecimal("320000"), 50, 40),
                            new VariantSeed("MH-TD-TH-30ML", "Lọ 30ml", new BigDecimal("680000"), new BigDecimal("850000"), 100, 25)
                    ));

            // 6. Vòng Tay Trầm Hương 108 Hạt (Khớp với OrderDataSeeder Đơn 2)
            Product p6 = createProductWithVariants(c4, "Vòng Tay Trầm Hương Tốc Kiến Tự Nhiên 108 Hạt Phối Charm Sen Vàng",
                    "vong-tay-tram-huong-toc-kien-108-hat-charm-sen",
                    "Vòng trầm tốc kiến vân dầu tự nhiên phối charm hoa sen mạ vàng cát tường", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-VT108-6MM", "Size hạt 6mm (Nữ)", new BigDecimal("2850000"), new BigDecimal("3200000"), 30, 20),
                            new VariantSeed("MH-VT108-8MM", "Size hạt 8mm (Nam)", new BigDecimal("3450000"), new BigDecimal("3900000"), 45, 15)
                    ));

            // 7. Nhang Trầm Hương Không Tăm
            createProductWithVariants(c1, "Nhang Trầm Hương Không Tăm Thư Giãn Phòng Trà",
                    "nhang-tram-khong-tam-phong-tra",
                    "Nhang không tăm thanh khiết dành riêng cho thưởng trà và đọc sách", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-NKT-40N", "Ống tre 40 nén", new BigDecimal("220000"), new BigDecimal("260000"), 60, 45),
                            new VariantSeed("MH-NKT-100N-VIP", "Hộp gỗ cao cấp 100 nén", new BigDecimal("520000"), new BigDecimal("620000"), 150, 25)
                    ));

            // 8. Tinh Dầu Đàn Hương
            createProductWithVariants(c3, "Tinh Dầu Đàn Hương Mysore Nguyên Chất (Sandalwood Oil)",
                    "tinh-dau-dan-huong-mysore-nguyen-chat",
                    "Hương gỗ đàn hương ấm nồng ngọt ngào xua tan lo âu phiền muộn", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-TDDH-10ML", "Chai nhỏ giọt 10ml", new BigDecimal("390000"), new BigDecimal("480000"), 50, 35),
                            new VariantSeed("MH-TDDH-30ML", "Chai nhỏ giọt 30ml", new BigDecimal("990000"), new BigDecimal("1250000"), 100, 20)
                    ));

            // 9. Tinh Dầu Bưởi Hồng & Sả Chanh
            createProductWithVariants(c3, "Tinh Dầu Bưởi Hồng & Sả Chanh Thanh Lọc Không Khí",
                    "tinh-dau-buoi-hong-sa-chanh-thanh-loc",
                    "Hương thanh mát sảng khoái thanh lọc không khí xua tan mệt mỏi", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-TDBH-50ML", "Chai xông phòng 50ml", new BigDecimal("1350000"), new BigDecimal("165000"), 120, 60),
                            new VariantSeed("MH-TDBH-100ML", "Chai xông phòng 100ml", new BigDecimal("240000"), new BigDecimal("300000"), 220, 40)
                    ));

            // 10. Vòng Gỗ Bách Xanh Chìm Nước
            createProductWithVariants(c4, "Vòng Gỗ Bách Xanh Chìm Nước Mộc Châu Thơm Ngọt",
                    "vong-go-bach-xanh-chim-nuoc-moc-chau",
                    "Bách xanh chìm nước mùi thơm ngọt kẹo phong thủy vượng khí tài lộc", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-VGBX-12MM", "Vòng hạt tròn 12mm", new BigDecimal("380000"), new BigDecimal("450000"), 40, 30),
                            new VariantSeed("MH-VGBX-14MM", "Vòng hạt tròn 14mm", new BigDecimal("460000"), new BigDecimal("550000"), 55, 25)
                    ));

            // 11. Vòng Gỗ Tử Đàn Tiểu Diệp
            createProductWithVariants(c4, "Vòng Tay Gỗ Tử Đàn Tiểu Diệp Ấn Độ Phong Thủy Vượng Khí",
                    "vong-tay-go-tu-dan-tieu-diep-an-do",
                    "Vua của các loài gỗ mang lại bình an hanh thông vượng khí cho gia chủ", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-VGTD-10MM", "Vòng hạt 10mm (Nữ/Nam)", new BigDecimal("890000"), new BigDecimal("1100000"), 35, 20),
                            new VariantSeed("MH-VGTD-12MM", "Vòng hạt 12mm (Nam)", new BigDecimal("1250000"), new BigDecimal("1500000"), 50, 15)
                    ));

            // 12. Chuỗi Niệm Phật 108 Hạt Gỗ Mun Sừng
            createProductWithVariants(c5, "Chuỗi Niệm Phật 108 Hạt Gỗ Mun Sừng Đen Tuyền Thiền Định",
                    "chuoi-niem-phat-108-hat-go-mun-sung",
                    "Gỗ mun sừng tự nhiên đen tuyền bóng mượt chuỗi hạt trợ duyên hành trì thiền định", "Mộc Hương",
                    List.of(
                            new VariantSeed("MH-CH108-MUN-6MM", "Chuỗi 108 hạt size 6mm", new BigDecimal("580000"), new BigDecimal("700000"), 30, 25),
                            new VariantSeed("MH-CH108-MUN-8MM", "Chuỗi 108 hạt size 8mm", new BigDecimal("790000"), new BigDecimal("950000"), 45, 20)
                    ));

            // 2. Seed Đánh giá mẫu liên kết đúng với userId=3,4,5 và orderId=1,4
            seedReviews(p1, p2, p3, p6);

            log.info("✅ Đã khởi tạo hoàn tất 5 Danh mục, 12 Sản phẩm đa biến thể và các đánh giá mẫu cho Mộc Hương Shop!");
        }
    }

    private void seedReviews(Product p1, Product p2, Product p3, Product p6) {
        if (reviewRepository.count() == 0) {
            List<Review> reviews = new ArrayList<>();

            // Review 1: Khách hàng Nguyễn Minh Triết (userId=3) cho sản phẩm 1 từ Đơn 1
            reviews.add(Review.builder()
                    .product(p1)
                    .userId(3L)
                    .orderItemId(1L)
                    .rating(5)
                    .title("Hương trầm thơm ngọt sâu, thư thái tâm hồn")
                    .content("Nhang thơm dịu không bị khét khói, thắp trong phòng trà cảm giác rất thanh tịnh. Đóng gói hộp rất trang nhã.")
                    .adminReply("Mộc Hương chân thành cảm ơn quý khách. Kính chúc quý khách thân tâm thường an lạc!")
                    .adminRepliedAt(LocalDateTime.now().minusDays(1))
                    .isVisible(true)
                    .build());

            // Review 2: Khách hàng Trần Thanh Hà (userId=4) cho sản phẩm 1 từ Đơn 4
            reviews.add(Review.builder()
                    .product(p1)
                    .userId(4L)
                    .orderItemId(4L)
                    .rating(5)
                    .title("Lễ hộp Sơn Mài đem biếu người lớn rất ưng ý")
                    .content("Ống sơn mài làm rất tỉ mỉ, sang trọng. Hương trầm kiến 30 năm đốt lên thơm dịu khắp cả tầng phòng khách.")
                    .adminReply("Dạ Mộc Hương rất vui vì lễ hộp đã làm hài lòng quý gia đình ạ.")
                    .adminRepliedAt(LocalDateTime.now().minusDays(3))
                    .isVisible(true)
                    .build());

            // Review 3: Khách hàng Lê Hoàng Nam (userId=5) cho sản phẩm 2
            reviews.add(Review.builder()
                    .product(p2)
                    .userId(5L)
                    .rating(5)
                    .title("Hương vòng 24 giờ thơm êm ái")
                    .content("Vòng hương cháy đều và bền, khói tỏa nhẹ không cay mắt. Đã ủng hộ shop nhiều lần.")
                    .isVisible(true)
                    .build());

            // Review 4: Khách hàng Nguyễn Minh Triết (userId=3) cho sản phẩm 3 từ Đơn 1
            reviews.add(Review.builder()
                    .product(p3)
                    .userId(3L)
                    .orderItemId(2L)
                    .rating(5)
                    .title("Thác khói chảy đẹp như mây bồng bềnh")
                    .content("Khói trầm chảy ngược rất mịn, ngắm nhìn sau một ngày làm việc thấy nhẹ nhõm hẳn. Rất đáng tiền!")
                    .isVisible(true)
                    .build());

            // Review 5: Khách hàng Nguyễn Minh Triết (userId=3) cho sản phẩm 6 từ Đơn 2
            reviews.add(Review.builder()
                    .product(p6)
                    .userId(3L)
                    .rating(5)
                    .title("Vòng trầm tốc kiến vân dầu rất đẹp")
                    .content("Vòng thơm ngọt tự nhiên, đeo một thời gian lên nước bóng rất sang. Charm hoa sen tinh tế.")
                    .adminReply("Chúc quý khách luôn được năng lượng an lành từ chuỗi trầm hương thiên nhiên hộ trì ạ!")
                    .adminRepliedAt(LocalDateTime.now().minusDays(2))
                    .isVisible(true)
                    .build());

            reviewRepository.saveAll(reviews);
        }
    }

    private Product createProductWithVariants(Category cat, String name, String slug, String shortDesc, String brand, List<VariantSeed> variants) {
        Product p = Product.builder()
                .category(cat)
                .name(name)
                .slug(slug)
                .shortDescription(shortDesc)
                .description("<p>" + shortDesc + ". Tác phẩm thủ công tuyển chọn từ làng nghề truyền thống Mộc Hương Shop.</p>")
                .brand(brand)
                .tags(List.of("organic", "thu-cong", "thien-nhien", "tram-that"))
                .isPublished(true)
                .isActive(true)
                .averageRating(BigDecimal.valueOf(5.0))
                .totalReviews(variants.size() > 2 ? 2 : 1)
                .totalSold(18)
                .build();
        Product savedProduct = productRepository.save(p);

        // Ảnh mẫu chất lượng cao phân chia theo loại sản phẩm
        String img1;
        String img2;
        if (slug.contains("nhang")) {
            img1 = "https://images.unsplash.com/photo-1602928321679-560bb453f190?w=800";
            img2 = "https://images.unsplash.com/photo-1544816155-12df9643f363?w=800";
        } else if (slug.contains("nu") || slug.contains("vong-tram") || slug.contains("huong-vong")) {
            img1 = "https://images.unsplash.com/photo-1515377905703-c4788e51af15?w=800";
            img2 = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800";
        } else if (slug.contains("tinh-dau")) {
            img1 = "https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=800";
            img2 = "https://images.unsplash.com/photo-1617897903246-719242758050?w=800";
        } else if (slug.contains("vong-tay") || slug.contains("vong-go")) {
            img1 = "https://images.unsplash.com/photo-1611591437281-460bfbe1220a?w=800";
            img2 = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=800";
        } else {
            img1 = "https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?w=800";
            img2 = "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=800";
        }

        productImageRepository.save(ProductImage.builder()
                .product(savedProduct)
                .cloudinaryPublicId("seed-" + slug + "-1")
                .imageUrl(img1)
                .isPrimary(true)
                .sortOrder(0)
                .altText(name + " - Ảnh chính")
                .build());

        productImageRepository.save(ProductImage.builder()
                .product(savedProduct)
                .cloudinaryPublicId("seed-" + slug + "-2")
                .imageUrl(img2)
                .isPrimary(false)
                .sortOrder(1)
                .altText(name + " - Ảnh chi tiết")
                .build());

        int sort = 0;
        for (VariantSeed vs : variants) {
            ProductVariant v = ProductVariant.builder()
                    .product(savedProduct)
                    .sku(vs.sku)
                    .variantName(vs.name)
                    .price(vs.price)
                    .originalPrice(vs.originalPrice)
                    .weightGram(vs.weightGram)
                    .isActive(true)
                    .sortOrder(sort++)
                    .build();
            ProductVariant savedV = variantRepository.save(v);

            Inventory inv = Inventory.builder()
                    .variant(savedV)
                    .quantity(vs.stock)
                    .reservedQuantity(0)
                    .lowStockThreshold(10)
                    .build();
            inventoryRepository.save(inv);
        }

        return savedProduct;
    }

    private record VariantSeed(String sku, String name, BigDecimal price, BigDecimal originalPrice, Integer weightGram, Integer stock) {}
}
