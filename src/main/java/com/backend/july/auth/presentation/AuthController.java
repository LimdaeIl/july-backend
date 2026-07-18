package com.backend.july.auth.presentation;

import com.backend.july.auth.application.RefreshTokenCookieProvider;
import com.backend.july.auth.application.SignInService;
import com.backend.july.auth.application.SignUpService;
import com.backend.july.auth.presentation.dto.request.SignInRequest;
import com.backend.july.auth.presentation.dto.request.SignUpRequest;
import com.backend.july.auth.presentation.dto.response.SignInResponse;
import com.backend.july.auth.presentation.dto.response.SignUpResponse;
import com.backend.july.auth.presentation.dto.result.SignInResult;
import com.backend.july.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final SignUpService signUpService;
    private final SignInService signInService;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;


    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @RequestBody @Valid SignUpRequest request
    ) {
        SignUpResponse response = signUpService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "회원 가입: 회원 가입에 성공했습니다.",
                        response
                ));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<SignInResponse>> signIn(
            @RequestBody @Valid SignInRequest request,
            HttpServletResponse servletResponse
    ) {
        SignInResult signInResult = signInService.signIn(request);

        refreshTokenCookieProvider.addRefreshTokenCookie(
                servletResponse,
                signInResult.refreshToken(),
                signInResult.refreshTokenRemainingSecond()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok(
                        "로그인: 로그인에 성공했습니다.",
                        SignInResponse.of(
                                signInResult.id(),
                                signInResult.accessToken()
                        ))
                );
    }
}
