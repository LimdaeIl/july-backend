package com.backend.july.auth.application;

import com.backend.july.auth.presentation.dto.request.SignUpRequest;
import com.backend.july.auth.presentation.dto.response.SignUpResponse;
import com.backend.july.member.domain.Address;
import com.backend.july.member.domain.Member;
import com.backend.july.member.exception.MemberErrorCode;
import com.backend.july.member.exception.MemberException;
import com.backend.july.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SignUpService {

    private final MemberRepository memberRepository;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(MemberErrorCode.MEMBER_ALREADY_EXISTS);
        }

        if (memberRepository.existsByPhone(request.phone())) {
            throw new MemberException(MemberErrorCode.MEMBER_PHONE_ALREADY_EXISTS);
        }

        Member member = Member.create(
                request.email(),
                request.password(),
                request.name(),
                request.phone(),
                Address.of(
                        request.address().city(),
                        request.address().state(),
                        request.address().zip()
                )
        );

        memberRepository.save(member);

        return SignUpResponse.from(member);
    }
}
