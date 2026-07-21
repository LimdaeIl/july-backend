package com.backend.july.order.presentation;

import com.backend.july.auth.infrastructure.security.principal.LoginMember;
import com.backend.july.common.response.ApiResponse;
import com.backend.july.common.response.CursorResponse;
import com.backend.july.order.application.CancelOrderService;
import com.backend.july.order.application.CreateOrderService;
import com.backend.july.order.application.GetOrderService;
import com.backend.july.order.application.GetOrdersService;
import com.backend.july.order.domain.OrderStatus;
import com.backend.july.order.presentation.dto.request.CreateOrderRequest;
import com.backend.july.order.presentation.dto.response.CancelOrderResponse;
import com.backend.july.order.presentation.dto.response.CreateOrderResponse;
import com.backend.july.order.presentation.dto.response.OrderDetailResponse;
import com.backend.july.order.presentation.dto.response.OrderSummaryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/orders")
@RestController
public class OrderController {

    private final CreateOrderService createOrderService;
    private final GetOrderService getOrderService;
    private final GetOrdersService getOrdersService;
    private final CancelOrderService cancelOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderResponse response = createOrderService.create(
                loginMember.memberId(),
                request.productId(),
                request.quantity()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.created(
                                "주문 생성에 성공했습니다.",
                                response
                        )
                );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable
            @Positive(message = "주문 상세 조회: 주문 ID는 양수여야 합니다.")
            Long orderId
    ) {
        OrderDetailResponse response = getOrderService.get(
                loginMember.memberId(),
                orderId
        );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "주문 상세 조회에 성공했습니다.",
                        response
                )
        );
    }

    @GetMapping
    public ResponseEntity<CursorResponse<OrderSummaryResponse, Long>> getOrders(
            @AuthenticationPrincipal LoginMember loginMember,

            @RequestParam(required = false)
            OrderStatus status,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            @Positive(message = "주문 목록 조회: 커서는 양수여야 합니다.")
            Long cursor,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "주문 목록 조회: 조회 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "주문 목록 조회: 조회 크기는 100 이하여야 합니다.")
            int size
    ) {
        CursorResponse<OrderSummaryResponse, Long> response =
                getOrdersService.get(
                        loginMember.memberId(),
                        status,
                        keyword,
                        cursor,
                        size
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable
            @Positive(message = "주문 취소: 주문 ID는 양수여야 합니다.")
            Long orderId
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
