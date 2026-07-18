package com.backend.july.auth.presentation.dto.result;

public record ReissueTokenResult(
        Long id,
        String newAccessToken,
        String newRefreshToken,
        long refreshTokenRemainingSecond
) {

    public static ReissueTokenResult of(Long id, String newAccessToken,
            String newRefreshToken, long refreshTokenRemainingSecond) {
        return new ReissueTokenResult(id, newAccessToken, newRefreshToken,
                refreshTokenRemainingSecond);
    }
}

