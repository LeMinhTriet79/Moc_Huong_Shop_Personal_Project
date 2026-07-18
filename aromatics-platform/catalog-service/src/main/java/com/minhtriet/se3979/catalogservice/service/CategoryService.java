package com.minhtriet.se3979.catalogservice.service;

import com.minhtriet.se3979.catalogservice.dto.request.CategoryRequest;
import com.minhtriet.se3979.catalogservice.dto.response.CategoryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllActiveCategories();

    List<CategoryResponse> getAllInactiveCategories();

    // Đã bổ sung MultipartFile để nhận ảnh
    CategoryResponse createCategory(CategoryRequest request, MultipartFile file);

    // Đã bổ sung MultipartFile để nhận ảnh
    CategoryResponse updateCategory(Long id, CategoryRequest request, MultipartFile file);

    void deleteCategory(Long id);

    CategoryResponse getCategoryById(Long id);
}