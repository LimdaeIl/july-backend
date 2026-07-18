package com.backend.july.auth.infrastructure.security.principal;

public record LoginMember(
        Long memberId,
        String role
) {

}
