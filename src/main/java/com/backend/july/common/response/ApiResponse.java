package com.backend.july.common.response;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        LocalDateTime timestamp,
        int status,
        String message,
        T data
) {

    public static <T> ApiResponse<T> ok(T data) {
        return of(HttpStatus.OK, "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return of(HttpStatus.OK, message, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return of(HttpStatus.CREATED, "리소스가 성공적으로 생성되었습니다.", data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return of(HttpStatus.CREATED, message, data);
    }

    private static <T> ApiResponse<T> of(HttpStatus status, String message, T data) {
        return new ApiResponse<>(LocalDateTime.now(), status.value(), message, data);
    }
}
