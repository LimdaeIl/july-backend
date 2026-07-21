package com.backend.july.cart.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemQuantityRequest(

        @NotNull(message = "변경할 수량은 필수입니다.")
        @Positive(message = "장바구니 수량은 1개 이상이어야 합니다.")
        Integer quantity
) {
}
