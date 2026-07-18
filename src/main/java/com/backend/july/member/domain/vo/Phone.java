package com.backend.july.member.domain.vo;

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
public class Phone {

    @Column(name = "phone", nullable = false, length = 11)
    private String phone;

    private Phone(String phone) {
        validate(phone, "Phone");

        this.phone = phone;
    }

    public static Phone of(String phone) {
        return new Phone(phone);
    }


    private void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
}
