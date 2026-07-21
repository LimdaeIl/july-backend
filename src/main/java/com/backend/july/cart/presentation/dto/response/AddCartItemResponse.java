package com.backend.july.cart.presentation.dto.response;

import com.backend.july.cart.domain.CartItem;
import java.math.BigDecimal;

public record AddCartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        BigDecimal currentPrice,
        int quantity,
        BigDecimal lineAmount
) {

    public static AddCartItemResponse from(CartItem cartItem) {
        BigDecimal currentPrice = cartItem.getProduct().getPrice();

        return new AddCartItemResponse(
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
