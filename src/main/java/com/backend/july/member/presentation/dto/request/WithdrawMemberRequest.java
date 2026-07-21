package com.backend.july.member.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WithdrawMemberRequest(

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        String password
) {
}
