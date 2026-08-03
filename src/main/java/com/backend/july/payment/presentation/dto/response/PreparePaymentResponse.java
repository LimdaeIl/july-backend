package com.backend.july.payment.presentation.dto.response;

import java.math.BigDecimal;

public record PreparePaymentResponse(
        Long paymentId,
        Long orderId,
        String orderNumber,
        String orderName,
        BigDecimal amount
) {
    public static PreparePaymentResponse of(
            Long paymentId,
            Long orderId,
            String orderNumber,
            String orderName,
            BigDecimal amount
    ) {
        return new PreparePaymentResponse(
                paymentId,
                orderId,
                orderNumber,
                orderName,
                amount
        );
    }
}
