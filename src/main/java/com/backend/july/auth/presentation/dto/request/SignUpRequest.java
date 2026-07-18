package com.backend.july.auth.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "회원가입: 이메일은 필수입니다.")
        @Email(message = "회원가입: 올바른 이메일 형식이 아닙니다.")
        @Size(min = 4, max = 100, message = "회원가입: 이메일은 4자 이상 100자 이하입니다.")
        String email,

        @NotBlank(message = "회원가입: 비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "회원가입: 비밀번호는 8자 이상 16자 이하입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d])[A-Za-z\\d\\W_]{8,20}$",
                message = "회원가입: 비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
        )
        String password,

        @NotBlank(message = "회원가입: 이름은 필수입니다.")
        @Size(min = 2, max = 100, message = "회원가입: 이름은 2자 이상 100자 이하입니다.")
        @Pattern(
                regexp = "^[가-힣A-Za-z0-9]{2,100}$",
                message = "회원가입: 이름은 한글, 영문, 숫자만 사용할 수 있습니다."
        )
        String name,

        @NotBlank(message = "회원가입: 휴대전화번호는 필수입니다.")
        @Size(min = 10, max = 11, message = "회원가입: 휴대전화번호는 10자 이상 11자 이하입니다.")
        @Pattern(
                regexp = "^010\\d{8}$",
                message = "회원가입: 휴대전화번호 형식은 01012345678 이어야 합니다."
        )
        String phone,

        @Valid
        AddressRequest address

) {

}
