package com.backend.july.product.presentation.dto.condition;

import com.backend.july.product.exception.ProductErrorCode;
import com.backend.july.product.exception.ProductException;
import com.backend.july.product.presentation.dto.ProductSortType;
import java.math.BigDecimal;
import java.time.Instant;

public record GetProductsCursorCondition(
        Long sellerId,
        ProductSortType sortType,
        Long cursorId,
        Instant cursorCreatedAt,
        BigDecimal cursorPrice,
        int size
) {

    public GetProductsCursorCondition {
        validateCursor(
                sortType,
                cursorId,
                cursorCreatedAt,
                cursorPrice
        );
    }

    private static void validateCursor(
            ProductSortType sortType,
            Long cursorId,
            Instant cursorCreatedAt,
            BigDecimal cursorPrice
    ) {
        boolean cursorAbsent = cursorId == null
                && cursorCreatedAt == null
                && cursorPrice == null;

        if (cursorAbsent) {
            return;
        }

        if (cursorId == null) {
            throw new ProductException(ProductErrorCode.CURSOR_ID_REQUIRED);
        }

        switch (sortType) {
            case LATEST -> validateLatestCursor(cursorCreatedAt, cursorPrice);

            case PRICE_LOW, PRICE_HIGH -> validatePriceCursor(cursorCreatedAt, cursorPrice);
        }
    }

    private static void validateLatestCursor(Instant cursorCreatedAt, BigDecimal cursorPrice) {
        if (cursorCreatedAt == null) {
            throw new ProductException(ProductErrorCode.CURSOR_CREATED_AT_REQUIRED);
        }

        if (cursorPrice != null) {
            throw new ProductException(ProductErrorCode.CURSOR_PRICE_NOT_ALLOWED);
        }
    }

    private static void validatePriceCursor(Instant cursorCreatedAt, BigDecimal cursorPrice) {
        if (cursorPrice == null) {
            throw new ProductException(ProductErrorCode.CURSOR_PRICE_REQUIRED);

        }

        if (cursorCreatedAt != null) {
            throw new ProductException(ProductErrorCode.CURSOR_CREATED_AT_REQUIRED);
        }
    }
}