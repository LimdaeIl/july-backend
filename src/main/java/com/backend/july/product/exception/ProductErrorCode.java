package com.backend.july.product.exception;

import com.backend.july.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    VALIDATE_FIELD(HttpStatus.BAD_REQUEST, "%s는 null 또는 공백일 수 없습니다."),
    INVALID_CURSOR_SORT_TYPE(HttpStatus.BAD_REQUEST, "가격 커서에는 가격 정렬 타입이 필요합니다."),
    CURSOR_ID_REQUIRED(HttpStatus.BAD_REQUEST, "커서 ID는 필수입니다."),
    CURSOR_CREATED_AT_REQUIRED(HttpStatus.BAD_REQUEST, "커서 생성일은 필수입니다."),
    CURSOR_PRICE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "커서 가격은 허용되지 않습니다."),
    CURSOR_PRICE_REQUIRED(HttpStatus.BAD_REQUEST, "커서 가격은 필수입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다."),
    INVENTORY_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 재고가 등록된 상품입니다.");


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
