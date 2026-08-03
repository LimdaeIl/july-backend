package com.backend.july.payment.application;

import com.backend.july.order.domain.OrderItem;
import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.order.exception.OrderErrorCode;
import com.backend.july.order.exception.OrderException;
import com.backend.july.order.infrastructure.PurchaseOrderRepository;
import com.backend.july.payment.domain.Payment;
import com.backend.july.payment.domain.PaymentStatus;
import com.backend.july.payment.exception.PaymentErrorCode;
import com.backend.july.payment.exception.PaymentException;
import com.backend.july.payment.infrastructure.PaymentRepository;
import com.backend.july.payment.presentation.dto.response.PreparePaymentResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PreparePaymentService {

    private final PurchaseOrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final Clock clock;

    @Transactional
    public PreparePaymentResponse prepare(
            Long memberId,
            Long orderId
    ) {
        PurchaseOrder order = findOrder(orderId);

        validateOwner(order, memberId);

        order.validatePayable(
                LocalDateTime.now(clock)
        );

        Payment payment =
                findOrCreateReadyPayment(order);

        return PreparePaymentResponse.of(
                payment.getId(),
                order.getId(),
                order.getOrderNumber(),
                createOrderName(order),
                payment.getAmount()
        );
    }

    private PurchaseOrder findOrder(
            Long orderId
    ) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderException(
                                OrderErrorCode.ORDER_NOT_FOUND
                        )
                );
    }

    private void validateOwner(
            PurchaseOrder order,
            Long memberId
    ) {
        if (!order.isOwnedBy(memberId)) {
            throw new PaymentException(
                    PaymentErrorCode
                            .PAYMENT_ACCESS_DENIED
            );
        }
    }

    private Payment findOrCreateReadyPayment(
            PurchaseOrder order
    ) {
        return paymentRepository
                .findFirstByOrderIdAndStatusOrderByIdDesc(
                        order.getId(),
                        PaymentStatus.READY
                )
                .map(payment -> {
                    validateReadyPaymentAmount(
                            payment,
                            order
                    );

                    return payment;
                })
                .orElseGet(() ->
                        createPayment(order)
                );
    }

    private Payment createPayment(
            PurchaseOrder order
    ) {
        Payment payment =
                Payment.create(
                        order,
                        order.getTotalAmount()
                );

        return paymentRepository.save(payment);
    }

    private void validateReadyPaymentAmount(
            Payment payment,
            PurchaseOrder order
    ) {
        if (
                payment.getAmount()
                        .compareTo(
                                order.getTotalAmount()
                        ) != 0
        ) {
            throw new PaymentException(
                    PaymentErrorCode
                            .PAYMENT_AMOUNT_MISMATCH
            );
        }
    }

    private String createOrderName(
            PurchaseOrder order
    ) {
        OrderItem firstOrderItem = order.getOrderItems()
                .stream()
                .findFirst()
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_ITEMS_EMPTY));

        int additionalCount = order.getOrderItems().size() - 1;

        if (additionalCount <= 0) {
            return firstOrderItem.getProductName();
        }

        return firstOrderItem.getProductName()
                + " 외 "
                + additionalCount
                + "건";
    }
}
