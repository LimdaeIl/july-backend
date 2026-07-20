package com.backend.july.order.exception;

import com.backend.july.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    MEMBER_REQUIRED(HttpStatus.BAD_REQUEST, "주문 회원은 필수입니다."),
    ORDER_NUMBER_REQUIRED(HttpStatus.BAD_REQUEST, "주문 번호는 필수입니다."),
    ORDER_ITEM_REQUIRED(HttpStatus.BAD_REQUEST, "주문 상품은 필수입니다."),
    ORDER_ITEMS_EMPTY(HttpStatus.BAD_REQUEST, "주문에는 하나 이상의 상품이 필요합니다."),
    DUPLICATE_ORDER_ITEM(HttpStatus.CONFLICT, "동일한 상품이 주문에 중복으로 포함되어 있습니다."),
    INVALID_ORDER_STATUS(HttpStatus.CONFLICT, "현재 주문 상태에서는 요청한 작업을 수행할 수 없습니다."),
    ORDER_NOT_OWNED(HttpStatus.FORBIDDEN, "해당 주문에 접근할 권한이 없습니다."),
    INVALID_TOTAL_AMOUNT(HttpStatus.BAD_REQUEST, "주문 총액이 올바르지 않습니다."),
    TOTAL_AMOUNT_OVERFLOW(HttpStatus.INTERNAL_SERVER_ERROR, "주문 총액이 허용 범위를 초과했습니다."),
    ORDER_REQUIRED(HttpStatus.BAD_REQUEST, "주문은 필수입니다."),
    PRODUCT_REQUIRED(HttpStatus.BAD_REQUEST, "상품은 필수입니다."),
    PRODUCT_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "상품명은 필수입니다."),
    ORDER_PRICE_REQUIRED(HttpStatus.BAD_REQUEST, "주문 가격은 필수입니다."),
    INVALID_ORDER_PRICE(HttpStatus.BAD_REQUEST, "주문 가격은 0 이상이어야 합니다."),
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "주문 수량은 1 이상이어야 합니다."),
    ORDER_ITEM_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "주문 상품이 이미 다른 주문에 연결되어 있습니다."),
    ORDER_ITEM_AMOUNT_OVERFLOW(HttpStatus.INTERNAL_SERVER_ERROR, "주문 상품 금액이 허용 범위를 초과했습니다."),;

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