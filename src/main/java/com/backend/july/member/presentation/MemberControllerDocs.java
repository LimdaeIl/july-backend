package com.backend.july.member.presentation;

import com.backend.july.auth.infrastructure.security.principal.LoginMember;
import com.backend.july.common.response.ApiResponse;
import com.backend.july.member.presentation.dto.response.GetMeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "회원 API",
        description = "로그인한 회원의 정보를 조회하는 API입니다."
)
public interface MemberControllerDocs {

    @Operation(
            summary = "내 정보 조회",
            description = """
                    로그인한 회원의 정보를 조회합니다.

                    요청 헤더의 액세스 토큰을 기준으로 현재 로그인한 회원을 식별하며,
                    회원 ID, 이메일, 이름, 권한 등의 회원 정보를 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 정보 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않았거나 액세스 토큰이 유효하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회원 정보를 찾을 수 없음"
            )
    })
    ResponseEntity<ApiResponse<GetMeResponse>> getMe(
            @Parameter(hidden = true)
            LoginMember loginMember
    );
}
