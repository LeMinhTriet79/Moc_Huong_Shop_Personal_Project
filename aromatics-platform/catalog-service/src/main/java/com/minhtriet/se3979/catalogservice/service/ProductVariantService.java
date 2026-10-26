package com.minhtriet.se3979.catalogservice.service;

import com.minhtriet.se3979.catalogservice.dto.request.VariantCreateRequest;
import com.minhtriet.se3979.catalogservice.dto.request.VariantUpdateRequest;
import com.minhtriet.se3979.catalogservice.dto.response.ProductVariantResponse;
import com.minhtriet.se3979.catalogservice.dto.response.VariantInternalInfoResponse;

import java.util.List;

public interface ProductVariantService {

    List<ProductVariantResponse> getVariantsByProductId(Long productId);

    ProductVariantResponse addVariant(Long productId, VariantCreateRequest request);

    ProductVariantResponse updateVariant(Long productId, Long variantId, VariantUpdateRequest request);

    void deleteVariant(Long productId, Long variantId);

    VariantInternalInfoResponse getVariantInternalInfo(Long variantId);

    List<VariantInternalInfoResponse> getBatchVariantInternalInfo(List<Long> variantIds);
}
