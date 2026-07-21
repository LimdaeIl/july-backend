package com.backend.july.cart.application;

import com.backend.july.cart.domain.CartItem;
import com.backend.july.cart.exception.CartErrorCode;
import com.backend.july.cart.exception.CartException;
import com.backend.july.cart.infrastructure.CartItemRepository;
import com.backend.july.cart.presentation.dto.response.UpdateCartItemQuantityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCartItemQuantityService {

    private final CartItemRepository cartItemRepository;

    @Transactional
    public UpdateCartItemQuantityResponse update(Long memberId, Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository
                .findByIdAndMemberIdForUpdate(cartItemId, memberId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_ITEM_NOT_FOUND));

        cartItem.changeQuantity(quantity);

        return UpdateCartItemQuantityResponse.from(cartItem);
    }
}
