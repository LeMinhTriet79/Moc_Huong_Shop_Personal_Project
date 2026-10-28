package com.minhtriet.se3979.catalogservice.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    @JsonAlias({"weightGrams"})
    private Integer weightGram;

    public Integer getWeightGrams() {
        return weightGram;
    }

    private Map<String, Object> attributes;

    private Boolean isActive;

    private Integer sortOrder;
}
