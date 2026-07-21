package com.backend.july.cart.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCartItemRequest(

        @NotNull(message = "상품 ID는 필수입니다.")
        @Positive(message = "상품 ID는 양수여야 합니다.")
        Long productId,

        @NotNull(message = "장바구니 수량은 필수입니다.")
        @Positive(message = "장바구니 수량은 1개 이상이어야 합니다.")
        Integer quantity
) {
}
