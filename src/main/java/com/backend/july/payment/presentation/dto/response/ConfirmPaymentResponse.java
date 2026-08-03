package com.backend.july.payment.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConfirmPaymentResponse(
        Long paymentId,
        Long orderId,
        String orderNumber,
        String paymentKey,
        BigDecimal amount,
        String paymentStatus,
        String orderStatus,
        LocalDateTime approvedAt
) {
}
