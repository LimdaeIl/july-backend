package com.backend.july.payment.application;

import com.backend.july.inventory.domain.Inventory;
import com.backend.july.inventory.infrastructure.InventoryRepository;
import com.backend.july.order.domain.OrderItem;
import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.payment.domain.Payment;
import com.backend.july.payment.infrastructure.PaymentRepository;
import com.backend.july.payment.infrastructure.toss.TossPaymentsClient;
import com.backend.july.payment.infrastructure.toss.dto.TossPaymentResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CancelPaymentService {

    private static final String TOSS_CANCELLED_STATUS =
            "CANCELED";

    private final PaymentRepository paymentRepository;
    private final InventoryRepository inventoryRepository;
    private final TossPaymentsClient tossPaymentsClient;

    @Transactional
    public void cancel(
            Long memberId,
            Long paymentId,
            String cancelReason
    ) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "결제를 찾을 수 없습니다."
                        )
                );

        PurchaseOrder order = payment.getOrder();

        order.validateOwner(memberId);

        if (!payment.isApproved()) {
            throw new IllegalStateException(
                    "승인된 결제만 취소할 수 있습니다."
            );
        }

        /*
         * 토스 결제를 먼저 취소합니다.
         *
         * 토스 취소에 실패하면 로컬 주문과 재고를
         * 변경하지 않습니다.
         */
        TossPaymentResponse tossResponse =
                tossPaymentsClient.cancel(
                        payment.getPaymentKey(),
                        cancelReason
                );

        if (!TOSS_CANCELLED_STATUS.equals(
                tossResponse.status()
        )) {
            throw new IllegalStateException(
                    "토스 결제 전체 취소가 완료되지 않았습니다."
            );
        }

        LocalDateTime cancelledAt =
                LocalDateTime.now();

        restoreInventory(order);

        payment.cancel(
                cancelReason,
                cancelledAt
        );

        order.cancelPaidOrder(cancelledAt);
    }

    private void restoreInventory(
            PurchaseOrder order
    ) {
        List<OrderItem> orderItems =
                order.getOrderItems();

        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .sorted()
                .toList();

        List<Inventory> inventories =
                inventoryRepository
                        .findAllByProductIdsForUpdate(
                                productIds
                        );

        if (inventories.size() != productIds.size()) {
            throw new IllegalStateException(
                    "일부 상품의 재고 정보를 찾을 수 없습니다."
            );
        }

        Map<Long, Inventory> inventoryByProductId =
                inventories.stream()
                        .collect(
                                Collectors.toMap(
                                        inventory ->
                                                inventory
                                                        .getProduct()
                                                        .getId(),
                                        Function.identity()
                                )
                        );

        for (OrderItem orderItem : orderItems) {
            Inventory inventory =
                    inventoryByProductId.get(
                            orderItem.getProductId()
                    );

            if (inventory == null) {
                throw new IllegalStateException(
                        "상품 재고 정보를 찾을 수 없습니다."
                );
            }

            inventory.increase(
                    orderItem.getQuantity()
            );
        }
    }
}
