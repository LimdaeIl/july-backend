package com.backend.july.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelPaymentRequest(
        @NotBlank
        @Size(max = 200)
        String cancelReason
) {
}
