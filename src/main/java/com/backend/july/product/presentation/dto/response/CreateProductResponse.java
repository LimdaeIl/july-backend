package com.backend.july.product.presentation.dto.response;

import com.backend.july.product.domain.Product;
import java.math.BigDecimal;

public record CreateProductResponse(
        Long productId,
        String name,
        BigDecimal price,
        String status
) {

    public static CreateProductResponse from(Product product) {
        return new CreateProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStatus().name()
        );
    }
}
