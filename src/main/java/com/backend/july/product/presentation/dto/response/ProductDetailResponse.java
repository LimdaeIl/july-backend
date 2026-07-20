package com.backend.july.product.presentation.dto.response;

import com.backend.july.inventory.domain.Inventory;
import com.backend.july.product.domain.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductDetailResponse(
        Long productId,
        String name,
        BigDecimal price,
        String status,
        int quantity,
        Long sellerId,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProductDetailResponse of(
            Product product,
            Inventory inventory
    ) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStatus().name(),
                inventory.getQuantity(),
                product.getCreatedBy(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
