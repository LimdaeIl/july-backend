package com.backend.july.member.application;

import com.backend.july.member.domain.Member;
import com.backend.july.member.exception.MemberErrorCode;
import com.backend.july.member.exception.MemberException;
import com.backend.july.member.infrastructure.MemberRepository;
import com.backend.july.member.presentation.dto.response.WithdrawMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawMemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public WithdrawMemberResponse withdraw(Long memberId, String rawPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        validatePassword(rawPassword, member.getPassword());

        member.withdraw();

        return WithdrawMemberResponse.from(member);
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new MemberException(MemberErrorCode.INVALID_PASSWORD);
        }
    }
}
