package com.backend.july.product.presentation.dto.response;

import com.backend.july.product.domain.Product;
import com.backend.july.product.domain.ProductStatus;

public record UpdateProductStatusResponse(
        Long productId,
        ProductStatus status
) {

    public static UpdateProductStatusResponse from(Product product) {
        return new UpdateProductStatusResponse(product.getId(), product.getStatus());
    }

}
