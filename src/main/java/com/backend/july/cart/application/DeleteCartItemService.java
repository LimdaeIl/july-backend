package com.backend.july.cart.application;

import com.backend.july.cart.domain.Cart;
import com.backend.july.cart.domain.CartItem;
import com.backend.july.cart.exception.CartErrorCode;
import com.backend.july.cart.exception.CartException;
import com.backend.july.cart.infrastructure.CartItemRepository;
import com.backend.july.cart.presentation.dto.response.DeleteCartItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCartItemService {

    private final CartItemRepository cartItemRepository;

    @Transactional
    public DeleteCartItemResponse delete(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository
                .findByIdAndMemberIdForUpdate(cartItemId, memberId)
                .orElseThrow(() ->
                        new CartException(CartErrorCode.CART_ITEM_NOT_FOUND)
                );

        Cart cart = cartItem.getCart();
        cart.removeItem(cartItem);

        return DeleteCartItemResponse.of(cartItemId);
    }
}
