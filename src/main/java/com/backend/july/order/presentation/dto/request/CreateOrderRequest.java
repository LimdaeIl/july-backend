package com.backend.july.order.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "주문 상품은 하나 이상이어야 합니다.")
        @Size(max = 50, message = "한 번에 주문할 수 있는 상품 종류는 최대 50개입니다.")
        List<@Valid OrderItemRequest> items
) {

        public record OrderItemRequest(

                @NotNull(message = "상품 ID는 필수입니다.")
                @Positive(message = "상품 ID는 양수여야 합니다.")
                Long productId,

                @NotNull(message = "주문 수량은 필수입니다.")
                @Positive(message = "주문 수량은 1개 이상이어야 합니다.")
                Integer quantity
        ) {
        }
}
