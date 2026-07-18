package com.backend.july.auth.infrastructure.jwt;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RotateResult {

    SUCCESS(1L, "성공"),
    NOT_FOUND(-1L, "리프레시 토큰 없음"),
    MISMATCH(0L, "리프레시 토큰 불일치");

    private final long code;
    private final String description;

    public static RotateResult from(Long code) {
        return Arrays.stream(values())
                .filter(result -> result.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 결과 코드: " + code));
    }
}

