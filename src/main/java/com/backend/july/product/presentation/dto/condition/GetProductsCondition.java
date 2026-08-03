package com.backend.july.product.presentation.dto.condition;

import com.backend.july.product.presentation.dto.ProductSortType;

public record GetProductsCondition(
        Long sellerId,
        String keyword,
        ProductSortType sortType,
        int page,
        int size
) {

    public GetProductsCondition {
        keyword = normalizeKeyword(keyword);
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }

    public boolean hasKeyword() {
        return keyword != null;
    }


}
