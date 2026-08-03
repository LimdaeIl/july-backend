package com.backend.july.payment.application;

import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.payment.domain.Payment;
import com.backend.july.payment.exception.PaymentErrorCode;
import com.backend.july.payment.exception.PaymentException;
import com.backend.july.payment.infrastructure.PaymentRepository;
import com.backend.july.payment.infrastructure.toss.TossPaymentsClient;
import com.backend.july.payment.infrastructure.toss.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CancelPaymentService {

    private static final String TOSS_CANCELLED_STATUS = "CANCELED";

    private final PaymentRepository paymentRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentCancelProcessor paymentCancelProcessor;

    public void cancel(Long memberId, Long paymentId, String cancelReason) {
        // 1. 검증 및 조회 (Non-Transactional)
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        PurchaseOrder order = payment.getOrder();

        if (!order.isOwnedBy(memberId)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_ACCESS_DENIED);
        }

        if (!payment.isApproved()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        // 2. 외부 PG사 취소 요청 (Network I/O - DB 커넥션 미점유)
        TossPaymentResponse tossResponse = tossPaymentsClient.cancel(payment.getPaymentKey(), cancelReason);

        if (tossResponse == null || !TOSS_CANCELLED_STATUS.equals(tossResponse.status())) {
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_NOT_CANCELLED);
        }

        // 3. 로컬 DB 상태 변경 및 재고 복구 (별도 트랜잭션 컴포넌트 호출)
        paymentCancelProcessor.processLocalCancellation(payment, order, cancelReason);
    }
}
