package com.backend.july.fixture;

import com.backend.july.order.presentation.dto.request.CreateOrderRequest.OrderItemRequest;
import java.util.List;

public final class OrderFixture {

    public static final int DEFAULT_ORDER_QUANTITY = 1;

    private OrderFixture() {
    }

    public static OrderItemRequest orderItemRequest(
            Long productId
    ) {
        return orderItemRequest(
                productId,
                DEFAULT_ORDER_QUANTITY
        );
    }

    public static OrderItemRequest orderItemRequest(
            Long productId,
            int quantity
    ) {
        return new OrderItemRequest(
                productId,
                quantity
        );
    }

    public static List<OrderItemRequest> orderItems(
            Long productId
    ) {
        return List.of(
                orderItemRequest(productId)
        );
    }

    public static List<OrderItemRequest> orderItems(
            Long productId,
            int quantity
    ) {
        return List.of(
                orderItemRequest(productId, quantity)
        );
    }
}
