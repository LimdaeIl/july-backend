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
import jakarta.persistence.OneToOne;
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
                @Index(name = "idx_payment_order_id", columnList = "order_id", unique = true),
                @Index(name = "idx_payment_key", columnList = "payment_key", unique = true),
                @Index(name = "idx_payment_status", columnList = "status")
        }
)
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private PurchaseOrder order;

    @Column(name = "payment_key", length = 200, unique = true)
    private String paymentKey;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "cancellation_reason", length = 500)
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

    public void approve(String paymentKey, LocalDateTime approvedAt) {
        validateReadyStatus();
        validatePaymentKey(paymentKey);
        validateApprovedAt(approvedAt);

        this.paymentKey = paymentKey;
        this.status = PaymentStatus.APPROVED;
        this.approvedAt = approvedAt;
        this.failureReason = null;
        this.failedAt = null;

        order.pay();
    }

    public void fail(String failureReason, LocalDateTime failedAt) {
        validateReadyStatus();
        validateFailureReason(failureReason);
        validateFailedAt(failedAt);

        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.failedAt = failedAt;

        order.fail();
    }

    public void cancel(String cancellationReason, LocalDateTime cancelledAt) {
        validateApprovedStatus();
        validateCancellationReason(cancellationReason);
        validateCancelledAt(cancelledAt);

        this.status = PaymentStatus.CANCELLED;
        this.cancellationReason = cancellationReason;
        this.cancelledAt = cancelledAt;

        order.cancel();
    }

    public boolean belongsTo(Long orderId) {
        return orderId != null && orderId.equals(order.getId());
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

    private static void validatePaymentKey(String paymentKey) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_KEY_REQUIRED);
        }
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

    private static void validateFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            throw new PaymentException(PaymentErrorCode.FAILURE_REASON_REQUIRED);
        }
    }

    private static void validateCancellationReason(String cancellationReason) {
        if (cancellationReason == null || cancellationReason.isBlank()) {
            throw new PaymentException(PaymentErrorCode.CANCELLATION_REASON_REQUIRED);
        }
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
