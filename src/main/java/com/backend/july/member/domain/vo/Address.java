package com.backend.july.member.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode // 값 비교를 위해 필수
@Embeddable
public class Address {

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "zip", nullable = false, length = 100)
    private String zip;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    private Address(String city, String state, String zip, String country) {
        validate(city, "City");
        validate(state, "State");
        validate(zip, "Zip");
        validate(country, "Country");

        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
    }

    public static Address of(String city, String state, String zip, String country) {
        return new Address(city, state, zip, country);
    }

    private void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
}

