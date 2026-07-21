package com.backend.july.cart.presentation.dto.response;

import com.backend.july.cart.domain.Cart;
import com.backend.july.cart.domain.CartItem;
import com.backend.july.product.domain.Product;
import com.backend.july.product.domain.ProductStatus;
import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        List<CartItemResponse> items,
        int itemCount,
        int totalQuantity,
        BigDecimal totalAmount
) {

    public static CartResponse empty() {
        return new CartResponse(
                null,
                List.of(),
                0,
                0,
                BigDecimal.ZERO
        );
    }

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(CartItemResponse::from)
                .toList();

        int totalQuantity = items.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::lineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                items,
                items.size(),
                totalQuantity,
                totalAmount
        );
    }

    public record CartItemResponse(
            Long cartItemId,
            Long productId,
            String productName,
            BigDecimal currentPrice,
            ProductStatus productStatus,
            boolean orderable,
            int quantity,
            BigDecimal lineAmount
    ) {

        public static CartItemResponse from(CartItem cartItem) {
            Product product = cartItem.getProduct();
            BigDecimal currentPrice = product.getPrice();

            return new CartItemResponse(
                    cartItem.getId(),
                    product.getId(),
                    product.getName(),
                    currentPrice,
                    product.getStatus(),
                    product.isOnSale(),
                    cartItem.getQuantity(),
                    currentPrice.multiply(
                            BigDecimal.valueOf(cartItem.getQuantity())
                    )
            );
        }
    }
}
