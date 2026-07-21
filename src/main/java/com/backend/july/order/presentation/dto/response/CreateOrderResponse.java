package com.backend.july.order.presentation.dto.response;

import com.backend.july.order.domain.OrderItem;
import com.backend.july.order.domain.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateOrderResponse(
        Long orderId,
        String orderNumber,
        List<OrderItemResponse> items,
        int itemCount,
        int totalQuantity,
        BigDecimal totalAmount,
        String status,
        LocalDateTime expiresAt
) {

    public static CreateOrderResponse from(PurchaseOrder order) {
        List<OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map(OrderItemResponse::from)
                .toList();

        int totalQuantity = order.getOrderItems()
                .stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        return new CreateOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                items,
                items.size(),
                totalQuantity,
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getExpiresAt()
        );
    }

    public record OrderItemResponse(
            Long orderItemId,
            Long productId,
            String productName,
            BigDecimal orderPrice,
            int quantity,
            BigDecimal lineAmount
    ) {

        public static OrderItemResponse from(OrderItem orderItem) {
            return new OrderItemResponse(
                    orderItem.getId(),
                    orderItem.getProductId(),
                    orderItem.getProductName(),
                    orderItem.getOrderPrice(),
                    orderItem.getQuantity(),
                    orderItem.getLineAmount()
            );
        }
    }
}
