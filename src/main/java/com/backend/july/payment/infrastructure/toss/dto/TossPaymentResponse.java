package com.backend.july.payment.infrastructure.toss.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        String method,
        BigDecimal totalAmount,
        BigDecimal balanceAmount,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt
) {
}
