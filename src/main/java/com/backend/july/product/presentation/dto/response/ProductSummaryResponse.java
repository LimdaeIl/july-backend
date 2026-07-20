package com.backend.july.product.presentation.dto.response;

import com.backend.july.inventory.domain.Inventory;
import com.backend.july.product.domain.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductSummaryResponse(
        Long productId,
        String name,
        BigDecimal price,
        String status,
        int quantity,
        Long sellerId,
        Instant createdAt
) {

    public static ProductSummaryResponse of(Product product, Inventory inventory) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStatus().name(),
                inventory.getQuantity(),
                product.getCreatedBy(),
                product.getCreatedAt()
        );
    }
}
