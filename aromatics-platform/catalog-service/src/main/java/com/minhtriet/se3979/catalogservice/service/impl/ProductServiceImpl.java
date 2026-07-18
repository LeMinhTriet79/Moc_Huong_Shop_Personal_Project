package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.request.ProductCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ProductDetailResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ProductResponse;
import com.minhtriet.se3979.catalogservice.entity.*;
import com.minhtriet.se3979.catalogservice.repository.*;
import com.minhtriet.se3979.catalogservice.service.CloudinaryService;
import com.minhtriet.se3979.catalogservice.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;
    @Override
    public Page<Object> searchProducts(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.searchProducts(keyword, categoryId, minPrice, maxPrice, pageable);

        // Map Entity sang DTO
        return products.map(this::mapToResponse).map(dto -> (Object) dto);
    }

    @Override
    public Object getProductDetail(String slug) {
        // 1. Tìm sản phẩm theo Slug
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với slug: " + slug));

        // 2. Chuyển đổi danh sách Hình ảnh
        List<ProductDetailResponse.ImageDto> imageDtos = product.getImages().stream()
                .map(img -> ProductDetailResponse.ImageDto.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .isPrimary(img.getIsPrimary())
                        .build())
                .toList();

        // 3. Chuyển đổi danh sách Phiên bản và lôi số lượng từ kho ra
        List<ProductDetailResponse.VariantDto> variantDtos = product.getVariants().stream()
                .map(var -> {
                    // Lấy số lượng tồn kho thực tế từ bảng Inventory
                    Integer stock = inventoryRepository.findByVariantId(var.getId())
                            .map(com.minhtriet.se3979.catalogservice.entity.Inventory::getQuantity)
                            .orElse(0);

                    return ProductDetailResponse.VariantDto.builder()
                            .id(var.getId())
                            .sku(var.getSku())
                            .variantName(var.getVariantName())
                            .price(var.getPrice())
                            .stockQuantity(stock)
                            .build();
                })
                .toList();

        // 4. Lắp ráp thành rổ Data khổng lồ trả về
        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .brand(product.getBrand())
                .tags(product.getTags())
                .averageRating(product.getAverageRating())
                .totalReviews(product.getTotalReviews())
                .totalSold(product.getTotalSold())
                .images(imageDtos)
                .variants(variantDtos)
                .build();
    }

    @Transactional
    @Override
    public Object createProductWithImages(ProductCreateRequest request, List<MultipartFile> files) {

        // 1. TÌM CATEGORY & LƯU PRODUCT (Như code cũ bạn đang chạy)
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        String slug = request.getName().toLowerCase().replaceAll("[^a-z0-9\\-]", "-") + "-" + System.currentTimeMillis();

        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .slug(slug)
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .brand(request.getBrand())
                .tags(request.getTags())
                .isPublished(true)
                .build();
        Product savedProduct = productRepository.save(product);

        // 2. UPLOAD ẢNH LÊN CLOUDINARY (Như code cũ)
        if (files != null && !files.isEmpty()) {
            List<ProductImage> productImages = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                try {
                    Map<String, Object> uploadResult = cloudinaryService.uploadImage(files.get(i));
                    ProductImage image = ProductImage.builder()
                            .product(savedProduct)
                            .cloudinaryPublicId(uploadResult.get("public_id").toString())
                            .imageUrl(uploadResult.get("secure_url").toString())
                            .isPrimary(i == 0)
                            .sortOrder(i)
                            .build();
                    productImages.add(image);
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi upload ảnh", e);
                }
            }
            productImageRepository.saveAll(productImages);
        }

        // 3. (MỚI) TẠO PHIÊN BẢN MẶC ĐỊNH CHO SẢN PHẨM
        ProductVariant variant = ProductVariant.builder()
                .product(savedProduct)
                .sku(request.getSku())
                .variantName("Default") // Tên mặc định
                .price(request.getPrice())
                .isActive(true)
                .build();
        ProductVariant savedVariant = productVariantRepository.save(variant);

        // 4. (MỚI) ĐỔ HÀNG VÀO KHO CHO PHIÊN BẢN ĐÓ
        Inventory inventory = Inventory.builder()
                .variant(savedVariant)
                .quantity(request.getStockQuantity())
                .build();
        inventoryRepository.save(inventory);

        return "Thêm sản phẩm, gắn ảnh và tạo kho thành công!";
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm cần xóa!"));

        // 1. Dọn rác Cloudinary
        List<ProductImage> images = product.getImages();
        if (images != null && !images.isEmpty()) {
            for (ProductImage img : images) {
                try {
                    if (img.getCloudinaryPublicId() != null) {
                        cloudinaryService.deleteImage(img.getCloudinaryPublicId());
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi dọn rác Cloudinary: " + e.getMessage());
                }
            }
        }

        // 2. (BỔ SUNG) Xóa Tồn kho (Inventory) trước để tránh lỗi Khóa ngoại
        List<ProductVariant> variants = product.getVariants();
        if (variants != null && !variants.isEmpty()) {
            for (ProductVariant variant : variants) {
                inventoryRepository.deleteByVariantId(variant.getId());
            }
        }

        // 3. Bây giờ mới an toàn để xóa Product (Sẽ tự động kéo theo Variant và Image)
        productRepository.delete(product);
    }

    // Hàm tiện ích chuyển Entity sang DTO
    private ProductResponse mapToResponse(Product p) {
        String primaryImage = p.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null);

        BigDecimal startPrice = p.getVariants().stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .shortDescription(p.getShortDescription())
                .brand(p.getBrand())
                .averageRating(p.getAverageRating())
                .totalReviews(p.getTotalReviews())
                .totalSold(p.getTotalSold())
                .primaryImageUrl(primaryImage)
                .startingPrice(startPrice)
                .build();
    }
}