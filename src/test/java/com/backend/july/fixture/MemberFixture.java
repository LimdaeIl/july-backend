package com.backend.july.fixture;

import com.backend.july.member.domain.Address;
import com.backend.july.member.domain.Member;

public final class MemberFixture {

    private MemberFixture() {
    }

    public static Member member(int sequence) {
        return Member.create(
                "member" + sequence + "@test.com",
                "fixture-password",
                "테스트 회원 " + sequence,
                phone(sequence),
                Address.of(
                        "서울특별시",
                        "강남구",
                        "06236"
                )
        );
    }

    private static String phone(int sequence) {
        return String.format(
                "010%08d",
                sequence % 100_000_000
        );
    }
}
