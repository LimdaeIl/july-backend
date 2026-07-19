package com.backend.july.product.presentation.dto.condition;

import com.backend.july.product.presentation.dto.ProductSortType;

public record GetProductsCondition(
        Long sellerId,
        ProductSortType sortType,
        int page,
        int size
) {

    public GetProductsCondition {
        if (sortType == null) {
            sortType = ProductSortType.LATEST;
        }
    }
}
