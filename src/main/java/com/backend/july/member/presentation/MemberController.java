package com.backend.july.member.presentation;

import com.backend.july.auth.infrastructure.security.principal.LoginMember;
import com.backend.july.common.response.ApiResponse;
import com.backend.july.member.application.GetMeService;
import com.backend.july.member.application.UpdateMemberService;
import com.backend.july.member.application.WithdrawMemberService;
import com.backend.july.member.presentation.dto.request.UpdateMemberRequest;
import com.backend.july.member.presentation.dto.request.WithdrawMemberRequest;
import com.backend.july.member.presentation.dto.response.GetMeResponse;
import com.backend.july.member.presentation.dto.response.UpdateMemberResponse;
import com.backend.july.member.presentation.dto.response.WithdrawMemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@RestController
public class MemberController implements MemberControllerDocs {

    private final GetMeService getMeService;
    private final UpdateMemberService updateMemberService;
    private final WithdrawMemberService withdrawMemberService;

    @GetMapping
    public ResponseEntity<ApiResponse<GetMeResponse>> getMe(
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        GetMeResponse response = getMeService.getMe(loginMember);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "내 정보 조회에 성공했습니다.",
                        response
                )
        );
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UpdateMemberResponse>> updateMember(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody UpdateMemberRequest request
    ) {
        UpdateMemberResponse response = updateMemberService.update(loginMember.memberId(), request);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "회원 정보 수정에 성공했습니다.",
                        response
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<WithdrawMemberResponse>> withdrawMember(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody WithdrawMemberRequest request
    ) {
        WithdrawMemberResponse response = withdrawMemberService.withdraw(loginMember.memberId(), request.password());

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "회원 탈퇴에 성공했습니다.",
                        response
                )
        );
    }
}