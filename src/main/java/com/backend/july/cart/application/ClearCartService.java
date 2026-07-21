package com.backend.july.cart.application;

import com.backend.july.cart.domain.Cart;
import com.backend.july.cart.infrastructure.CartRepository;
import com.backend.july.cart.presentation.dto.response.ClearCartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClearCartService {

    private final CartRepository cartRepository;

    @Transactional
    public ClearCartResponse clear(Long memberId) {
        Cart cart = cartRepository.findByMemberIdForUpdate(memberId).orElse(null);

        if (cart == null) {
            return ClearCartResponse.of(0);
        }

        int deletedItemCount = cart.clear();

        return ClearCartResponse.of(deletedItemCount);
    }
}
