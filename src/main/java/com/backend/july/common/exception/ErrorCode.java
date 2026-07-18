package com.backend.july.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus status();

    String message();

    // enum 상수명을 에러 식별자로 반환
    default String name() {
        if (this instanceof Enum<?> enumConstant) {
            return enumConstant.name();
        }
        return this.getClass().getSimpleName(); // enum이 아닐 경우를 위한 가벼운 폴백(Fallback)
    }

    // 메시지 템플릿에 전달받은 값을 적용
    default String format(Object... arguments) {
        if (arguments == null || arguments.length == 0) {
            return message();
        }

        return message().formatted(arguments);
    }
}
