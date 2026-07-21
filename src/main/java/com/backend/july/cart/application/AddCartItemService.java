package com.backend.july.cart.application;

import com.backend.july.cart.domain.Cart;
import com.backend.july.cart.domain.CartItem;
import com.backend.july.cart.exception.CartErrorCode;
import com.backend.july.cart.exception.CartException;
import com.backend.july.cart.infrastructure.CartRepository;
import com.backend.july.cart.presentation.dto.request.AddCartItemRequest;
import com.backend.july.cart.presentation.dto.response.AddCartItemResponse;
import com.backend.july.member.domain.Member;
import com.backend.july.member.exception.MemberErrorCode;
import com.backend.july.member.exception.MemberException;
import com.backend.july.member.infrastructure.MemberRepository;
import com.backend.july.product.domain.Product;
import com.backend.july.product.infrastructure.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddCartItemService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    @Transactional
    public AddCartItemResponse add(Long memberId, AddCartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new CartException(CartErrorCode.PRODUCT_NOT_FOUND));

        product.validateOrderable();

        Cart cart = getOrCreateCart(memberId);

        CartItem cartItem = cart.addProduct(product, request.quantity());

        cartRepository.save(cart);

        return AddCartItemResponse.from(cartItem);
    }

    private Cart getOrCreateCart(Long memberId) {
        Cart existingCart = cartRepository.findByMemberIdForUpdate(memberId).orElse(null);

        if (existingCart != null) {
            return existingCart;
        }

        /*
         * 아직 장바구니가 없으면 회원 행을 잠급니다.
         *
         * 동일 회원에게 장바구니를 동시에 생성하려는 요청을 직렬화하여
         * member_id 유니크 제약 조건 충돌을 방지합니다.
         */
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        /*
         * 회원 락을 기다리는 동안 다른 트랜잭션이 장바구니를 생성했을 수 있으므로
         * 락 획득 후 다시 확인합니다.
         */
        return cartRepository.findByMemberIdForUpdate(memberId)
                .orElseGet(() -> Cart.create(member));
    }
}
