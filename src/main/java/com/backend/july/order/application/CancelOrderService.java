package com.backend.july.order.application;

import com.backend.july.inventory.domain.Inventory;
import com.backend.july.inventory.exception.InventoryErrorCode;
import com.backend.july.inventory.exception.InventoryException;
import com.backend.july.inventory.infrastructure.InventoryRepository;
import com.backend.july.order.domain.OrderItem;
import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.order.exception.OrderErrorCode;
import com.backend.july.order.exception.OrderException;
import com.backend.july.order.infrastructure.PurchaseOrderRepository;
import com.backend.july.order.presentation.dto.response.CancelOrderResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final Clock clock;

    @Transactional
    public CancelOrderResponse cancel(Long memberId, Long orderId) {
        PurchaseOrder order = purchaseOrderRepository
                .findByIdAndMemberIdForUpdate(orderId, memberId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        LocalDateTime cancelledAt = LocalDateTime.now(clock);

        order.cancelPendingOrder(cancelledAt);

        for (OrderItem orderItem : order.getOrderItems()) {
            Inventory inventory = inventoryRepository
                    .findByProductIdForUpdate(orderItem.getProductId())
                    .orElseThrow(
                            () -> new InventoryException(InventoryErrorCode.INVENTORY_NOT_FOUND));

            inventory.increase(orderItem.getQuantity());
        }

        return CancelOrderResponse.from(order);
    }
}
