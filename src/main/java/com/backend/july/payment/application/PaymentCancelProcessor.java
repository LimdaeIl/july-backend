package com.backend.july.payment.application;

import com.backend.july.inventory.domain.Inventory;
import com.backend.july.inventory.infrastructure.InventoryRepository;
import com.backend.july.order.domain.OrderItem;
import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.payment.domain.Payment;
import com.backend.july.payment.exception.PaymentErrorCode;
import com.backend.july.payment.exception.PaymentException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PaymentCancelProcessor {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public void processLocalCancellation(Payment payment, PurchaseOrder order, String cancelReason) {
        LocalDateTime cancelledAt = LocalDateTime.now();

        restoreInventory(order);

        payment.cancel(cancelReason, cancelledAt);
        order.cancelPaidOrder(cancelledAt);
    }

    private void restoreInventory(PurchaseOrder order) {
        List<OrderItem> orderItems = order.getOrderItems();

        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .sorted()
                .toList();

        List<Inventory> inventories = inventoryRepository.findAllByProductIdsForUpdate(productIds);

        if (inventories.size() != productIds.size()) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_ORDER_MISMATCH);
        }

        Map<Long, Inventory> inventoryByProductId = inventories.stream()
                .collect(Collectors.toMap(
                        inventory -> inventory.getProduct().getId(),
                        Function.identity()
                ));

        for (OrderItem orderItem : orderItems) {
            Inventory inventory = inventoryByProductId.get(orderItem.getProductId());

            if (inventory == null) {
                throw new PaymentException(PaymentErrorCode.PAYMENT_ORDER_MISMATCH);
            }

            inventory.increase(orderItem.getQuantity());
        }
    }
}
