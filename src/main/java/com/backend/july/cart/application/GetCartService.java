package com.backend.july.cart.application;

import com.backend.july.cart.infrastructure.CartRepository;
import com.backend.july.cart.presentation.dto.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCartService {

    private final CartRepository cartRepository;

    @Transactional(readOnly = true)
    public CartResponse get(Long memberId) {
        return cartRepository.findDetailByMemberId(memberId)
                .map(CartResponse::from)
                .orElseGet(CartResponse::empty);
    }
}
