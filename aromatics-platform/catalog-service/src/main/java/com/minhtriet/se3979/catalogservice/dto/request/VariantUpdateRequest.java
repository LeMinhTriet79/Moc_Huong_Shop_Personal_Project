package com.minhtriet.se3979.catalogservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantUpdateRequest {

    private String variantName;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá bán phải lớn hơn 0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer weightGram;

    private Map<String, Object> attributes;

    private Boolean isActive;

    private Integer sortOrder;
}
