package com.minhtriet.se3979.catalogservice.service.impl;

import com.minhtriet.se3979.catalogservice.dto.request.CategoryRequest;
import com.minhtriet.se3979.catalogservice.dto.response.CategoryResponse;
import com.minhtriet.se3979.catalogservice.entity.Category;
import com.minhtriet.se3979.catalogservice.repository.CategoryRepository;
import com.minhtriet.se3979.catalogservice.service.CategoryService;
import com.minhtriet.se3979.catalogservice.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
        return mapToResponse(category);
    }

    // 3. TẠO MỚI
    // 3. TẠO MỚI
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, MultipartFile file) {
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha!"));
        }

        Category category = Category.builder()
                .name(request.getName())
                // SỬA Ở ĐÂY: Dùng luôn hàm generateSlug ngay lúc khởi tạo
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
                // (ĐÃ XÓA DÒNG SET SLUG BỊ ĐẶT SAI CHỖ Ở ĐÂY)
            } catch (Exception e) {
                throw new RuntimeException("Lỗi upload ảnh danh mục lên mây!", e);
            }
        }

        return mapToResponse(categoryRepository.save(category));
    }

    // 4. CẬP NHẬT THÔNG MINH (PARTIAL UPDATE)
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request, MultipartFile file) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        // Nếu có truyền JSON lên thì mới sửa chữ
        if (request != null) {
            if (request.getName() != null && !request.getName().isEmpty()) {
                category.setName(request.getName());
                category.setSlug(generateSlug(request.getName()));
            }
            if (request.getParentId() != null) {
                if (request.getParentId().equals(id)) {
                    throw new RuntimeException("Danh mục không thể tự làm cha của chính nó!");
                }
                Category parent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha!"));
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

        // Nếu có up ảnh mới thì xóa ảnh cũ trên mây và lưu ảnh mới
        if (file != null && !file.isEmpty()) {
            try {
                if (category.getCloudinaryPublicId() != null) {
                    cloudinaryService.deleteImage(category.getCloudinaryPublicId());
                }
                Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
                category.setImageUrl(uploadResult.get("secure_url").toString());
                category.setCloudinaryPublicId(uploadResult.get("public_id").toString());
            } catch (Exception e) {
                throw new RuntimeException("Lỗi cập nhật ảnh danh mục!", e);
            }
        }

        return mapToResponse(categoryRepository.save(category));
    }

    // 5. XÓA MỀM (SOFT DELETE)
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        if (!category.getIsActive()) {
            throw new RuntimeException("Danh mục này đã bị ẩn (xóa) từ trước rồi!");
        }

        // Chỉ đổi trạng thái, giữ nguyên dữ liệu và ảnh để phục vụ khôi phục
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

        // 1. Lột dấu tiếng Việt (Ví dụ: "Nhang Trầm" -> "Nhang Tram")
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(temp).replaceAll("");

        // 2. Chữ "Đ/đ" trong tiếng Việt rất lì lợm, phải replace tay
        slug = slug.replaceAll("Đ", "D").replaceAll("đ", "d");

        // 3. Viết thường, thay khoảng trắng bằng dấu gạch ngang, xóa ký tự đặc biệt
        slug = slug.toLowerCase().replaceAll("[^a-z0-9\\-]", "-");

        // 4. Xóa các dấu gạch ngang bị trùng nhau (Ví dụ: "nhang---tram" -> "nhang-tram")
        slug = slug.replaceAll("-+", "-");

        // 5. Xóa dấu gạch ngang ở đầu và cuối chuỗi nếu có
        return slug.replaceAll("^-|-$", "");
    }
}