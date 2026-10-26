package com.minhtriet.se3979.catalogservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhtriet.se3979.catalogservice.dto.request.ProductCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.request.ProductUpdateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ApiResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ProductDetailResponse;
import com.minhtriet.se3979.catalogservice.dto.response.ProductResponse;
import com.minhtriet.se3979.catalogservice.exception.AppException;
import com.minhtriet.se3979.catalogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    // Tìm kiếm sản phẩm theo nhiều tiêu chí
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Page<ProductResponse> products = productService.searchProducts(
                keyword, categoryId, brand, minPrice, maxPrice, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(products, "Lấy danh sách sản phẩm thành công"));
    }

    // Top sản phẩm nổi bật & bán chạy (Có Redis Cache)
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts() {
        List<ProductResponse> featured = productService.getFeaturedProducts();
        return ResponseEntity.ok(ApiResponse.success(featured, "Lấy danh sách sản phẩm nổi bật thành công"));
    }

    // Xem sản phẩm trong thùng rác (STAFF, ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getInactiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Page<ProductResponse> products = productService.getInactiveProducts(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(products, "Lấy danh sách sản phẩm trong thùng rác thành công"));
    }

    // Xem chi tiết sản phẩm theo slug (Có Redis Cache)
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable String slug) {
        ProductDetailResponse response = productService.getProductDetail(slug);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy chi tiết sản phẩm thành công"));
    }

    // Tạo sản phẩm mới kèm ảnh & biến thể (STAFF, ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> files) {
        try {
            ProductCreateRequest request = objectMapper.readValue(productJson, ProductCreateRequest.class);
            ProductDetailResponse response = productService.createProductWithImages(request, files);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Thêm sản phẩm mới thành công!"));
        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Lỗi định dạng JSON hoặc dữ liệu sản phẩm: " + e.getMessage());
        }
    }

    // Cập nhật sản phẩm (STAFF, ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Long id,
            @RequestPart(value = "product", required = false) String productJson,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) {
        try {
            ProductUpdateRequest request = null;
            if (productJson != null && !productJson.isBlank()) {
                request = objectMapper.readValue(productJson, ProductUpdateRequest.class);
            }
            ProductDetailResponse response = productService.updateProduct(id, request, newImages);
            return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật sản phẩm thành công!"));
        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Lỗi dữ liệu đầu vào: " + e.getMessage());
        }
    }

    // Đưa sản phẩm vào thùng rác - Xóa mềm (STAFF, ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("OK", "Đã đưa sản phẩm vào thùng rác thành công!"));
    }

    // Khôi phục sản phẩm từ thùng rác (STAFF, ADMIN)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PutMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<String>> restoreProduct(@PathVariable Long id) {
        productService.restoreProduct(id);
        return ResponseEntity.ok(ApiResponse.success("OK", "Đã khôi phục sản phẩm thành công!"));
    }

    // Xóa vĩnh viễn sản phẩm và dọn sạch Cloudinary (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/force")
    public ResponseEntity<ApiResponse<String>> hardDeleteProduct(@PathVariable Long id) {
        productService.hardDeleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("OK", "Đã xóa vĩnh viễn sản phẩm và dọn sạch ảnh trên Cloudinary!"));
    }
}