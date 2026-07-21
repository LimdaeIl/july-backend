package com.backend.july.member.presentation.dto.response;

import com.backend.july.member.domain.Member;
import com.backend.july.member.domain.MemberStatus;

public record WithdrawMemberResponse(
        Long memberId,
        MemberStatus status
) {

    public static WithdrawMemberResponse from(Member member) {
        return new WithdrawMemberResponse(
                member.getId(),
                member.getStatus()
        );
    }
}
