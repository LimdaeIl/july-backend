package com.backend.july.product.presentation.dto;

import org.springframework.data.domain.Sort;

public enum ProductSortType {

    LATEST,
    PRICE_LOW,
    PRICE_HIGH;

    public Sort toSort() {
        return switch (this) {
            case LATEST -> Sort.by(
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id")
            );

            case PRICE_LOW -> Sort.by(
                    Sort.Order.asc("price"),
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id")
            );

            case PRICE_HIGH -> Sort.by(
                    Sort.Order.desc("price"),
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id")
            );
        };
    }
}
