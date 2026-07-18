package com.backend.july.member.domain.vo;

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
public class Name {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    private Name(String name) {
        validate(name, "Name");

        this.name = name;
    }

    public static Name of(String name) {
        return new Name(name);
    }


    private void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new MemberException(MemberErrorCode.VALIDATE_FIELD, fieldName);
        }
    }


}
