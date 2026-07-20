package com.backend.july.product.presentation.dto.response;

import com.backend.july.product.domain.Product;
import com.backend.july.product.domain.ProductStatus;
import java.math.BigDecimal;

public record UpdateProductResponse(
        Long productId,
        String name,
        BigDecimal price
) {

    public static UpdateProductResponse from(Product product) {
        return new UpdateProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice()
        );
    }
}
