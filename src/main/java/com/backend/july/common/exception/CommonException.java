package com.backend.july.common.exception;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {

    private final ErrorCode errorCode;

    public CommonException(ErrorCode errorCode) {
        super(requireErrorCode(errorCode).message());
        this.errorCode = errorCode;
    }

    public CommonException(ErrorCode errorCode, Object... arguments) {
        super(requireErrorCode(errorCode).format(arguments));
        this.errorCode = errorCode;
    }

    // ErrorCode가 없는 잘못된 예외 생성을 방지
    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode는 null일 수 없습니다.");
    }
}
