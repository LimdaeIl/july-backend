package com.backend.july.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ConfirmPaymentRequest(
        @NotBlank(message = "결제 확인: 결제 키(paymentKey)는 필수 입력값입니다.")
        String paymentKey,

        @NotBlank(message = "결제 확인: 주문 번호(orderId)는 필수 입력값입니다.")
        String orderId,

        @NotNull(message = "결제 확인: 결제 금액은 필수 입력값입니다.")
        @Positive(message = "결제 확인: 결제 금액은 0보다 큰 양수여야 합니다.")
        BigDecimal amount
) {
}
