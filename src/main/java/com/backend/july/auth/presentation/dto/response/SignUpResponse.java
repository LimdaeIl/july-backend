package com.backend.july.auth.presentation.dto.response;

import com.backend.july.member.domain.Member;

public record SignUpResponse(
        Long id,
        String email,
        String name,
        String phone,
        String role,
        String status,
        String city,
        String state,
        String zip

) {

    public static SignUpResponse from(Member member) {
        return new SignUpResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getRole().name(),
                member.getStatus().name(),
                member.getAddress().getCity(),
                member.getAddress().getState(),
                member.getAddress().getZip()
        );
    }
}
