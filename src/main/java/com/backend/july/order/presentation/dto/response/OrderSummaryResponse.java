package com.backend.july.order.presentation.dto.response;

import com.backend.july.order.domain.OrderItem;
import com.backend.july.order.domain.OrderStatus;
import com.backend.july.order.domain.PurchaseOrder;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record OrderSummaryResponse(
        Long orderId,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant orderedAt,
        LocalDateTime expiresAt,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        int itemCount,
        OrderItemSummaryResponse representativeItem
) {

    public static OrderSummaryResponse from(PurchaseOrder order) {
        List<OrderItem> orderItems = order.getOrderItems();

        OrderItem representativeItem = orderItems.stream()
                .findFirst()
                .orElse(null);

        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getExpiresAt(),
                order.getPaidAt(),
                order.getCancelledAt(),
                orderItems.size(),
                representativeItem == null
                        ? null
                        : OrderItemSummaryResponse.from(representativeItem)
        );
    }

    public record OrderItemSummaryResponse(
            Long orderItemId,
            Long productId,
            String productName,
            BigDecimal orderPrice,
            int quantity,
            BigDecimal lineAmount
    ) {

        public static OrderItemSummaryResponse from(OrderItem orderItem) {
            return new OrderItemSummaryResponse(
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
