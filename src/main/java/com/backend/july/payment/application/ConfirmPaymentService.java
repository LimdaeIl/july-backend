package com.backend.july.payment.application;

import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.order.exception.OrderErrorCode;
import com.backend.july.order.exception.OrderException;
import com.backend.july.order.infrastructure.PurchaseOrderRepository;
import com.backend.july.payment.domain.Payment;
import com.backend.july.payment.domain.PaymentStatus;
import com.backend.july.payment.exception.PaymentErrorCode;
import com.backend.july.payment.exception.PaymentException;
import com.backend.july.payment.infrastructure.PaymentRepository;
import com.backend.july.payment.infrastructure.toss.TossPaymentsClient;
import com.backend.july.payment.infrastructure.toss.dto.TossPaymentResponse;
import com.backend.july.payment.presentation.dto.request.ConfirmPaymentRequest;
import com.backend.july.payment.presentation.dto.response.ConfirmPaymentResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConfirmPaymentService {

    private static final String TOSS_PAYMENT_DONE_STATUS = "DONE";

    private final PurchaseOrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentFailureRecorder paymentFailureRecorder;
    private final PaymentConfirmProcessor paymentConfirmProcessor;
    private final Clock clock;

    public ConfirmPaymentResponse confirm(
            Long memberId,
            ConfirmPaymentRequest request
    ) {
        // 1. 사전 검증 및 조회 (Non-Transactional Read)
        PurchaseOrder order = findOrder(request.orderId());

        log.info(
                "결제 승인 사전 검증 - orderId={}, orderNumber={}, orderStatus={}, amount={}",
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount()
        );

        validateOrderOwner(order, memberId);
        order.validatePayable(LocalDateTime.now(clock));
        validateRequestedAmount(order, request);

        Payment payment = findReadyPayment(order.getId());
        validatePaymentMatchesOrder(payment, order);

        // 2. 외부 PG사 승인 요청 (Network I/O)
        TossPaymentResponse tossResponse = requestTossConfirmation(
                payment,
                request
        );

        // 3. 토스 응답 데이터 검증
        validateTossResponse(tossResponse, request, order);

        LocalDateTime approvedAt = convertToServiceTime(tossResponse.approvedAt());

        // 4. 승인 결과 DB 반영 (Transactional - ID만 넘겨서 새로 조회 후 반영)
        return paymentConfirmProcessor.processApproval(
                payment.getId(),
                order.getId(),
                tossResponse.paymentKey(),
                approvedAt
        );
    }

    private TossPaymentResponse requestTossConfirmation(Payment payment, ConfirmPaymentRequest request) {
        try {
            return tossPaymentsClient.confirm(
                    request.paymentKey(),
                    request.orderId(),
                    request.amount()
            );
        } catch (PaymentException exception) {
            // 4xx 실패일 때만 DB에 실패 기록 남기기 (RequiresNew 적용되어 있음)
            if (exception.getErrorCode() == PaymentErrorCode.TOSS_PAYMENT_CONFIRM_FAILED) {
                paymentFailureRecorder.record(payment.getId(), "토스 결제 승인 실패");
            }
            throw exception;
        }
    }

    private PurchaseOrder findOrder(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    private void validateOrderOwner(PurchaseOrder order, Long memberId) {
        if (!order.isOwnedBy(memberId)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_ACCESS_DENIED);
        }
    }

    private void validateRequestedAmount(PurchaseOrder order, ConfirmPaymentRequest request) {
        if (order.getTotalAmount().compareTo(request.amount()) != 0) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private Payment findReadyPayment(Long orderId) {
        return paymentRepository.findFirstByOrderIdAndStatusOrderByIdDesc(orderId, PaymentStatus.READY)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_PREPARATION_NOT_FOUND));
    }

    private void validatePaymentMatchesOrder(Payment payment, PurchaseOrder order) {
        if (!payment.belongsTo(order.getId())) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_ORDER_MISMATCH);
        }

        if (payment.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void validateTossResponse(
            TossPaymentResponse tossResponse,
            ConfirmPaymentRequest request,
            PurchaseOrder order
    ) {
        if (tossResponse == null) {
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_INVALID_RESPONSE);
        }

        if (!TOSS_PAYMENT_DONE_STATUS.equals(tossResponse.status())) {
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_NOT_DONE);
        }

        if (!request.paymentKey().equals(tossResponse.paymentKey())) {
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_KEY_MISMATCH);
        }

        if (!request.orderId().equals(tossResponse.orderId())) {
            throw new PaymentException(PaymentErrorCode.TOSS_ORDER_ID_MISMATCH);
        }

        if (tossResponse.totalAmount() == null ||
                tossResponse.totalAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private LocalDateTime convertToServiceTime(OffsetDateTime approvedAt) {
        if (approvedAt == null) {
            return LocalDateTime.now(clock);
        }

        return approvedAt.atZoneSameInstant(clock.getZone()).toLocalDateTime();
    }
}
