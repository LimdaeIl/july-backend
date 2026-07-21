package com.backend.july.cart.exception;

import com.backend.july.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {

    MEMBER_REQUIRED(HttpStatus.BAD_REQUEST, "장바구니 회원은 필수입니다."),
    PRODUCT_REQUIRED(HttpStatus.BAD_REQUEST, "장바구니 상품은 필수입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니에 담을 상품을 찾을 수 없습니다."),
    CART_REQUIRED(HttpStatus.BAD_REQUEST, "장바구니는 필수입니다."),
    CART_ITEM_REQUIRED(HttpStatus.BAD_REQUEST, "장바구니 상품은 필수입니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 상품을 찾을 수 없습니다."),
    INVALID_CART_ITEM_QUANTITY(HttpStatus.BAD_REQUEST, "장바구니 상품 수량은 1 이상이어야 합니다."),
    CART_ITEM_QUANTITY_OVERFLOW(HttpStatus.BAD_REQUEST, "장바구니 상품 수량이 허용 범위를 초과했습니다."),
    CART_ITEM_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "장바구니 상품이 이미 다른 장바구니에 연결되어 있습니다."),
    CART_ITEM_NOT_OWNED(HttpStatus.FORBIDDEN, "해당 장바구니 상품에 접근할 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }
}
