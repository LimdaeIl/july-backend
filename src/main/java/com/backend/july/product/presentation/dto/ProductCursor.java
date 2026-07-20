package com.backend.july.product.presentation.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductCursor(
        Long productId,
        Instant createdAt,
        BigDecimal price
) {

    public static ProductCursor latest(
            Long productId,
            Instant createdAt
    ) {
        return new ProductCursor(
                productId,
                createdAt,
                null
        );
    }

    public static ProductCursor price(
            Long productId,
            BigDecimal price
    ) {
        return new ProductCursor(
                productId,
                null,
                price
        );
    }
}