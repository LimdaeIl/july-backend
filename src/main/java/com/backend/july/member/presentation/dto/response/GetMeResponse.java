package com.backend.july.member.presentation.dto.response;

import com.backend.july.member.domain.Member;

public record GetMeResponse(
        Long id,
        String email,
        String name,
        String phone,
        Address address
) {

    public static GetMeResponse from(Member member) {
        return new GetMeResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                Address.from(member)
        );
    }

    private record Address(
            String city,
            String state,
            String zip
    ) {
        public static Address from(Member member) {
            return new Address(
                    member.getAddress().getCity(),
                    member.getAddress().getState(),
                    member.getAddress().getZip()
            );
        }

    }
}
