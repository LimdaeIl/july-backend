package com.backend.july.product.exception;

import com.backend.july.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    VALIDATE_FIELD(HttpStatus.BAD_REQUEST, "%s는 null 또는 공백일 수 없습니다.");



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
