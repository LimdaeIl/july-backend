package com.backend.july.payment.application;

import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.order.exception.OrderErrorCode;
import com.backend.july.order.exception.OrderException;
import com.backend.july.order.infrastructure.PurchaseOrderRepository;
import com.backend.july.payment.domain.Payment;
import com.backend.july.payment.exception.PaymentErrorCode;
import com.backend.july.payment.exception.PaymentException;
import com.backend.july.payment.infrastructure.PaymentRepository;
import com.backend.july.payment.presentation.dto.response.ConfirmPaymentResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PaymentConfirmProcessor {

    private final PaymentRepository paymentRepository;
    private final PurchaseOrderRepository orderRepository;

    @Transactional
    public ConfirmPaymentResponse processApproval(
            Long paymentId,
            Long orderId,
            String paymentKey,
            LocalDateTime approvedAt
    ) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND)
                );

        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderException(OrderErrorCode.ORDER_NOT_FOUND)
                );

        /*
         * Payment.approve() 내부에서 다음 작업을 모두 처리한다.
         * 1. 주문 결제 가능 상태 검증
         * 2. 주문 상태를 PAID로 변경
         * 3. Payment 상태를 APPROVED로 변경
         */
        payment.approve(paymentKey, approvedAt);

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
