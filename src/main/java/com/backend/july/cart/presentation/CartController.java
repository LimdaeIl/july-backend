package com.backend.july.cart.presentation;

import com.backend.july.auth.infrastructure.security.principal.LoginMember;
import com.backend.july.cart.application.AddCartItemService;
import com.backend.july.cart.application.ClearCartService;
import com.backend.july.cart.application.DeleteCartItemService;
import com.backend.july.cart.application.GetCartService;
import com.backend.july.cart.application.UpdateCartItemQuantityService;
import com.backend.july.cart.presentation.dto.request.AddCartItemRequest;
import com.backend.july.cart.presentation.dto.request.UpdateCartItemQuantityRequest;
import com.backend.july.cart.presentation.dto.response.AddCartItemResponse;
import com.backend.july.cart.presentation.dto.response.CartResponse;
import com.backend.july.cart.presentation.dto.response.ClearCartResponse;
import com.backend.july.cart.presentation.dto.response.DeleteCartItemResponse;
import com.backend.july.cart.presentation.dto.response.UpdateCartItemQuantityResponse;
import com.backend.july.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/cart")
@RestController
public class CartController implements CartControllerDocs {

    private final AddCartItemService addCartItemService;
    private final GetCartService getCartService;
    private final UpdateCartItemQuantityService updateCartItemQuantityService;
    private final DeleteCartItemService deleteCartItemService;
    private final ClearCartService clearCartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<AddCartItemResponse>> addItem(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        AddCartItemResponse response = addCartItemService.add(loginMember.memberId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.created(
                                "장바구니 상품 추가에 성공했습니다.",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        CartResponse response = getCartService.get(loginMember.memberId()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "장바구니 조회에 성공했습니다.",
                        response
                )
        );
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<UpdateCartItemQuantityResponse>>
    updateItemQuantity(
            @AuthenticationPrincipal LoginMember loginMember,

            @PathVariable
            @Positive(message = "장바구니 상품 ID는 양수여야 합니다.")
            Long cartItemId,

            @Valid
            @RequestBody
            UpdateCartItemQuantityRequest request
    ) {
        UpdateCartItemQuantityResponse response = updateCartItemQuantityService.update(
                loginMember.memberId(), cartItemId, request.quantity());

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "장바구니 상품 수량 변경에 성공했습니다.",
                        response
                )
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<DeleteCartItemResponse>> deleteItem(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable
            @Positive(message = "장바구니 상품 ID는 양수여야 합니다.")
            Long cartItemId
    ) {
        DeleteCartItemResponse response = deleteCartItemService.delete(loginMember.memberId(), cartItemId);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "장바구니 상품 삭제에 성공했습니다.",
                        response
                )
        );
    }

    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<ClearCartResponse>> clearCart(
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        ClearCartResponse response = clearCartService.clear(loginMember.memberId());

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "장바구니 비우기에 성공했습니다.",
                        response
                )
        );
    }
}
