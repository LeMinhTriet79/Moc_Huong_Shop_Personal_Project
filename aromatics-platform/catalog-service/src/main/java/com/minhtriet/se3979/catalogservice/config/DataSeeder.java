package com.minhtriet.se3979.catalogservice.config;

import com.minhtriet.se3979.catalogservice.entity.Category;
import com.minhtriet.se3979.catalogservice.entity.Inventory;
import com.minhtriet.se3979.catalogservice.entity.Product;
import com.minhtriet.se3979.catalogservice.entity.ProductVariant;
import com.minhtriet.se3979.catalogservice.repository.CategoryRepository;
import com.minhtriet.se3979.catalogservice.repository.InventoryRepository;
import com.minhtriet.se3979.catalogservice.repository.ProductRepository;
import com.minhtriet.se3979.catalogservice.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;

    @Override
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

            log.info("📦 Đang khởi tạo 10 Sản phẩm mẫu kèm đa biến thể và kho hàng...");

            // 1. Nhang Trầm Kiến
            createProductWithVariants(c1, "Nhang Trầm Hương Kiến Tự Nhiên", "nhang-tram-huong-kien",
                    "Nhang trầm tự nhiên không tăm khói thanh nhẹ", "Mộc Hương",
                    List.of(
                            new VariantSeed("SKU-NHANG-40", "Hộp 40 nén (20cm)", new BigDecimal("180000"), new BigDecimal("220000"), 100, 50),
                            new VariantSeed("SKU-NHANG-100", "Hộp 100 nén (30cm)", new BigDecimal("380000"), new BigDecimal("450000"), 250, 40)
                    ));

            // 2. Nhang Tiên Phước
            createProductWithVariants(c1, "Nhang Trầm Tiên Phước Thượng Hạng", "nhang-tram-tien-phuoc",
                    "Trầm hương già năm vùng Tiên Phước Quảng Nam", "Mộc Hương",
                    List.of(
                            new VariantSeed("SKU-TP-50", "Hộp 50 nén cao cấp", new BigDecimal("320000"), new BigDecimal("380000"), 120, 30),
                            new VariantSeed("SKU-TP-100", "Hộp 100 nén cao cấp", new BigDecimal("590000"), new BigDecimal("700000"), 250, 25)
                    ));

            // 3. Trầm Nụ Thác Khói
            createProductWithVariants(c2, "Trầm Nụ Thác Khói Cao Cấp", "tram-nu-thac-khoi",
                    "Nụ trầm hương khói chảy ngược bồng bềnh như mây", "Mộc Hương",
                    List.of(
                            new VariantSeed("SKU-NU-30", "Hộp 30 viên khói ngược", new BigDecimal("150000"), new BigDecimal("180000"), 80, 60),
                            new VariantSeed("SKU-NU-60", "Hộp 60 viên tiết kiệm", new BigDecimal("270000"), new BigDecimal("320000"), 160, 45)
                    ));

            // 4. Hương Vòng Thư Giãn
            createProductWithVariants(c2, "Hương Vòng Trầm Hương Thư Giãn 24H", "huong-vong-tram-huong-24h",
                    "Hương vòng cháy liên tục 24 giờ thơm ấm cúng", "Mộc Hương",
                    List.of(
                            new VariantSeed("SKU-VONG-10", "Hộp 10 khoanh lớn", new BigDecimal("220000"), new BigDecimal("260000"), 200, 35),
                            new VariantSeed("SKU-VONG-20", "Hộp 20 khoanh lớn", new BigDecimal("390000"), new BigDecimal("480000"), 400, 20)
                    ));

            // 5. Tinh Dầu Trầm Hương
            createProductWithVariants(c3, "Tinh Dầu Trầm Hương Thiên Nhiên", "tinh-dau-tram-huong-nguyen-chat",
                    "Tinh dầu chưng cất hơi nước từ trầm hương nguyên chất", "Mộc Hương",
                    List.of(
                            new VariantSeed("SKU-TD-TRAM-10ML", "Chai nhỏ giọt 10ml", new BigDecimal("450000"), new BigDecimal("550000"), 50, 25),
                            new VariantSeed("SKU-TD-TRAM-30ML", "Chai nhỏ giọt 30ml", new BigDecimal("1150000"), new BigDecimal("1400000"), 100, 15)
                    ));

            // 6. Tinh Dầu Đàn Hương
            createProductWithVariants(c3, "Tinh Dầu Đàn Hương Ấn Độ (Sandalwood)", "tinh-dau-dan-huong-an-do",
                    "Hương gỗ ấm ngọt nồng nàn thanh tịnh tâm hồn", "Mộc Hương",
                    List.of(
                            new VariantSeed("SKU-TD-DAN-10ML", "Chai nhỏ giọt 10ml", new BigDecimal("380000"), new BigDecimal("450000"), 50, 30),
                            new VariantSeed("SKU-TD-DAN-30ML", "Chai nhỏ giọt 30ml", new BigDecimal("980000"), new BigDecimal("1200000"), 100, 20)
                    ));

            // 7. Tinh Dầu Bưởi Hồng & Sả Chanh
            createProductWithVariants(c3, "Tinh Dầu Bưởi Hồng & Sả Chanh Xông Phòng", "tinh-dau-buoi-hong-sa-chanh",
                    "Thư giãn tinh thần, thanh lọc không khí xua đuổi côn trùng", "Mộc Hương",
                    List.of(
                            new VariantSeed("SKU-TD-SA-50ML", "Chai xông phòng 50ml", new BigDecimal("120000"), new BigDecimal("150000"), 120, 50),
                            new VariantSeed("SKU-TD-SA-100ML", "Chai xông phòng 100ml", new BigDecimal("210000"), new BigDecimal("260000"), 220, 40)
                    ));

            // 8. Vòng Tay Trầm Hương 108 Hạt
            createProductWithVariants(c4, "Vòng Tay Trầm Hương Tốc Kiến 108 Hạt", "vong-tay-tram-huong-toc-kien-108",
                    "Vòng trầm tốc kiến vân dầu tự nhiên thơm vĩnh viễn", "Mộc Hương",
                    List.of(
                            new VariantSeed("SKU-VT-108-6MM", "Chuỗi 108 hạt size 6mm", new BigDecimal("650000"), new BigDecimal("850000"), 30, 20),
                            new VariantSeed("SKU-VT-108-8MM", "Chuỗi 108 hạt size 8mm", new BigDecimal("950000"), new BigDecimal("1200000"), 45, 15)
                    ));

            // 9. Vòng Gỗ Bách Xanh Chìm Nước
            createProductWithVariants(c4, "Vòng Gỗ Bách Xanh Chìm Nước Mộc Châu", "vong-go-bach-xanh-chim-nuoc",
                    "Bách xanh chìm nước mùi thơm ngọt kẹo phong thủy vượng khí", "Mộc Hương",
                    List.of(
                            new VariantSeed("SKU-BX-12MM", "Vòng hạt tròn 12mm (Nam/Nữ)", new BigDecimal("350000"), new BigDecimal("450000"), 40, 25),
                            new VariantSeed("SKU-BX-14MM", "Vòng hạt tròn 14mm (Nam)", new BigDecimal("420000"), new BigDecimal("550000"), 55, 20)
                    ));

            // 10. Lư Xông Trầm Gốm Sứ Men Hỏa Biến
            createProductWithVariants(c5, "Lư Xông Trầm Hương Men Hỏa Biến Nghệ Thuật", "lu-xong-tram-men-hoa-bien",
                    "Lư gốm sứ Bát Tràng men hỏa biến nắp đồng xông nụ xông vòng", "Mộc Hương",
                    List.of(
                            new VariantSeed("SKU-LU-XANH", "Màu Xanh Cổ Bích", new BigDecimal("240000"), new BigDecimal("300000"), 350, 30),
                            new VariantSeed("SKU-LU-NAU", "Màu Nâu Trầm Đất", new BigDecimal("240000"), new BigDecimal("300000"), 350, 30)
                    ));

            log.info("✅ Đã khởi tạo hoàn tất 5 Danh mục và 10 Sản phẩm đa biến thể cho Mộc Hương Shop!");
        }
    }

    private void createProductWithVariants(Category cat, String name, String slug, String shortDesc, String brand, List<VariantSeed> variants) {
        Product p = Product.builder()
                .category(cat)
                .name(name)
                .slug(slug + "-" + System.currentTimeMillis())
                .shortDescription(shortDesc)
                .description("<p>" + shortDesc + ". Sản phẩm tuyển chọn thủ công từ Mộc Hương Shop.</p>")
                .brand(brand)
                .tags(List.of("organic", "thu-cong", "thien-nhien"))
                .isPublished(true)
                .isActive(true)
                .averageRating(BigDecimal.valueOf(5.0))
                .totalReviews(1)
                .totalSold(12)
                .build();
        Product savedProduct = productRepository.save(p);

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
    }

    private record VariantSeed(String sku, String name, BigDecimal price, BigDecimal originalPrice, Integer weightGram, Integer stock) {}
}
