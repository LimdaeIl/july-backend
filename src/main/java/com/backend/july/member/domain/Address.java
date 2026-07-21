package com.backend.july.member.domain;

import com.backend.july.member.exception.MemberErrorCode;
import com.backend.july.member.exception.MemberException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Address {

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "zip", nullable = false, length = 20)
    private String zip;

    private Address(String city, String state, String zip) {
        this.city = validate(city, "도시");
        this.state = validate(state, "지역");
        this.zip = validate(zip, "우편번호");
    }

    public static Address of(String city, String state, String zip) {
        return new Address(city, state, zip);
    }

    private String validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new MemberException(MemberErrorCode.VALIDATE_FIELD, fieldName);
        }

        return value;
    }
}