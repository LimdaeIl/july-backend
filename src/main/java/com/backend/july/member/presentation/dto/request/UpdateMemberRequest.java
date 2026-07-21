package com.backend.july.member.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMemberRequest(

        @Size(min = 1, max = 100, message = "이름은 1자 이상 100자 이하여야 합니다.")
        String name,

        @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        String phone,

        @Valid
        UpdateAddressRequest address
) {

    public boolean hasNoChanges() {
        return name == null && phone == null && address == null;
    }
}
