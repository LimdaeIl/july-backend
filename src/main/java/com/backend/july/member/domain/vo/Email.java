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
public class Email {

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    private Email(String email) {
        validateEmail(email);

        this.email = email;
    }

    public static Email of(String email) {
        return new Email(email);
    }

    private void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }
}
