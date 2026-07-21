package com.backend.july.cart.presentation.dto.response;

import com.backend.july.cart.domain.CartItem;
import java.math.BigDecimal;

public record UpdateCartItemQuantityResponse(
        Long cartItemId,
        Long productId,
        String productName,
        BigDecimal currentPrice,
        int quantity,
        BigDecimal lineAmount
) {

    public static UpdateCartItemQuantityResponse from(CartItem cartItem) {
        BigDecimal currentPrice = cartItem.getProduct().getPrice();

        return new UpdateCartItemQuantityResponse(
                cartItem.getId(),
                cartItem.getProductId(),
                cartItem.getProduct().getName(),
                currentPrice,
                cartItem.getQuantity(),
                currentPrice.multiply(
                        BigDecimal.valueOf(cartItem.getQuantity())
                )
        );
    }
}
