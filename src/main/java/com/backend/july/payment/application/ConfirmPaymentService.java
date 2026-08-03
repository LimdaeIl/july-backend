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
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
@Service
public class ConfirmPaymentService {

    private static final String TOSS_PAYMENT_DONE_STATUS = "DONE";

    private final PurchaseOrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentFailureRecorder paymentFailureRecorder;
    private final PaymentConfirmProcessor paymentConfirmProcessor; // 추가
    private final Clock clock;

    // @Transactional 제거: 외부 HTTP 통신 중 DB 커넥션 점유 방지
    public ConfirmPaymentResponse confirm(
            Long memberId,
            ConfirmPaymentRequest request
    ) {
        // 1. 사전 검증 및 조회 (Non-Transactional Read)
        PurchaseOrder order = findOrder(request.orderId());
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

        // 4. 승인 결과 DB 반영 (Transactional)
        return paymentConfirmProcessor.processApproval(
                payment,
                order,
                tossResponse.paymentKey(),
                approvedAt
        );
    }


    // 서비스 내부에서는 try-catch가 거의 사라짐
    private TossPaymentResponse requestTossConfirmation(Payment payment, ConfirmPaymentRequest request) {
        try {
            return tossPaymentsClient.confirm(
                    request.paymentKey(),
                    request.orderId(),
                    request.amount()
            );
        } catch (PaymentException exception) {
            // 4xx 실패일 때만 기록 남기기
            if (exception.getErrorCode() == PaymentErrorCode.TOSS_PAYMENT_CONFIRM_FAILED) {
                paymentFailureRecorder.record(payment.getId(), "토스 결제 승인 실패");
            }
            throw exception; // 예외 재던지기
        }
    }

    private TossPaymentResponse handleTossResponseException(
            Payment payment,
            RestClientResponseException exception
    ) {
        HttpStatusCode statusCode = exception.getStatusCode();

        if (statusCode.is4xxClientError()) {
            // 4xx는 트랜잭션 밖이므로 DB 기록이 롤백될 위험 없이 잘 기록됨
            paymentFailureRecorder.record(
                    payment.getId(),
                    createTossFailureReason(exception)
            );

            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_CONFIRM_FAILED);
        }

        throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_CONFIRM_UNCERTAIN);
    }

    private String createTossFailureReason(RestClientResponseException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (!responseBody.isBlank()) {
            return responseBody;
        }

        String statusText = exception.getStatusText();
        if (!statusText.isBlank()) {
            return statusText;
        }

        return "토스 결제 승인 요청이 거절되었습니다.";
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
        return paymentRepository.findFirstByOrderIdAndStatusOrderByIdDesc(orderId,
                        PaymentStatus.READY)
                .orElseThrow(
                        () -> new PaymentException(PaymentErrorCode.PAYMENT_PREPARATION_NOT_FOUND));
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
