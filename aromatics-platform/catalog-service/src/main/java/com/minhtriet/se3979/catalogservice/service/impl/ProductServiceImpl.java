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
                .isActive(product.getIsActive()) // <--- ĐÃ THÊM CỜ BÁO HIỆU
                .build();
    }

    @Transactional
    @Override
    public Object createProductWithImages(ProductCreateRequest request, List<MultipartFile> files) {
        // 1. FAIL FAST (CHẾT TỪ VÒNG GỬI XE): Kiểm tra trùng SKU trước khi làm bất cứ việc gì
        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("Mã SKU '" + request.getSku() + "' đã tồn tại. Vui lòng chọn mã khác!");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        String slug = generateSlug(request.getName()) + "-" + System.currentTimeMillis();

        // 2. TẠO GIỎ RÁC TẠM THỜI: Lưu ID ảnh vừa up, lỡ DB lỗi thì lên mây xóa ngay
        List<String> uploadedCloudinaryIds = new ArrayList<>();

        try {
            Product product = Product.builder()
                    .category(category)
                    .name(request.getName())
                    .slug(slug)
                    .shortDescription(request.getShortDescription())
                    .description(request.getDescription())
                    .brand(request.getBrand())
                    .tags(request.getTags())
                    .isPublished(true)
                    .isActive(true)
                    .build();
            Product savedProduct = productRepository.save(product);

            if (files != null && !files.isEmpty()) {
                List<ProductImage> productImages = new ArrayList<>();
                for (int i = 0; i < files.size(); i++) {
                    Map<String, Object> uploadResult = cloudinaryService.uploadImage(files.get(i));
                    String publicId = uploadResult.get("public_id").toString();

                    // Thảy public_id vào giỏ rác tạm
                    uploadedCloudinaryIds.add(publicId);

                    ProductImage image = ProductImage.builder()
                            .product(savedProduct)
                            .cloudinaryPublicId(publicId)
                            .imageUrl(uploadResult.get("secure_url").toString())
                            .isPrimary(i == 0)
                            .sortOrder(i)
                            .build();
                    productImages.add(image);
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

        } catch (Exception e) {
            // 3. DỌN DẸP TÀN CUỘC (COMPENSATING TRANSACTION): Nếu đoạn code trên bị lỗi, tự động leo lên mây xóa rác
            if (!uploadedCloudinaryIds.isEmpty()) {
                for (String publicId : uploadedCloudinaryIds) {
                    try {
                        cloudinaryService.deleteImage(publicId);
                        System.out.println("Đã tự động dọn rác Cloudinary do quá trình lưu DB thất bại: " + publicId);
                    } catch (Exception ex) {
                        System.err.println("Lỗi dọn rác Cloudinary: " + ex.getMessage());
                    }
                }
            }
            // Báo lỗi ra ngoài cho Frontend biết
            throw new RuntimeException("Lỗi khi tạo sản phẩm: " + e.getMessage());
        }
    }

    // XÓA MỀM (SOFT DELETE) - Rất sạch sẽ và an toàn
    @Transactional
    @Override
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm cần xóa!"));

        if (!product.getIsActive()) {
            throw new RuntimeException("Sản phẩm này đã bị xóa (ẩn) từ trước rồi!");
        }

        product.setIsActive(false);
        productRepository.save(product);
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
            List<ProductImage> imagesToDelete = product.getImages().stream()
                    .filter(img -> request.getDeletedImageIds().contains(img.getId()))
                    .toList();

            for (ProductImage img : imagesToDelete) {
                try {
                    if (img.getCloudinaryPublicId() != null) {
                        cloudinaryService.deleteImage(img.getCloudinaryPublicId());
                    }
                    product.getImages().remove(img);
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

        productRepository.save(product);
        return "Cập nhật sản phẩm và thư viện ảnh thành công!";
    }

    // THÙNG RÁC
    @Override
    public Page<Object> getInactiveProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsActiveFalse(pageable);
        return products.map(this::mapToResponse).map(dto -> (Object) dto);
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
                .isActive(p.getIsActive()) // <--- ĐÃ THÊM CỜ BÁO HIỆU
                .build();
    }

    // KHÔI PHỤC SẢN PHẨM TỪ THÙNG RÁC
    @Transactional
    @Override
    public void restoreProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        if (product.getIsActive()) {
            throw new RuntimeException("Sản phẩm này vẫn đang hoạt động, không cần khôi phục!");
        }

        product.setIsActive(true);
        productRepository.save(product);
    }

    // XÓA VĨNH VIỄN (Chỉ được xóa khi sản phẩm đang nằm trong thùng rác)
    @Transactional
    @Override
    public void hardDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        if (product.getIsActive()) {
            throw new RuntimeException("Phải đưa sản phẩm vào thùng rác trước khi xóa vĩnh viễn!");
        }

        // 1. Dọn rác Cloudinary
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (ProductImage img : product.getImages()) {
                try {
                    if (img.getCloudinaryPublicId() != null) {
                        cloudinaryService.deleteImage(img.getCloudinaryPublicId());
                        System.out.println("Đã leo lên mây xóa ảnh!");
                        System.err.println("Đã leo lên mây xóa ảnh! AHIHI");
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi dọn rác Cloudinary: " + e.getMessage());
                }
            }
        }

        // 2. Xóa Tồn kho trước để tránh lỗi khóa ngoại
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            for (ProductVariant variant : product.getVariants()) {
                inventoryRepository.deleteByVariantId(variant.getId());
            }
        }

        // 3. Thiêu rụi dưới Database
        productRepository.delete(product);
    }

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