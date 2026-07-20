package com.backend.july.order.presentation.dto.response;

import com.backend.july.order.domain.PurchaseOrder;
import java.time.LocalDateTime;

public record CancelOrderResponse(
        Long orderId,
        String status,
        LocalDateTime cancelledAt
) {

    public static CancelOrderResponse from(PurchaseOrder order) {
        return new CancelOrderResponse(order.getId(), order.getStatus().name(),
                order.getCancelledAt());
    }
}
