package com.backend.july.member.presentation.dto.request;

import com.backend.july.member.domain.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAddressRequest(

        @NotBlank(message = "도시는 필수입니다.")
        @Size(max = 100, message = "도시는 100자 이하여야 합니다.")
        String city,

        @NotBlank(message = "지역은 필수입니다.")
        @Size(max = 100, message = "지역은 100자 이하여야 합니다.")
        String state,

        @NotBlank(message = "우편번호는 필수입니다.")
        @Size(max = 20, message = "우편번호는 20자 이하여야 합니다.")
        String zip
) {

    public Address toAddress() {
        return Address.of(city, state, zip);
    }
}
