package com.backend.july.inventory.exception;

import com.backend.july.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {

    INVENTORY_INSUFFICIENT(HttpStatus.BAD_REQUEST, "재고가 부족합니다. 현재 수량: %s 요청 수량: %s"),
    INVENTORY_OVERFLOW(HttpStatus.BAD_REQUEST, "재고 수량이 허용 범위를 초과했습니다. 현재 수량: %s 요청 수량: %s"),
    PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST, "상품을 찾을 수 없습니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "재고 수량은 0 이상이어야 합니다."),
    INVALID_CHANGE_QUANTITY(HttpStatus.BAD_REQUEST, "재고 변경 수량은 1 이상이어야 합니다."),
    INVENTORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "재고를 찾을 수 없습니다.");

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
