package com.backend.july.product.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(

        @NotNull(message = "상품 상태는 필수입니다.")
        ProductChangeableStatus status
) {

    public enum ProductChangeableStatus {
        ON_SALE,
        HIDDEN
    }

}
