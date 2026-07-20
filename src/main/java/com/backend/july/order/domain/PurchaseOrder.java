package com.backend.july.order.domain;

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
                )
        }
)
@Entity
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, updatable = false, length = 50, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    // PurchaseOrder가 연관관계의 주인이 아니며, OrderItem의 order 필드가 FK를 관리한다.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> orderItems = new ArrayList<>();

    @Version
    private Long version;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    private PurchaseOrder(String orderNumber, Member member, LocalDateTime expiresAt) {
        validateOrderNumber(orderNumber);
        validateMember(member);
        validateExpiresAt(expiresAt);

        this.orderNumber = orderNumber;
        this.member = member;
        this.expiresAt = expiresAt;
        this.totalAmount = BigDecimal.ZERO;
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    public static PurchaseOrder create(String orderNumber, Member member, LocalDateTime expiresAt) {
        return new PurchaseOrder(orderNumber, member, expiresAt);
    }

    /**
     * OrderItem 생성 후 주문에 연결할 때 사용한다.
     * 양방향 연관관계 편의 메서드는 PurchaseOrder 한 곳에서 관리한다.
     */
    public void addItem(OrderItem orderItem) {
        validateOrderItem(orderItem);
        validatePendingPaymentStatus();
        validateDuplicateProduct(orderItem);

        orderItem.assignOrder(this);
        this.orderItems.add(orderItem);

        calculateTotalAmount();
    }

    /**
     * 필요하지 않다면 외부에서 컬렉션을 직접 수정하지 못하게 한다.
     */
    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    /**
     * 주문 생성을 마치기 전 최소 한 개 이상의 상품이 있는지 검증한다.
     * 주문 저장 직전에 서비스에서 호출할 수 있다.
     */
    public void validateOrderReady() {
        if (orderItems.isEmpty()) {
            throw new OrderException(OrderErrorCode.ORDER_ITEMS_EMPTY);
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderException(
                    OrderErrorCode.INVALID_TOTAL_AMOUNT
            );
        }
    }


     // 결제 승인 완료 후 주문 상태를 변경한다.
    public void pay() {
        validatePendingPaymentStatus();
        validateOrderReady();

        this.status = OrderStatus.PAID;
    }

    /**
     * 주문을 취소한다.
     * 결제 대기 또는 결제 완료 주문만 취소할 수 있도록 제한한다.
     */
    public void cancel(LocalDateTime cancelledAt) {
        if (cancelledAt == null) {
            throw new OrderException(OrderErrorCode.CANCELLED_AT_REQUIRED);
        }

        validatePendingPaymentStatus();

        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    /**
     * 재고 차감, 주문 생성 또는 결제 처리 실패 상태를 표현한다.
     */
    public void fail() {
        validatePendingPaymentStatus();

        this.status = OrderStatus.FAILED;
    }

    public boolean isOwnedBy(Long memberId) {
        if (memberId == null) {
            return false;
        }

        return memberId.equals(member.getId());
    }

    public void validateOwner(Long memberId) {
        if (!isOwnedBy(memberId)) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_OWNED);
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

    private void calculateTotalAmount() {
        try {
            this.totalAmount = orderItems.stream()
                    .map(OrderItem::getLineAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (ArithmeticException exception) {
            throw new OrderException(OrderErrorCode.TOTAL_AMOUNT_OVERFLOW);
        }
    }

    private void validateDuplicateProduct(OrderItem newOrderItem) {
        Long newProductId = newOrderItem.getProductId();

        boolean duplicated = this.orderItems.stream()
                .anyMatch(orderItem -> orderItem.hasSameProduct(newProductId));

        if (duplicated) {
            throw new OrderException(OrderErrorCode.DUPLICATE_ORDER_ITEM);
        }
    }

    private void validatePendingPaymentStatus() {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_STATUS);
        }
    }

    private static void validateOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new OrderException(OrderErrorCode.ORDER_NUMBER_REQUIRED);
        }
    }

    private static void validateMember(Member member) {
        if (member == null) {
            throw new OrderException(OrderErrorCode.MEMBER_REQUIRED);
        }
    }

    private static void validateOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_REQUIRED);
        }
    }

    public boolean isExpired(LocalDateTime now) {
        if (now == null) {
            throw new OrderException(
                    OrderErrorCode.CURRENT_TIME_REQUIRED
            );
        }

        return !now.isBefore(expiresAt);
    }
    public void validatePayable(LocalDateTime now) {
        validatePendingPaymentStatus();

        if (isExpired(now)) {
            throw new OrderException(
                    OrderErrorCode.ORDER_EXPIRED
            );
        }

        validateOrderReady();
    }
}
