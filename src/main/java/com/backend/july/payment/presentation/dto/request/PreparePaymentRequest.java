package com.backend.july.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PreparePaymentRequest(
        @NotNull
        @Positive
        Long orderId
) {
}
