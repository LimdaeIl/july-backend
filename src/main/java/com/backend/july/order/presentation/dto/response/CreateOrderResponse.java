package com.backend.july.order.presentation.dto.response;

import com.backend.july.order.domain.OrderItem;
import com.backend.july.order.domain.PurchaseOrder;
import java.math.BigDecimal;

public record CreateOrderResponse(
        Long orderId,
        String orderNumber,
        Long productId,
        String productName,
        BigDecimal orderPrice,
        int quantity,
        BigDecimal totalAmount,
        String status
) {

    public static CreateOrderResponse of(PurchaseOrder savedOrder, OrderItem orderItem) {
        return new CreateOrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                orderItem.getProduct().getId(),
                orderItem.getProductName(),
                orderItem.getOrderPrice(),
                orderItem.getQuantity(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus().name()
        );
    }
}
