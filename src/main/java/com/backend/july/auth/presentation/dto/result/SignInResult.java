package com.backend.july.auth.presentation.dto.result;

public record SignInResult(
        Long id,
        String accessToken,
        String refreshToken,
        long refreshTokenRemainingSecond
) {

    public static SignInResult of(Long id, String accessToken, String refreshToken,
            long refreshTokenRemainingSecond) {
        return new SignInResult(id, accessToken, refreshToken,
                refreshTokenRemainingSecond);
    }
}

