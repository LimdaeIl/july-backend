package com.backend.july.auth.infrastructure.jwt;

import com.backend.july.auth.exception.AuthErrorCode;
import com.backend.july.auth.exception.AuthException;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String issuer,
        String base64Secret,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_JWT_ISSUER);
        }

        if (base64Secret == null || base64Secret.isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_JWT_SECRET);
        }

        validateExpiration(accessTokenExpiration, "Access Token");
        validateExpiration(refreshTokenExpiration, "Refresh Token");
    }

    private static void validateExpiration(Duration expiration, String tokenName) {
        if (expiration == null || expiration.isZero() || expiration.isNegative()) {
            throw new AuthException(AuthErrorCode.INVALID_JWT_EXPIRATION, tokenName);
        }
    }
}