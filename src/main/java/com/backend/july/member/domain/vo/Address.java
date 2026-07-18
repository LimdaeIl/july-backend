package com.backend.july.member.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    private Address(String city, String state, String zip, String country) {
        validateCity(city);
        validateState(state);
        validateZip(zip);
        validateCountry(country);

        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
    }

    public static Address of(String city, String state, String zip, String country) {
        return new  Address(city, state, zip, country);
    }

    private void validateCity(String city) {
        if (city == null || city.isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
    }

    private void validateState(String state) {
        if (state == null || state.isEmpty()) {
            throw new IllegalArgumentException("State cannot be null or empty");
        }
    }

    private void validateZip(String zip) {
        if (zip == null || zip.isEmpty()) {
            throw new IllegalArgumentException("Zip cannot be null or empty");
        }
    }

    private void validateCountry(String country) {
        if (country == null || country.isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
    }
}
