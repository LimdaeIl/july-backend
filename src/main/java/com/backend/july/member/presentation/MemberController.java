package com.backend.july.member.presentation;

import com.backend.july.auth.infrastructure.security.principal.LoginMember;
import com.backend.july.common.response.ApiResponse;
import com.backend.july.member.application.GetMeService;
import com.backend.july.member.presentation.dto.response.GetMeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@RestController
public class MemberController implements MemberControllerDocs {

    private final GetMeService getMeService;

    @GetMapping
    public ResponseEntity<ApiResponse<GetMeResponse>> getMe(
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        GetMeResponse response = getMeService.getMe(loginMember);
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "내 정보 조회: 내 정보 정보 조회에 성공했습니다.",
                        response)
        );
    }
}
