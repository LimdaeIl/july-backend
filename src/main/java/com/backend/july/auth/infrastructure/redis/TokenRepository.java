package com.backend.july.auth.infrastructure.redis;

import com.backend.july.auth.exception.AuthErrorCode;
import com.backend.july.auth.exception.AuthException;
import com.backend.july.auth.infrastructure.jwt.RotateResult;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class TokenRepository {

    private static final String KEY_PREFIX = "auth:refreshtoken:";

    private static final DefaultRedisScript<Long> ROTATE_IF_MATCHES_SCRIPT =
            new DefaultRedisScript<>("""
                    local current = redis.call('GET', KEYS[1])
                    
                    if current == false then
                        return -1
                    end
                    
                    if current ~= ARGV[1] then
                        return 0
                    end
                    
                    redis.call('PSETEX', KEYS[1], ARGV[3], ARGV[2])
                    return 1
                    """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public void save(Long memberId, String hashedRefreshToken, Duration ttl) {
        try {
            validateRefreshTokenTtl(ttl);

            redisTemplate.opsForValue()
                    .set(key(memberId), hashedRefreshToken, ttl);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }

    public Optional<String> findByMemberId(Long memberId) {
        try {
            return Optional.ofNullable(
                    redisTemplate.opsForValue().get(key(memberId))
            );
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }

    public void deleteByMemberId(Long memberId) {
        try {
            redisTemplate.delete(key(memberId));
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }

    public void rotateIfMatches(
            Long memberId,
            String hashedOldRefreshToken,
            String hashedNewRefreshToken,
            Duration refreshTokenTtl
    ) {
        try {
            validateRefreshTokenTtl(refreshTokenTtl);

            Long resultCode = redisTemplate.execute(
                    ROTATE_IF_MATCHES_SCRIPT,
                    Collections.singletonList(key(memberId)),
                    hashedOldRefreshToken,
                    hashedNewRefreshToken,
                    String.valueOf(refreshTokenTtl.toMillis())
            );

            RotateResult result = RotateResult.from(resultCode);

            switch (result) {
                case SUCCESS -> {
                    return;
                }
                case NOT_FOUND, MISMATCH ->
                        throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
            }
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }

    private void validateRefreshTokenTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }

    private String key(Long memberId) {
        return KEY_PREFIX + memberId;
    }
}

