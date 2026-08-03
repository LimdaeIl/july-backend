package com.backend.july.payment.application;

import com.backend.july.payment.domain.Payment;
import com.backend.july.payment.exception.PaymentErrorCode;
import com.backend.july.payment.exception.PaymentException;
import com.backend.july.payment.infrastructure.PaymentRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentFailureRecorder {

    private static final int MAX_FAILURE_REASON_LENGTH = 500;

    private final PaymentRepository paymentRepository;
    private final Clock clock;

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void record(
            Long paymentId,
            String reason
    ) {
        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new PaymentException(
                                        PaymentErrorCode
                                                .PAYMENT_NOT_FOUND
                                )
                        );

        payment.fail(
                normalizeReason(reason),
                LocalDateTime.now(clock)
        );
    }

    private String normalizeReason(
            String reason
    ) {
        if (reason == null || reason.isBlank()) {
            return "토스 결제 승인 요청에 실패했습니다.";
        }

        String trimmedReason = reason.trim();

        if (
                trimmedReason.length() <=
                        MAX_FAILURE_REASON_LENGTH
        ) {
            return trimmedReason;
        }

        return trimmedReason.substring(
                0,
                MAX_FAILURE_REASON_LENGTH
        );
    }
}
