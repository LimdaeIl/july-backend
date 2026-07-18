package com.backend.july.auth.infrastructure.jwt;


import com.backend.july.auth.exception.AuthErrorCode;
import com.backend.july.auth.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String ROLE_CLAIM = "role";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtTokenProvider(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.base64Secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long memberId, String role) {
        return createToken(memberId, ACCESS_TOKEN, role, jwtProperties.accessTokenExpiration());
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, REFRESH_TOKEN, null, jwtProperties.refreshTokenExpiration());
    }

    public void validateAccessToken(String token) {
        Claims claims = parseClaims(
                token,
                AuthErrorCode.EXPIRED_ACCESS_TOKEN,
                AuthErrorCode.INVALID_ACCESS_TOKEN
        );

        validateTokenType(claims, ACCESS_TOKEN);
    }

    public void validateRefreshToken(String token) {
        Claims claims = parseClaims(
                token,
                AuthErrorCode.EXPIRED_REFRESH_TOKEN,
                AuthErrorCode.INVALID_REFRESH_TOKEN
        );

        validateTokenType(claims, REFRESH_TOKEN);
    }

    public Long getMemberIdFromAccessToken(String token) {
        Claims claims = parseClaims(
                token,
                AuthErrorCode.EXPIRED_ACCESS_TOKEN,
                AuthErrorCode.INVALID_ACCESS_TOKEN
        );

        validateTokenType(claims, ACCESS_TOKEN);

        return Long.valueOf(claims.getSubject());
    }

    public Long getMemberIdFromRefreshToken(String token) {
        Claims claims = parseClaims(
                token,
                AuthErrorCode.EXPIRED_REFRESH_TOKEN,
                AuthErrorCode.INVALID_REFRESH_TOKEN
        );

        validateTokenType(claims, REFRESH_TOKEN);

        return Long.valueOf(claims.getSubject());
    }

    public String getRoleFromAccessToken(String token) {
        Claims claims = parseClaims(
                token,
                AuthErrorCode.EXPIRED_ACCESS_TOKEN,
                AuthErrorCode.INVALID_ACCESS_TOKEN
        );

        validateTokenType(claims, ACCESS_TOKEN);

        return claims.get(ROLE_CLAIM, String.class);
    }

    public long getAccessTokenRemainingMillis(String token) {
        Claims claims = parseClaims(
                token,
                AuthErrorCode.EXPIRED_ACCESS_TOKEN,
                AuthErrorCode.INVALID_ACCESS_TOKEN
        );

        validateTokenType(claims, ACCESS_TOKEN);

        return getRemainingMillis(claims, AuthErrorCode.INVALID_ACCESS_TOKEN);
    }

    public long getRefreshTokenRemainingMillis(String token) {
        Claims claims = parseClaims(
                token,
                AuthErrorCode.EXPIRED_REFRESH_TOKEN,
                AuthErrorCode.INVALID_REFRESH_TOKEN
        );

        validateTokenType(claims, REFRESH_TOKEN);

        return getRemainingMillis(claims, AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    private long getRemainingMillis(Claims claims, AuthErrorCode invalidErrorCode) {
        Date expiration = claims.getExpiration();

        if (expiration == null) {
            throw new AuthException(invalidErrorCode);
        }

        long remainingMillis = expiration.getTime() - clock.millis();

        return Math.max(remainingMillis, 0);
    }

    public long getAccessTokenRemainingSeconds(String token) {
        return getAccessTokenRemainingMillis(token) / 1000;
    }

    public long getRefreshTokenRemainingSeconds(String token) {
        return getRefreshTokenRemainingMillis(token) / 1000;
    }

    public long getRefreshTokenExpirationMillis() {
        return jwtProperties.refreshTokenExpiration().toMillis();
    }

    private String createToken(
            Long memberId,
            String tokenType,
            String role,
            Duration expirationDuration
    ) {
        Instant now = clock.instant();
        Instant expiration = now.plus(expirationDuration);

        JwtBuilder builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(memberId))
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration));

        if (role != null && !role.isBlank()) {
            builder.claim(ROLE_CLAIM, role);
        }

        return builder
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(
            String token,
            AuthErrorCode expiredErrorCode,
            AuthErrorCode invalidErrorCode
    ) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new AuthException(expiredErrorCode);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(invalidErrorCode);
        }
    }

    private void validateTokenType(Claims claims, String expectedTokenType) {
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);

        if (!expectedTokenType.equals(tokenType)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN_TYPE);
        }
    }
}

