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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Embeddable
public class Address {

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "zip", nullable = false, length = 100)
    private String zip;

    private Address(String city, String state, String zip) {
        validate(city, "City");
        validate(state, "State");
        validate(zip, "Zip");

        this.city = city;
        this.state = state;
        this.zip = zip;}

    public static Address of(String city, String state, String zip) {
        return new Address(city, state, zip);
    }

    private void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new MemberException(MemberErrorCode.VALIDATE_FIELD, fieldName);
        }
    }
}

