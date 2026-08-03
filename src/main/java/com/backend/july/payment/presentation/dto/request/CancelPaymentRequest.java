package com.backend.july.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelPaymentRequest(
        @NotBlank(message = "결제 취소: 취소 사유는 필수 입력값입니다.")
        @Size(max = 200, message = "결제 취소: 취소 사유는 최대 200자까지 입력 가능합니다.")
        String cancelReason
) {
}
