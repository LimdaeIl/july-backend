package com.backend.july.order.domain;

import com.backend.july.common.audit.BaseAuditEntity;
import com.backend.july.member.domain.Member;
import com.backend.july.order.exception.OrderErrorCode;
import com.backend.july.order.exception.OrderException;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v1_orders",
        indexes = {
                @Index(
                        name = "idx_order_number",
                        columnList = "order_number",
                        unique = true
                ),
                @Index(
                        name = "idx_order_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_order_member_status",
                        columnList = "member_id, status"
                ),
                @Index(
                        name = "idx_order_status_expires_at",
                        columnList = "status, expires_at"
                )
        }
)
@Entity
public class PurchaseOrder extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "order_number",
            nullable = false,
            updatable = false,
            length = 50,
            unique = true
    )
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Version
    private Long version;

    private PurchaseOrder(
            String orderNumber,
            Member member,
            LocalDateTime expiresAt
    ) {
        validateOrderNumber(orderNumber);
        validateMember(member);
        validateExpiresAt(expiresAt);

        this.orderNumber = orderNumber;
        this.member = member;
        this.expiresAt = expiresAt;
        this.totalAmount = BigDecimal.ZERO;
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    public static PurchaseOrder create(
            String orderNumber,
            Member member,
            LocalDateTime expiresAt
    ) {
        return new PurchaseOrder(
                orderNumber,
                member,
                expiresAt
        );
    }

    /**
     * 주문 상품을 주문에 연결한다.
     * 양방향 연관관계는 PurchaseOrder에서만 관리한다.
     */
    public void addItem(OrderItem orderItem) {
        validateOrderItem(orderItem);
        validatePendingPaymentStatus();
        validateDuplicateProduct(orderItem);

        orderItem.assignOrder(this);
        this.orderItems.add(orderItem);

        calculateTotalAmount();
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    /**
     * 주문 저장 또는 결제 전에 주문 구성이 유효한지 검증한다.
     */
    public void validateOrderReady() {
        if (orderItems.isEmpty()) {
            throw new OrderException(
                    OrderErrorCode.ORDER_ITEMS_EMPTY
            );
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderException(
                    OrderErrorCode.INVALID_TOTAL_AMOUNT
            );
        }
    }

    /**
     * 결제 승인 완료 후 호출한다.
     */
    public void pay(LocalDateTime paidAt) {
        validatePaidAt(paidAt);
        validatePayable(paidAt);

        this.status = OrderStatus.PAID;
        this.paidAt = paidAt;
    }

    /**
     * 결제 전 주문을 사용자가 취소할 때 호출한다.
     */
    public void cancelPendingOrder(LocalDateTime cancelledAt) {
        validateCancelledAt(cancelledAt);
        validatePendingPaymentStatus();

        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    /**
     * 결제 승인 후 PG 환불까지 성공한 주문을 취소할 때 호출한다.
     * 이 메서드는 일반 주문 취소 API에서 직접 호출하면 안 된다.
     */
    public void cancelPaidOrder(LocalDateTime cancelledAt) {
        validateCancelledAt(cancelledAt);
        validatePaidStatus();

        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    /**
     * 결제 기한이 지난 주문을 만료 처리한다.
     */
    public void expire(LocalDateTime expiredAt) {
        validateCurrentTime(expiredAt);
        validatePendingPaymentStatus();

        if (!isExpired(expiredAt)) {
            throw new OrderException(
                    OrderErrorCode.ORDER_NOT_EXPIRED
            );
        }

        this.status = OrderStatus.EXPIRED;
    }

    public boolean isOwnedBy(Long memberId) {
        if (memberId == null) {
            return false;
        }

        return memberId.equals(member.getId());
    }

    public void validateOwner(Long memberId) {
        if (!isOwnedBy(memberId)) {
            throw new OrderException(
                    OrderErrorCode.ORDER_NOT_OWNED
            );
        }
    }

    public boolean isPendingPayment() {
        return status == OrderStatus.PENDING_PAYMENT;
    }

    public boolean isPaid() {
        return status == OrderStatus.PAID;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    public boolean isExpiredStatus() {
        return status == OrderStatus.EXPIRED;
    }

    /**
     * expiresAt과 같은 시각부터 만료된 것으로 판단한다.
     */
    public boolean isExpired(LocalDateTime now) {
        validateCurrentTime(now);

        return !now.isBefore(expiresAt);
    }

    /**
     * 결제를 수행할 수 있는 주문인지 검증한다.
     */
    public void validatePayable(LocalDateTime now) {
        validateCurrentTime(now);
        validatePendingPaymentStatus();

        if (isExpired(now)) {
            throw new OrderException(
                    OrderErrorCode.ORDER_EXPIRED
            );
        }

        validateOrderReady();
    }

    private void calculateTotalAmount() {
        BigDecimal calculatedAmount = orderItems.stream()
                .map(OrderItem::getLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (calculatedAmount.signum() < 0) {
            throw new OrderException(
                    OrderErrorCode.TOTAL_AMOUNT_OVERFLOW
            );
        }

        this.totalAmount = calculatedAmount;
    }

    private void validateDuplicateProduct(
            OrderItem newOrderItem
    ) {
        Long newProductId = newOrderItem.getProductId();

        boolean duplicated = orderItems.stream()
                .anyMatch(orderItem ->
                        orderItem.hasSameProduct(newProductId)
                );

        if (duplicated) {
            throw new OrderException(
                    OrderErrorCode.DUPLICATE_ORDER_ITEM
            );
        }
    }

    private void validatePendingPaymentStatus() {
        if (!isPendingPayment()) {
            throw new OrderException(
                    OrderErrorCode.INVALID_ORDER_STATUS
            );
        }
    }

    private void validatePaidStatus() {
        if (!isPaid()) {
            throw new OrderException(
                    OrderErrorCode.INVALID_ORDER_STATUS
            );
        }
    }

    private static void validateOrderNumber(
            String orderNumber
    ) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new OrderException(
                    OrderErrorCode.ORDER_NUMBER_REQUIRED
            );
        }
    }

    private static void validateMember(Member member) {
        if (member == null) {
            throw new OrderException(
                    OrderErrorCode.MEMBER_REQUIRED
            );
        }
    }

    private static void validateOrderItem(
            OrderItem orderItem
    ) {
        if (orderItem == null) {
            throw new OrderException(
                    OrderErrorCode.ORDER_ITEM_REQUIRED
            );
        }
    }

    private static void validateExpiresAt(
            LocalDateTime expiresAt
    ) {
        if (expiresAt == null) {
            throw new OrderException(
                    OrderErrorCode.EXPIRES_AT_REQUIRED
            );
        }
    }

    private static void validatePaidAt(
            LocalDateTime paidAt
    ) {
        if (paidAt == null) {
            throw new OrderException(
                    OrderErrorCode.PAID_AT_REQUIRED
            );
        }
    }

    private static void validateCancelledAt(
            LocalDateTime cancelledAt
    ) {
        if (cancelledAt == null) {
            throw new OrderException(
                    OrderErrorCode.CANCELLED_AT_REQUIRED
            );
        }
    }

    private static void validateCurrentTime(
            LocalDateTime now
    ) {
        if (now == null) {
            throw new OrderException(
                    OrderErrorCode.CURRENT_TIME_REQUIRED
            );
        }
    }
}
