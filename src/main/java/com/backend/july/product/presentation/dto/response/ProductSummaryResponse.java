package com.backend.july.product.presentation.dto.response;

import com.backend.july.product.domain.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductSummaryResponse(
        Long productId,
        String name,
        BigDecimal price,
        String status,
        Long sellerId,
        Instant createdAt
) {

    public static ProductSummaryResponse from(Product product) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStatus().name(),
                product.getCreatedBy(),
                product.getCreatedAt()
        );
    }
}
