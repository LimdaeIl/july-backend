package com.backend.july.auth.presentation;

import com.backend.july.auth.presentation.dto.request.SignInRequest;
import com.backend.july.auth.presentation.dto.request.SignUpRequest;
import com.backend.july.auth.presentation.dto.response.ReissueResponse;
import com.backend.july.auth.presentation.dto.response.SignInResponse;
import com.backend.july.auth.presentation.dto.response.SignUpResponse;
import com.backend.july.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "인증 API",
        description = "회원 가입, 로그인, 토큰 재발급 및 로그아웃을 처리하는 API입니다."
)
public interface AuthControllerDocs {

    @Operation(
            summary = "회원 가입",
            description = """
                    새로운 회원을 생성합니다.

                    회원 가입에 필요한 이메일, 비밀번호 및 회원 정보를 요청 본문으로 전달합니다.
                    이메일 등 고유 정보가 이미 사용 중인 경우 회원 가입에 실패합니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원 가입 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패 또는 올바르지 않은 회원 정보"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 이메일 등 회원 정보 중복"
            )
    })
    ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @Valid SignUpRequest request
    );

    @Operation(
            summary = "로그인",
            description = """
                    이메일과 비밀번호를 이용하여 로그인합니다.

                    로그인에 성공하면 응답 본문으로 액세스 토큰을 반환하고,
                    리프레시 토큰은 HttpOnly 쿠키로 발급합니다.

                    이후 인증이 필요한 API 요청에는 액세스 토큰을 사용합니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "이메일 또는 비밀번호가 올바르지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회원 정보를 찾을 수 없음"
            )
    })
    ResponseEntity<ApiResponse<SignInResponse>> signIn(
            @Valid SignInRequest request,

            @Parameter(hidden = true)
            HttpServletResponse servletResponse
    );

    @Operation(
            summary = "토큰 재발급",
            description = """
                    리프레시 토큰을 이용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.

                    리프레시 토큰은 요청 쿠키에서 조회합니다.
                    재발급에 성공하면 새로운 액세스 토큰은 응답 본문으로 반환하고,
                    새로운 리프레시 토큰은 HttpOnly 쿠키로 다시 설정합니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 재발급 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "리프레시 토큰이 없거나 유효하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "리프레시 토큰에 해당하는 회원을 찾을 수 없음"
            )
    })
    ResponseEntity<ApiResponse<ReissueResponse>> reissue(
            @Parameter(
                    name = "refreshToken",
                    description = "로그인 시 HttpOnly 쿠키로 발급된 리프레시 토큰입니다.",
                    in = ParameterIn.COOKIE
            )
            String refreshToken,

            @Parameter(hidden = true)
            HttpServletResponse servletResponse
    );

    @Operation(
            summary = "로그아웃",
            description = """
                    현재 로그인 상태를 종료합니다.

                    요청 쿠키의 리프레시 토큰을 무효화하고,
                    클라이언트에 저장된 리프레시 토큰 쿠키를 제거합니다.

                    로그아웃이 완료되면 응답 본문 없이 204 상태 코드를 반환합니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "로그아웃 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "리프레시 토큰이 유효하지 않음"
            )
    })
    ResponseEntity<Void> signOut(
            @Parameter(
                    name = "refreshToken",
                    description = "로그인 시 HttpOnly 쿠키로 발급된 리프레시 토큰입니다.",
                    in = ParameterIn.COOKIE
            )
            String refreshToken,

            @Parameter(hidden = true)
            HttpServletResponse response
    );
}