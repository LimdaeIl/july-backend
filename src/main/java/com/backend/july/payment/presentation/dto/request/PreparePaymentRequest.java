package com.backend.july.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PreparePaymentRequest(
        @NotNull(message = "결제 준비: 주문 ID는 필수 입력값입니다.")
        @Positive(message = "결제 준비: 주문 ID는 올바른 양수 형태여야 합니다.")
        Long orderId
) {
}
