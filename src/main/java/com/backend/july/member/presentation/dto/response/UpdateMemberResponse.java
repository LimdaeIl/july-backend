package com.backend.july.member.presentation.dto.response;

import com.backend.july.member.domain.Address;
import com.backend.july.member.domain.Member;
import com.backend.july.member.domain.MemberStatus;

public record UpdateMemberResponse(
        Long memberId,
        String email,
        String name,
        String phone,
        AddressResponse address,
        MemberStatus status
) {

    public static UpdateMemberResponse from(Member member) {
        return new UpdateMemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                AddressResponse.from(member.getAddress()),
                member.getStatus()
        );
    }

    public record AddressResponse(String city, String state, String zip) {

        public static AddressResponse from(Address address) {
            return new AddressResponse(
                    address.getCity(),
                    address.getState(),
                    address.getZip()
            );
        }
    }
}
