package com.minhtriet.se3979.catalogservice.service;

import java.util.List;

public interface CategoryService {
    // Tạm thời trả về Object, lát nữa chúng ta sẽ viết DTO thay thế
    List<Object> getAllActiveCategories();
}