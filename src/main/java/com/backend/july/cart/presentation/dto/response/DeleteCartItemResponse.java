package com.backend.july.cart.presentation.dto.response;

public record DeleteCartItemResponse(
        Long cartItemId
) {

    public static DeleteCartItemResponse of(Long cartItemId) {
        return new DeleteCartItemResponse(cartItemId);
    }
}
