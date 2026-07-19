package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.request.ProductCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.request.ProductUpdateRequest;
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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
        return products.map(this::mapToResponse).map(dto -> (Object) dto);
    }

    @Override
    public Object getProductDetail(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với slug: " + slug));

        List<ProductDetailResponse.ImageDto> imageDtos = product.getImages().stream()
                .map(img -> ProductDetailResponse.ImageDto.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .isPrimary(img.getIsPrimary())
                        .build())
                .toList();

        List<ProductDetailResponse.VariantDto> variantDtos = product.getVariants().stream()
                .map(var -> {
                    Integer stock = inventoryRepository.findByVariantId(var.getId())
                            .map(Inventory::getQuantity)
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
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        String slug = generateSlug(request.getName()) + "-" + System.currentTimeMillis();

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

        ProductVariant variant = ProductVariant.builder()
                .product(savedProduct)
                .sku(request.getSku())
                .variantName("Default")
                .price(request.getPrice())
                .isActive(true)
                .build();
        ProductVariant savedVariant = productVariantRepository.save(variant);

        Inventory inventory = Inventory.builder()
                .variant(savedVariant)
                .quantity(request.getStockQuantity())
                .build();
        inventoryRepository.save(inventory);

        return "Thêm sản phẩm, gắn ảnh và tạo kho thành công!";
    }

    @Transactional
    @Override
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm cần xóa!"));

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

        List<ProductVariant> variants = product.getVariants();
        if (variants != null && !variants.isEmpty()) {
            for (ProductVariant variant : variants) {
                inventoryRepository.deleteByVariantId(variant.getId());
            }
        }

        productRepository.delete(product);
    }

    @Transactional
    @Override
    public Object updateProduct(Long productId, ProductUpdateRequest request, List<MultipartFile> newImages) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        if (request != null) {
            if (request.getName() != null && !request.getName().isEmpty()) {
                product.setName(request.getName());
                product.setSlug(generateSlug(request.getName()) + "-" + System.currentTimeMillis());
            }
            if (request.getCategoryId() != null) {
                Category category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
                product.setCategory(category);
            }
            if (request.getShortDescription() != null) product.setShortDescription(request.getShortDescription());
            if (request.getDescription() != null) product.setDescription(request.getDescription());
            if (request.getBrand() != null) product.setBrand(request.getBrand());
            if (request.getIsPublished() != null) product.setIsPublished(request.getIsPublished());

            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                ProductVariant defaultVariant = product.getVariants().get(0);
                if (request.getPrice() != null) defaultVariant.setPrice(request.getPrice());
                if (request.getQuantity() != null && defaultVariant.getInventory() != null) {
                    defaultVariant.getInventory().setQuantity(request.getQuantity());
                }
            }
        }

        if (request != null && request.getDeletedImageIds() != null && !request.getDeletedImageIds().isEmpty()) {
            List<ProductImage> imagesToDelete = productImageRepository.findAllById(request.getDeletedImageIds());
            for (ProductImage img : imagesToDelete) {
                try {
                    cloudinaryService.deleteImage(img.getCloudinaryPublicId());
                    productImageRepository.delete(img);
                } catch (Exception e) {
                    System.err.println("Lỗi xóa ảnh cũ: " + e.getMessage());
                }
            }
        }

        if (newImages != null && !newImages.isEmpty()) {
            int currentMaxSortOrder = product.getImages().stream()
                    .mapToInt(ProductImage::getSortOrder)
                    .max().orElse(0);

            for (MultipartFile file : newImages) {
                if (file.isEmpty()) continue;
                try {
                    Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
                    currentMaxSortOrder++;

                    ProductImage newImage = ProductImage.builder()
                            .product(product)
                            .cloudinaryPublicId(uploadResult.get("public_id").toString())
                            .imageUrl(uploadResult.get("secure_url").toString())
                            .isPrimary(false)
                            .sortOrder(currentMaxSortOrder)
                            .build();
                    product.getImages().add(newImage);
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi upload ảnh mới lên mây!", e);
                }
            }
        }

        return productRepository.save(product);
    }

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

    // Hàm tiện ích chuẩn hóa Slug tiếng Việt
    private String generateSlug(String input) {
        if (input == null || input.isEmpty()) return "";
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(temp).replaceAll("");
        slug = slug.replaceAll("Đ", "D").replaceAll("đ", "d");
        slug = slug.toLowerCase().replaceAll("[^a-z0-9\\-]", "-");
        slug = slug.replaceAll("-+", "-");
        return slug.replaceAll("^-|-$", "");
    }
}