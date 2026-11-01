package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.request.CategoryRequest;
import com.minhtriet.se3979.catalogservice.dto.response.CategoryResponse;
import com.minhtriet.se3979.catalogservice.entity.Category;
import com.minhtriet.se3979.catalogservice.exception.AppException;
import com.minhtriet.se3979.catalogservice.repository.CategoryRepository;
import com.minhtriet.se3979.catalogservice.service.CategoryService;
import com.minhtriet.se3979.catalogservice.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    // 1. LẤY TẤT CẢ (CHỈ LẤY ACTIVE)
    @Override
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // LẤY TẤT CẢ (CHỈ LẤY INACTIVE)
    @Override
    public List<CategoryResponse> getAllInactiveCategories() {
        return categoryRepository.findByIsActiveFalse().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 2. LẤY CHI TIẾT
    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục!"));
        return mapToResponse(category);
    }

    // 3. TẠO MỚI
    @Transactional
    @Override
    public CategoryResponse createCategory(CategoryRequest request, MultipartFile file) {
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục cha!"));
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(generateSlug(request.getName()))
                .parent(parent)
                .description(request.getDescription())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        if (file != null && !file.isEmpty()) {
            try {
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
                category.setImageUrl(uploadResult.get("secure_url").toString());
                category.setCloudinaryPublicId(uploadResult.get("public_id").toString());
            } catch (Exception e) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi upload ảnh danh mục lên mây: " + e.getMessage());
            }
        }

        return mapToResponse(categoryRepository.save(category));
    }

    // 4. CẬP NHẬT THÔNG MINH (PARTIAL UPDATE)
    @Transactional
    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request, MultipartFile file) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục!"));

        if (request != null) {
            if (request.getName() != null && !request.getName().isEmpty()) {
                category.setName(request.getName());
                category.setSlug(generateSlug(request.getName()));
            }
            if (request.getParentId() != null) {
                if (request.getParentId().equals(id)) {
                    throw new AppException(HttpStatus.BAD_REQUEST, "Danh mục không thể tự làm cha của chính nó!");
                }
                Category parent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục cha!"));
                category.setParent(parent);
            }
            if (request.getDescription() != null) {
                category.setDescription(request.getDescription());
            }
            if (request.getSortOrder() != null) {
                category.setSortOrder(request.getSortOrder());
            }
            if (request.getIsActive() != null) {
                category.setIsActive(request.getIsActive());
            }
        }

        if (file != null && !file.isEmpty()) {
            try {
                if (category.getCloudinaryPublicId() != null) {
                    cloudinaryService.deleteImage(category.getCloudinaryPublicId());
                }
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
                category.setImageUrl(uploadResult.get("secure_url").toString());
                category.setCloudinaryPublicId(uploadResult.get("public_id").toString());
            } catch (Exception e) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi cập nhật ảnh danh mục: " + e.getMessage());
            }
        }

        return mapToResponse(categoryRepository.save(category));
    }

    // 5. XÓA MỀM (SOFT DELETE)
    @Transactional
    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục!"));

        if (!Boolean.TRUE.equals(category.getIsActive())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Danh mục này đã bị ẩn (xóa) từ trước rồi!");
        }

        category.setIsActive(false);
        categoryRepository.save(category);
    }

    // HÀM TIỆN ÍCH MAPPER
    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .imageUrl(category.getImageUrl())
                .description(category.getDescription())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
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
