package com.backend.july.order.presentation;

import com.backend.july.auth.infrastructure.security.principal.LoginMember;
import com.backend.july.common.response.ApiResponse;
import com.backend.july.order.application.CancelOrderService;
import com.backend.july.order.application.CreateOrderService;
import com.backend.july.order.application.GetOrderService;
import com.backend.july.order.presentation.dto.request.CreateOrderRequest;
import com.backend.july.order.presentation.dto.response.CancelOrderResponse;
import com.backend.july.order.presentation.dto.response.CreateOrderResponse;
import com.backend.july.order.presentation.dto.response.OrderDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderService createOrderService;
    private final GetOrderService getOrderService;
    private final CancelOrderService cancelOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderResponse response = createOrderService.create(loginMember.memberId(),
                request.productId(),
                request.quantity());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.created(
                                "주문 생성에 성공했습니다.",
                                response
                        ));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable Long orderId
    ) {
        OrderDetailResponse response = getOrderService.get(loginMember.memberId(), orderId);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "주문 상세 조회에 성공했습니다.",
                        response
                )
        );
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable Long orderId
    ) {
        CancelOrderResponse response = cancelOrderService.cancel(loginMember.memberId(), orderId);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "주문 취소에 성공했습니다.",
                        response
                )
        );
    }
}