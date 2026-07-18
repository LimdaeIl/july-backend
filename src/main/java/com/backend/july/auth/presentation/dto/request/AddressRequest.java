package com.backend.july.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(

        @NotBlank(message = "회원가입: 도시는 필수입니다.")
        @Size(max = 100, message = "회원가입: 도시는 100자 이하입니다.")
        String city,

        @NotBlank(message = "회원가입: 시/도는 필수입니다.")
        @Size(max = 100, message = "회원가입: 시/도는 100자 이하입니다.")
        String state,

        @NotBlank(message = "회원가입: 우편번호는 필수입니다.")
        @Size(max = 100, message = "회원가입: 우편번호는 100자 이하입니다.")
        String zip
) {

}
