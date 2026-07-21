package com.backend.july.auth.application;

import com.backend.july.auth.exception.AuthErrorCode;
import com.backend.july.auth.exception.AuthException;
import com.backend.july.auth.infrastructure.jwt.JWTHashUtil;
import com.backend.july.auth.infrastructure.jwt.JwtTokenProvider;
import com.backend.july.auth.infrastructure.redis.TokenRepository;
import com.backend.july.auth.presentation.dto.request.SignInRequest;
import com.backend.july.auth.presentation.dto.result.SignInResult;
import com.backend.july.member.domain.Member;
import com.backend.july.member.infrastructure.MemberRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SignInService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;
    private final JWTHashUtil jwtHashUtil;

    @Transactional
    public SignInResult signIn(SignInRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(
                        () -> new AuthException(AuthErrorCode.INVALID_SIGN_IN)
                );

        validateSignIn(member, request.password());

        String accessToken = jwtTokenProvider.createAccessToken(
                member.getId(),
                member.getRole().name()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(
                member.getId()
        );

        long refreshTokenRemainingMillis =
                jwtTokenProvider.getRefreshTokenRemainingMillis(refreshToken);

        String hashedToken = jwtHashUtil.sha256(refreshToken);

        tokenRepository.save(
                member.getId(),
                hashedToken,
                Duration.ofMillis(refreshTokenRemainingMillis)
        );

        return SignInResult.of(
                member.getId(),
                accessToken,
                refreshToken,
                jwtTokenProvider.getRefreshTokenRemainingSeconds(refreshToken)
        );
    }

    private void validateSignIn(
            Member member,
            String rawPassword
    ) {
        if (!member.isSignInAllowed()) {
            throw new AuthException(AuthErrorCode.INVALID_SIGN_IN);
        }

        if (!passwordEncoder.matches(
                rawPassword,
                member.getPassword()
        )) {
            throw new AuthException(AuthErrorCode.INVALID_SIGN_IN);
        }
    }
}
