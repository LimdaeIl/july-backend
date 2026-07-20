package com.backend.july.product.presentation.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank(message = "상품 생성: 상품명은 필수입니다.")
        @Size(min = 2, max = 100, message = "상품 생성: 상품명은 2자 이상 100자 이하입니다.")
        String name,

        @NotNull(message = "상품 생성: 상품 가격은 필수입니다.")
        @Min(value = 1, message = "상품 생성: 상품 가격은 1원 이상이어야 합니다.")
        @Digits(integer = 19, fraction = 0, message = "상품 생성: 상품 가격은 정수만 입력 가능합니다.")
        BigDecimal price,

        @PositiveOrZero(message = "상품 생성: 상품 수량은 0 이상이어야 합니다.")
        int initialQuantity
) {
}
