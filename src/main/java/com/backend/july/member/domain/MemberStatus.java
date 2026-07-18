package com.backend.july.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {
    ACTIVATE("활성"),
    DEACTIVATE("비활성"),
    WITHDRAWAL("탈퇴");

    private final String description;
}
