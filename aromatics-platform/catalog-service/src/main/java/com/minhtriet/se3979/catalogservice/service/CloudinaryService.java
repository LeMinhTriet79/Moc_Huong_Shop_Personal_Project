package com.minhtriet.se3979.catalogservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Hàm upload 1 file ảnh
    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        // Tự động tạo thư mục "se3979_products" trên Cloudinary để khỏi lộn xộn
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "se3979_products"
        ));
    }

    // Hàm xóa ảnh trên Cloudinary (Dùng khi update hoặc delete sản phẩm)
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}