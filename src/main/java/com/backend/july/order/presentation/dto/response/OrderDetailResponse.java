package com.backend.july.order.presentation.dto.response;

import com.backend.july.order.domain.OrderItem;
import com.backend.july.order.domain.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        String orderNumber,
        BigDecimal totalAmount,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        List<OrderItemResponse> items
) {

    public static OrderDetailResponse from(PurchaseOrder order) {
        List<OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getExpiresAt(),
                order.getPaidAt(),
                order.getCancelledAt(),
                items
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
