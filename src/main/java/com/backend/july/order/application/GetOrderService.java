package com.backend.july.order.application;

import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.order.exception.OrderErrorCode;
import com.backend.july.order.exception.OrderException;
import com.backend.july.order.infrastructure.PurchaseOrderRepository;
import com.backend.july.order.presentation.dto.response.OrderDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    @Transactional(readOnly = true)
    public OrderDetailResponse get(Long memberId, Long orderId) {
        PurchaseOrder order = purchaseOrderRepository
                .findDetailByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        return OrderDetailResponse.from(order);
    }
}
