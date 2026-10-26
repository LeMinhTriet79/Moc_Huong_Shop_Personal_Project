package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.request.ProductCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.request.ProductUpdateRequest;
import com.minhtriet.se3979.catalogservice.dto.request.VariantCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ProductDetailResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ProductResponse;
import com.minhtriet.se3979.catalogservice.entity.*;
import com.minhtriet.se3979.catalogservice.exception.AppException;
import com.minhtriet.se3979.catalogservice.repository.*;
import com.minhtriet.se3979.catalogservice.service.CloudinaryService;
import com.minhtriet.se3979.catalogservice.service.ProductCacheService;
import com.minhtriet.se3979.catalogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductCacheService productCacheService;

    @Override
    public Page<ProductResponse> searchProducts(String keyword, Long categoryId, String brand, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.searchProducts(keyword, categoryId, brand, minPrice, maxPrice, pageable);
        return products.map(this::mapToResponse);
    }

    @Override
    public List<ProductResponse> getFeaturedProducts() {
        Optional<List<ProductResponse>> cached = productCacheService.getFeaturedProducts();
        if (cached.isPresent()) {
            return cached.get();
        }

        List<Product> topProducts = productRepository.findTopFeatured(PageRequest.of(0, 10));
        List<ProductResponse> response = topProducts.stream()
                .map(this::mapToResponse)
                .toList();

        productCacheService.cacheFeaturedProducts(response);
        return response;
    }

    @Override
    public ProductDetailResponse getProductDetail(String slug) {
        Optional<ProductDetailResponse> cached = productCacheService.getProductDetailBySlug(slug);
        if (cached.isPresent()) {
            return cached.get();
        }

        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với slug: " + slug));

        ProductDetailResponse detail = mapToDetailResponse(product);
        productCacheService.cacheProductDetail(slug, detail);
        return detail;
    }

    @Transactional
    @Override
    public ProductDetailResponse createProductWithImages(ProductCreateRequest request, List<MultipartFile> files) {
        // 1. Kiểm tra SKU (Fail Fast)
        boolean hasMultiVariants = request.getVariants() != null && !request.getVariants().isEmpty();
        if (hasMultiVariants) {
            Set<String> skuSet = new HashSet<>();
            for (VariantCreateRequest vReq : request.getVariants()) {
                if (!skuSet.add(vReq.getSku())) {
                    throw new AppException(HttpStatus.BAD_REQUEST, "Danh sách biến thể có mã SKU trùng nhau: " + vReq.getSku());
                }
                if (productVariantRepository.existsBySku(vReq.getSku())) {
                    throw new AppException(HttpStatus.BAD_REQUEST, "Mã SKU '" + vReq.getSku() + "' đã tồn tại!");
                }
            }
        } else {
            if (request.getSku() == null || request.getSku().isBlank()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập mã SKU hoặc danh sách biến thể!");
            }
            if (productVariantRepository.existsBySku(request.getSku())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Mã SKU '" + request.getSku() + "' đã tồn tại!");
            }
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục!"));

        String slug = generateSlug(request.getName()) + "-" + System.currentTimeMillis();

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

            // Upload ảnh lên Cloudinary
            if (files != null && !files.isEmpty()) {
                List<ProductImage> productImages = new ArrayList<>();
                for (int i = 0; i < files.size(); i++) {
                    if (files.get(i).isEmpty()) continue;
                    Map<String, Object> uploadResult = cloudinaryService.uploadImage(files.get(i));
                    String publicId = uploadResult.get("public_id").toString();
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
                savedProduct.setImages(productImages);
            }

            // Tạo các biến thể & tồn kho
            List<ProductVariant> savedVariants = new ArrayList<>();
            if (hasMultiVariants) {
                int sortOrder = 0;
                for (VariantCreateRequest vReq : request.getVariants()) {
                    ProductVariant variant = ProductVariant.builder()
                            .product(savedProduct)
                            .sku(vReq.getSku())
                            .variantName(vReq.getVariantName())
                            .price(vReq.getPrice())
                            .originalPrice(vReq.getOriginalPrice())
                            .weightGram(vReq.getWeightGram())
                            .attributes(vReq.getAttributes())
                            .isActive(true)
                            .sortOrder(sortOrder++)
                            .build();
                    ProductVariant savedVar = productVariantRepository.save(variant);

                    Inventory inventory = Inventory.builder()
                            .variant(savedVar)
                            .quantity(vReq.getInitialStock() != null ? vReq.getInitialStock() : 0)
                            .reservedQuantity(0)
                            .lowStockThreshold(vReq.getLowStockThreshold() != null ? vReq.getLowStockThreshold() : 10)
                            .build();
                    inventoryRepository.save(inventory);
                    savedVar.setInventory(inventory);
                    savedVariants.add(savedVar);
                }
            } else {
                ProductVariant variant = ProductVariant.builder()
                        .product(savedProduct)
                        .sku(request.getSku())
                        .variantName("Default")
                        .price(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO)
                        .isActive(true)
                        .sortOrder(0)
                        .build();
                ProductVariant savedVar = productVariantRepository.save(variant);

                Inventory inventory = Inventory.builder()
                        .variant(savedVar)
                        .quantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                        .reservedQuantity(0)
                        .lowStockThreshold(10)
                        .build();
                inventoryRepository.save(inventory);
                savedVar.setInventory(inventory);
                savedVariants.add(savedVar);
            }
            savedProduct.setVariants(savedVariants);

            // Xóa cache danh sách sản phẩm nổi bật
            productCacheService.evictProduct(slug);

            return mapToDetailResponse(savedProduct);

        } catch (Exception e) {
            // Compensating Transaction
            if (!uploadedCloudinaryIds.isEmpty()) {
                for (String publicId : uploadedCloudinaryIds) {
                    try {
                        cloudinaryService.deleteImage(publicId);
                    } catch (Exception ex) {
                        log.error("Lỗi dọn rác Cloudinary: {}", ex.getMessage());
                    }
                }
            }
            if (e instanceof AppException) throw (AppException) e;
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi tạo sản phẩm: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public ProductDetailResponse updateProduct(Long productId, ProductUpdateRequest request, List<MultipartFile> newImages) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm id: " + productId));

        String oldSlug = product.getSlug();

        if (request != null) {
            if (request.getName() != null && !request.getName().isBlank()) {
                product.setName(request.getName());
                product.setSlug(generateSlug(request.getName()) + "-" + System.currentTimeMillis());
            }
            if (request.getCategoryId() != null) {
                Category category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục!"));
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

        // Xóa ảnh cũ
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
                    log.error("Lỗi xóa ảnh cũ trên Cloudinary: {}", e.getMessage());
                }
            }
        }

        // Thêm ảnh mới
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
                    throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi upload ảnh mới lên mây: " + e.getMessage());
                }
            }
        }

        Product saved = productRepository.save(product);

        // Evict cache cả slug cũ và slug mới
        productCacheService.evictProduct(oldSlug);
        productCacheService.evictProduct(saved.getSlug());

        return mapToDetailResponse(saved);
    }

    @Transactional
    @Override
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm cần xóa!"));

        if (!product.getIsActive()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Sản phẩm này đã bị xóa (ẩn) từ trước rồi!");
        }

        product.setIsActive(false);
        productRepository.save(product);

        productCacheService.evictProduct(product.getSlug());
    }

    @Transactional
    @Override
    public void restoreProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm!"));

        if (product.getIsActive()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Sản phẩm này vẫn đang hoạt động, không cần khôi phục!");
        }

        product.setIsActive(true);
        productRepository.save(product);

        productCacheService.evictProduct(product.getSlug());
    }

    @Transactional
    @Override
    public void hardDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm!"));

        if (product.getIsActive()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Phải đưa sản phẩm vào thùng rác trước khi xóa vĩnh viễn!");
        }

        // Xóa ảnh Cloudinary
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (ProductImage img : product.getImages()) {
                try {
                    if (img.getCloudinaryPublicId() != null) {
                        cloudinaryService.deleteImage(img.getCloudinaryPublicId());
                    }
                } catch (Exception e) {
                    log.error("Lỗi dọn rác Cloudinary: {}", e.getMessage());
                }
            }
        }

        // Xóa tồn kho
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            for (ProductVariant variant : product.getVariants()) {
                inventoryRepository.deleteByVariantId(variant.getId());
            }
        }

        productRepository.delete(product);

        productCacheService.evictProduct(product.getSlug());
    }

    @Override
    public Page<ProductResponse> getInactiveProducts(Pageable pageable) {
        return productRepository.findByIsActiveFalse(pageable).map(this::mapToResponse);
    }

    private ProductResponse mapToResponse(Product p) {
        String primaryImage = null;
        if (p.getImages() != null) {
            primaryImage = p.getImages().stream()
                    .filter(ProductImage::getIsPrimary)
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(null);
            if (primaryImage == null && !p.getImages().isEmpty()) {
                primaryImage = p.getImages().get(0).getImageUrl();
            }
        }

        BigDecimal startPrice = BigDecimal.ZERO;
        if (p.getVariants() != null && !p.getVariants().isEmpty()) {
            startPrice = p.getVariants().stream()
                    .map(ProductVariant::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        }

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
                .isActive(p.getIsActive())
                .build();
    }

    private ProductDetailResponse mapToDetailResponse(Product product) {
        List<ProductDetailResponse.ImageDto> imageDtos = product.getImages() != null
                ? product.getImages().stream()
                .map(img -> ProductDetailResponse.ImageDto.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .isPrimary(img.getIsPrimary())
                        .build())
                .toList()
                : List.of();

        List<ProductDetailResponse.VariantDto> variantDtos = product.getVariants() != null
                ? product.getVariants().stream()
                .map(var -> {
                    Integer stock = 0;
                    Integer reserved = 0;
                    if (var.getInventory() != null) {
                        stock = var.getInventory().getQuantity();
                        reserved = var.getInventory().getReservedQuantity();
                    } else {
                        var invOpt = inventoryRepository.findByVariantId(var.getId());
                        if (invOpt.isPresent()) {
                            stock = invOpt.get().getQuantity();
                            reserved = invOpt.get().getReservedQuantity();
                        }
                    }
                    int available = Math.max(0, stock - reserved);

                    return ProductDetailResponse.VariantDto.builder()
                            .id(var.getId())
                            .sku(var.getSku())
                            .variantName(var.getVariantName())
                            .price(var.getPrice())
                            .originalPrice(var.getOriginalPrice())
                            .weightGram(var.getWeightGram())
                            .attributes(var.getAttributes())
                            .isActive(var.getIsActive())
                            .stockQuantity(stock)
                            .reservedQuantity(reserved)
                            .availableStock(available)
                            .build();
                })
                .toList()
                : List.of();

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
                .isActive(product.getIsActive())
                .build();
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