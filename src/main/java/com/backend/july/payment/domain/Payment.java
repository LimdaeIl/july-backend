package com.backend.july.payment.domain;

import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.payment.exception.PaymentErrorCode;
import com.backend.july.payment.exception.PaymentException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v1_payments",
        indexes = {
                @Index(name = "idx_payment_order_id", columnList = "order_id"),
                @Index(name = "idx_payment_key", columnList = "payment_key", unique = true),
                @Index(name = "idx_payment_order_status", columnList = "order_id, status")
        }
)
@Entity
public class Payment {

    private static final int PAYMENT_KEY_MAX_LENGTH = 200;
    private static final int REASON_MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 주문 한 건에 여러 결제 시도가 존재할 수 있다.
     * <p>
     * 예: Payment 1 = FAILED Payment 2 = APPROVED
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;

    @Column(name = "payment_key", length = PAYMENT_KEY_MAX_LENGTH, unique = true)
    private String paymentKey;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(
            name = "failure_reason",
            length = REASON_MAX_LENGTH
    )
    private String failureReason;

    @Column(
            name = "cancellation_reason",
            length = REASON_MAX_LENGTH
    )
    private String cancellationReason;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Version
    private Long version;

    private Payment(PurchaseOrder order, BigDecimal amount) {
        validateOrder(order);
        validateAmount(amount);
        validateOrderAmount(order, amount);

        this.order = order;
        this.amount = amount;
        this.status = PaymentStatus.READY;
    }

    public static Payment create(PurchaseOrder order, BigDecimal amount) {
        return new Payment(order, amount);
    }

    /**
     * PG 결제 승인이 성공한 경우 호출한다.
     */
    public void approve(String paymentKey, LocalDateTime approvedAt) {
        validateReadyStatus();
        validateApprovedAt(approvedAt);

        String validatedPaymentKey = validatePaymentKey(paymentKey);

        /*
         * 주문 상태와 만료 시간을 먼저 검증한다.
         * 주문 결제가 불가능하면 Payment도 승인 상태로 변경되지 않는다.
         */
        order.pay(approvedAt);

        this.paymentKey = validatedPaymentKey;
        this.status = PaymentStatus.APPROVED;
        this.approvedAt = approvedAt;
    }

    /**
     * 현재 결제 시도만 실패 처리한다.
     * <p>
     * 주문은 PENDING_PAYMENT 상태를 유지하여 만료 전 다른 결제를 다시 시도할 수 있게 한다.
     */
    public void fail(String failureReason, LocalDateTime failedAt) {
        validateReadyStatus();
        validateFailedAt(failedAt);

        this.failureReason = validateFailureReason(failureReason);
        this.status = PaymentStatus.FAILED;
        this.failedAt = failedAt;
    }

    /**
     * PG 결제 취소 또는 환불 성공 후 호출한다.
     * <p>
     * 주문 상태 변경과 재고 복구는 애플리케이션 서비스에서 처리한다.
     */
    public void cancel(String cancellationReason, LocalDateTime cancelledAt) {
        validateApprovedStatus();
        validateCancelledAt(cancelledAt);

        this.cancellationReason = validateCancellationReason(cancellationReason);
        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    public boolean belongsTo(Long orderId) {
        return orderId != null && orderId.equals(order.getId());
    }

    public boolean hasPaymentKey(String paymentKey) {
        return paymentKey != null
                && paymentKey.equals(this.paymentKey);
    }

    public boolean isReady() {
        return status == PaymentStatus.READY;
    }

    public boolean isApproved() {
        return status == PaymentStatus.APPROVED;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public boolean isCancelled() {
        return status == PaymentStatus.CANCELLED;
    }

    private void validateReadyStatus() {
        if (!isReady()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }
    }

    private void validateApprovedStatus() {
        if (!isApproved()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }
    }

    private static void validateOrder(PurchaseOrder order) {
        if (order == null) {
            throw new PaymentException(PaymentErrorCode.ORDER_REQUIRED);
        }
    }

    private static String validatePaymentKey(String paymentKey) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_KEY_REQUIRED);
        }

        String trimmedPaymentKey = paymentKey.trim();

        if (trimmedPaymentKey.length() > PAYMENT_KEY_MAX_LENGTH) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_KEY);
        }

        return trimmedPaymentKey;
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_REQUIRED);
        }

        if (amount.signum() <= 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }

    private static void validateOrderAmount(PurchaseOrder order, BigDecimal amount) {
        if (order.getTotalAmount().compareTo(amount) != 0) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private static String validateFailureReason(
            String failureReason
    ) {
        if (failureReason == null || failureReason.isBlank()) {
            throw new PaymentException(PaymentErrorCode.FAILURE_REASON_REQUIRED);
        }

        String trimmedFailureReason =
                failureReason.trim();

        if (trimmedFailureReason.length() > REASON_MAX_LENGTH) {
            throw new PaymentException(PaymentErrorCode.INVALID_FAILURE_REASON);
        }

        return trimmedFailureReason;
    }

    private static String validateCancellationReason(
            String cancellationReason
    ) {
        if (cancellationReason == null || cancellationReason.isBlank()) {
            throw new PaymentException(PaymentErrorCode.CANCELLATION_REASON_REQUIRED);
        }

        String trimmedCancellationReason =
                cancellationReason.trim();

        if (trimmedCancellationReason.length() > REASON_MAX_LENGTH) {
            throw new PaymentException(PaymentErrorCode.INVALID_CANCELLATION_REASON);
        }

        return trimmedCancellationReason;
    }

    private static void validateApprovedAt(LocalDateTime approvedAt) {
        if (approvedAt == null) {
            throw new PaymentException(PaymentErrorCode.APPROVED_AT_REQUIRED);
        }
    }

    private static void validateFailedAt(LocalDateTime failedAt) {
        if (failedAt == null) {
            throw new PaymentException(PaymentErrorCode.FAILED_AT_REQUIRED);
        }
    }

    private static void validateCancelledAt(LocalDateTime cancelledAt) {
        if (cancelledAt == null) {
            throw new PaymentException(PaymentErrorCode.CANCELLED_AT_REQUIRED);
        }
    }
}