package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.request.VariantCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.request.VariantUpdateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ProductVariantResponse;
import com.minhtriet.se3979.catalogservice.dto.response.VariantInternalInfoResponse;
import com.minhtriet.se3979.catalogservice.entity.Inventory;
import com.minhtriet.se3979.catalogservice.entity.Product;
import com.minhtriet.se3979.catalogservice.entity.ProductImage;
import com.minhtriet.se3979.catalogservice.entity.ProductVariant;
import com.minhtriet.se3979.catalogservice.exception.AppException;
import com.minhtriet.se3979.catalogservice.repository.InventoryRepository;
import com.minhtriet.se3979.catalogservice.repository.ProductRepository;
import com.minhtriet.se3979.catalogservice.repository.ProductVariantRepository;
import com.minhtriet.se3979.catalogservice.service.ProductCacheService;
import com.minhtriet.se3979.catalogservice.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductCacheService productCacheService;

    @Override
    public List<ProductVariantResponse> getVariantsByProductId(Long productId) {
        return variantRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public ProductVariantResponse addVariant(Long productId, VariantCreateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với id: " + productId));

        if (variantRepository.existsBySku(request.getSku())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã SKU '" + request.getSku() + "' đã tồn tại!");
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.getSku())
                .variantName(request.getVariantName())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .weightGram(request.getWeightGram())
                .attributes(request.getAttributes())
                .isActive(true)
                .sortOrder(0)
                .build();

        ProductVariant savedVariant = variantRepository.save(variant);

        Inventory inventory = Inventory.builder()
                .variant(savedVariant)
                .quantity(request.getInitialStock() != null ? request.getInitialStock() : 0)
                .reservedQuantity(0)
                .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10)
                .build();

        inventoryRepository.save(inventory);

        // Xóa cache sản phẩm
        productCacheService.evictProduct(product.getSlug());

        log.info("Thêm biến thể mới thành công: variantId={}, sku={}, productId={}",
                savedVariant.getId(), savedVariant.getSku(), productId);

        return mapToResponse(savedVariant);
    }

    @Transactional
    @Override
    public ProductVariantResponse updateVariant(Long productId, Long variantId, VariantUpdateRequest request) {
        ProductVariant variant = variantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy biến thể id " + variantId + " thuộc sản phẩm id " + productId));

        if (request.getVariantName() != null && !request.getVariantName().isBlank()) {
            variant.setVariantName(request.getVariantName());
        }
        if (request.getPrice() != null) {
            variant.setPrice(request.getPrice());
        }
        if (request.getOriginalPrice() != null) {
            variant.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getWeightGram() != null) {
            variant.setWeightGram(request.getWeightGram());
        }
        if (request.getAttributes() != null) {
            variant.setAttributes(request.getAttributes());
        }
        if (request.getIsActive() != null) {
            variant.setIsActive(request.getIsActive());
        }
        if (request.getSortOrder() != null) {
            variant.setSortOrder(request.getSortOrder());
        }

        ProductVariant saved = variantRepository.save(variant);

        // Xóa cache sản phẩm
        if (variant.getProduct() != null) {
            productCacheService.evictProduct(variant.getProduct().getSlug());
        }

        log.info("Cập nhật biến thể thành công: variantId={}, productId={}", variantId, productId);
        return mapToResponse(saved);
    }

    @Transactional
    @Override
    public void deleteVariant(Long productId, Long variantId) {
        ProductVariant variant = variantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy biến thể id " + variantId + " thuộc sản phẩm id " + productId));

        variant.setIsActive(false);
        variantRepository.save(variant);

        // Xóa cache sản phẩm
        if (variant.getProduct() != null) {
            productCacheService.evictProduct(variant.getProduct().getSlug());
        }

        log.info("Đã ẩn biến thể (soft-delete): variantId={}, productId={}", variantId, productId);
    }

    @Override
    public VariantInternalInfoResponse getVariantInternalInfo(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy biến thể với id: " + variantId));

        return mapToInternalInfo(variant);
    }

    @Override
    public List<VariantInternalInfoResponse> getBatchVariantInternalInfo(List<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return List.of();
        }
        return variantRepository.findByIdIn(variantIds).stream()
                .map(this::mapToInternalInfo)
                .toList();
    }

    private ProductVariantResponse mapToResponse(ProductVariant v) {
        Integer stock = 0;
        Integer reserved = 0;
        if (v.getInventory() != null) {
            stock = v.getInventory().getQuantity();
            reserved = v.getInventory().getReservedQuantity();
        } else {
            var invOpt = inventoryRepository.findByVariantId(v.getId());
            if (invOpt.isPresent()) {
                stock = invOpt.get().getQuantity();
                reserved = invOpt.get().getReservedQuantity();
            }
        }
        int available = Math.max(0, stock - reserved);

        return ProductVariantResponse.builder()
                .id(v.getId())
                .productId(v.getProduct() != null ? v.getProduct().getId() : null)
                .sku(v.getSku())
                .variantName(v.getVariantName())
                .price(v.getPrice())
                .originalPrice(v.getOriginalPrice())
                .weightGram(v.getWeightGram())
                .attributes(v.getAttributes())
                .isActive(v.getIsActive())
                .sortOrder(v.getSortOrder())
                .stockQuantity(stock)
                .reservedQuantity(reserved)
                .availableStock(available)
                .createdAt(v.getCreatedAt())
                .build();
    }

    private VariantInternalInfoResponse mapToInternalInfo(ProductVariant v) {
        Integer stock = 0;
        Integer reserved = 0;
        if (v.getInventory() != null) {
            stock = v.getInventory().getQuantity();
            reserved = v.getInventory().getReservedQuantity();
        } else {
            var invOpt = inventoryRepository.findByVariantId(v.getId());
            if (invOpt.isPresent()) {
                stock = invOpt.get().getQuantity();
                reserved = invOpt.get().getReservedQuantity();
            }
        }
        int available = Math.max(0, stock - reserved);

        String primaryImageUrl = null;
        String productName = null;
        Long productId = null;

        if (v.getProduct() != null) {
            productId = v.getProduct().getId();
            productName = v.getProduct().getName();
            if (v.getProduct().getImages() != null) {
                primaryImageUrl = v.getProduct().getImages().stream()
                        .filter(ProductImage::getIsPrimary)
                        .map(ProductImage::getImageUrl)
                        .findFirst()
                        .orElse(null);
                if (primaryImageUrl == null && !v.getProduct().getImages().isEmpty()) {
                    primaryImageUrl = v.getProduct().getImages().get(0).getImageUrl();
                }
            }
        }

        return VariantInternalInfoResponse.builder()
                .productId(productId)
                .variantId(v.getId())
                .productName(productName)
                .variantName(v.getVariantName())
                .sku(v.getSku())
                .price(v.getPrice())
                .primaryImageUrl(primaryImageUrl)
                .availableStock(available)
                .isActive(v.getIsActive() && (v.getProduct() == null || v.getProduct().getIsActive()))
                .build();
    }
}
