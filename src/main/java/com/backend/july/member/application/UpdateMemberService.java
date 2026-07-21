package com.backend.july.member.application;

import com.backend.july.member.domain.Address;
import com.backend.july.member.domain.Member;
import com.backend.july.member.exception.MemberErrorCode;
import com.backend.july.member.exception.MemberException;
import com.backend.july.member.infrastructure.MemberRepository;
import com.backend.july.member.presentation.dto.request.UpdateMemberRequest;
import com.backend.july.member.presentation.dto.response.UpdateMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateMemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public UpdateMemberResponse update(Long memberId, UpdateMemberRequest request) {
        validateRequest(request);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        String updatedName = request.name() != null
                ? request.name()
                : member.getName();

        String updatedPhone = request.phone() != null
                ? request.phone()
                : member.getPhone();

        Address updatedAddress = request.address() != null
                ? request.address().toAddress()
                : member.getAddress();

        validateDuplicatePhone(member, updatedPhone);

        member.updateProfile(updatedName, updatedPhone, updatedAddress);

        return UpdateMemberResponse.from(member);
    }

    private void validateRequest(UpdateMemberRequest request) {
        if (request.hasNoChanges()) {
            throw new MemberException(MemberErrorCode.NO_PROFILE_CHANGES);
        }
    }

    private void validateDuplicatePhone(Member member, String updatedPhone) {
        if (member.hasSamePhone(updatedPhone)) {
            return;
        }

        if (memberRepository.existsByPhoneAndIdNot(updatedPhone, member.getId())) {
            throw new MemberException(MemberErrorCode.DUPLICATE_PHONE);
        }
    }
}
