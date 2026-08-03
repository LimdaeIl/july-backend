package com.backend.july.payment.application;

import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.payment.domain.Payment;
import com.backend.july.payment.presentation.dto.response.ConfirmPaymentResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PaymentConfirmProcessor {

    @Transactional
    public ConfirmPaymentResponse processApproval(
            Payment payment,
            PurchaseOrder order,
            String paymentKey,
            LocalDateTime approvedAt
    ) {
        // 1. Payment 승인 처리
        payment.approve(paymentKey, approvedAt);

        // 2. 주문 결제 완료 상태 변경 (필요한 경우 order 도메인 메서드 호출)
        // order.completePayment(approvedAt);

        return new ConfirmPaymentResponse(
                payment.getId(),
                order.getId(),
                order.getOrderNumber(),
                payment.getPaymentKey(),
                payment.getAmount(),
                payment.getStatus().name(),
                order.getStatus().name(),
                approvedAt
        );
    }
}
