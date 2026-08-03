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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
@Service
public class ConfirmPaymentService {

    private static final String TOSS_PAYMENT_DONE_STATUS = "DONE";

    private final PurchaseOrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentFailureRecorder paymentFailureRecorder;
    private final Clock clock;

    @Transactional
    public ConfirmPaymentResponse confirm(
            Long memberId,
            ConfirmPaymentRequest request
    ) {
        PurchaseOrder order = findOrder(request.orderId());

        validateOrderOwner(order, memberId);

        order.validatePayable(
                LocalDateTime.now(clock)
        );

        validateRequestedAmount(order, request);

        Payment payment = findReadyPayment(order.getId());

        validatePaymentMatchesOrder(payment, order);

        TossPaymentResponse tossResponse =
                requestTossConfirmation(
                        payment,
                        request
                );

        validateTossResponse(
                tossResponse,
                request,
                order
        );

        LocalDateTime approvedAt =
                convertToServiceTime(
                        tossResponse.approvedAt()
                );

        payment.approve(
                tossResponse.paymentKey(),
                approvedAt
        );

        return createResponse(
                payment,
                order,
                approvedAt
        );
    }

    private TossPaymentResponse requestTossConfirmation(
            Payment payment,
            ConfirmPaymentRequest request
    ) {
        try {
            return tossPaymentsClient.confirm(
                    request.paymentKey(),
                    request.orderId(),
                    request.amount()
            );
        } catch (RestClientResponseException exception) {
            return handleTossResponseException(
                    payment,
                    exception
            );
        } catch (ResourceAccessException exception) {
            /*
             * 타임아웃 또는 네트워크 오류입니다.
             * 실제 토스 승인이 끝났을 가능성이 있으므로
             * FAILED로 확정하지 않습니다.
             */
            throw new PaymentException(
                    PaymentErrorCode
                            .TOSS_PAYMENT_CONFIRM_UNCERTAIN
            );
        } catch (RestClientException exception) {
            /*
             * 응답 변환 실패 등 결과가 불명확한 오류입니다.
             */
            throw new PaymentException(
                    PaymentErrorCode
                            .TOSS_PAYMENT_CONFIRM_UNCERTAIN
            );
        }
    }

    private TossPaymentResponse handleTossResponseException(
            Payment payment,
            RestClientResponseException exception
    ) {
        HttpStatusCode statusCode =
                exception.getStatusCode();

        /*
         * 4xx는 잘못된 paymentKey, 금액 불일치,
         * 이미 처리된 결제 등 명확한 요청 실패로 봅니다.
         */
        if (statusCode.is4xxClientError()) {
            paymentFailureRecorder.record(
                    payment.getId(),
                    createTossFailureReason(exception)
            );

            throw new PaymentException(
                    PaymentErrorCode
                            .TOSS_PAYMENT_CONFIRM_FAILED
            );
        }

        /*
         * 5xx는 토스 내부에서 승인이 수행되었는지
         * 단정하기 어렵기 때문에 FAILED로 저장하지 않습니다.
         */
        throw new PaymentException(
                PaymentErrorCode
                        .TOSS_PAYMENT_CONFIRM_UNCERTAIN
        );
    }

    private String createTossFailureReason(
            RestClientResponseException exception
    ) {
        String responseBody =
                exception.getResponseBodyAsString();

        if (
                responseBody != null &&
                        !responseBody.isBlank()
        ) {
            return responseBody;
        }

        String statusText =
                exception.getStatusText();

        if (
                statusText != null &&
                        !statusText.isBlank()
        ) {
            return statusText;
        }

        return "토스 결제 승인 요청이 거절되었습니다.";
    }

    private PurchaseOrder findOrder(
            String orderNumber
    ) {
        return orderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new OrderException(
                                OrderErrorCode.ORDER_NOT_FOUND
                        )
                );
    }

    private void validateOrderOwner(
            PurchaseOrder order,
            Long memberId
    ) {
        if (!order.isOwnedBy(memberId)) {
            throw new PaymentException(
                    PaymentErrorCode.PAYMENT_ACCESS_DENIED
            );
        }
    }

    private void validateRequestedAmount(
            PurchaseOrder order,
            ConfirmPaymentRequest request
    ) {
        if (
                order.getTotalAmount()
                        .compareTo(request.amount()) != 0
        ) {
            throw new PaymentException(
                    PaymentErrorCode
                            .PAYMENT_AMOUNT_MISMATCH
            );
        }
    }

    private Payment findReadyPayment(
            Long orderId
    ) {
        return paymentRepository
                .findFirstByOrderIdAndStatusOrderByIdDesc(
                        orderId,
                        PaymentStatus.READY
                )
                .orElseThrow(() ->
                        new PaymentException(
                                PaymentErrorCode
                                        .PAYMENT_PREPARATION_NOT_FOUND
                        )
                );
    }

    private void validatePaymentMatchesOrder(
            Payment payment,
            PurchaseOrder order
    ) {
        if (!payment.belongsTo(order.getId())) {
            throw new PaymentException(
                    PaymentErrorCode
                            .PAYMENT_ORDER_MISMATCH
            );
        }

        if (
                payment.getAmount()
                        .compareTo(order.getTotalAmount()) != 0
        ) {
            throw new PaymentException(
                    PaymentErrorCode
                            .PAYMENT_AMOUNT_MISMATCH
            );
        }
    }

    private void validateTossResponse(
            TossPaymentResponse tossResponse,
            ConfirmPaymentRequest request,
            PurchaseOrder order
    ) {
        if (tossResponse == null) {
            throw new PaymentException(
                    PaymentErrorCode
                            .TOSS_PAYMENT_INVALID_RESPONSE
            );
        }

        if (
                !TOSS_PAYMENT_DONE_STATUS.equals(
                        tossResponse.status()
                )
        ) {
            throw new PaymentException(
                    PaymentErrorCode
                            .TOSS_PAYMENT_NOT_DONE
            );
        }

        if (
                !request.paymentKey().equals(
                        tossResponse.paymentKey()
                )
        ) {
            throw new PaymentException(
                    PaymentErrorCode
                            .TOSS_PAYMENT_KEY_MISMATCH
            );
        }

        if (
                !request.orderId().equals(
                        tossResponse.orderId()
                )
        ) {
            throw new PaymentException(
                    PaymentErrorCode
                            .TOSS_ORDER_ID_MISMATCH
            );
        }

        if (
                tossResponse.totalAmount() == null ||
                        tossResponse.totalAmount()
                                .compareTo(
                                        order.getTotalAmount()
                                ) != 0
        ) {
            throw new PaymentException(
                    PaymentErrorCode
                            .TOSS_PAYMENT_AMOUNT_MISMATCH
            );
        }
    }

    private LocalDateTime convertToServiceTime(
            OffsetDateTime approvedAt
    ) {
        if (approvedAt == null) {
            return LocalDateTime.now(clock);
        }

        return approvedAt
                .atZoneSameInstant(clock.getZone())
                .toLocalDateTime();
    }

    private ConfirmPaymentResponse createResponse(
            Payment payment,
            PurchaseOrder order,
            LocalDateTime approvedAt
    ) {
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
