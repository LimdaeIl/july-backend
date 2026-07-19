package com.backend.july.member.application;

import com.backend.july.auth.infrastructure.security.principal.LoginMember;
import com.backend.july.member.domain.Member;
import com.backend.july.member.exception.MemberErrorCode;
import com.backend.july.member.exception.MemberException;
import com.backend.july.member.infrastructure.MemberRepository;
import com.backend.july.member.presentation.dto.response.GetMeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GetMeService {

    private final MemberRepository memberRepository;


    @Transactional(readOnly = true)
    public GetMeResponse getMe(LoginMember loginMember) {
        Member member = memberRepository.findById(loginMember.memberId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        return GetMeResponse.from(member);
    }
}
