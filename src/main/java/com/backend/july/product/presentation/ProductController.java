package com.backend.july.product.presentation;

import com.backend.july.common.response.ApiResponse;
import com.backend.july.common.response.PageResponse;
import com.backend.july.product.application.GetProductsService;
import com.backend.july.product.presentation.dto.ProductSortType;
import com.backend.july.product.presentation.dto.condition.GetProductsCondition;
import com.backend.july.product.presentation.dto.response.ProductSummaryResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@RestController
public class ProductController {

    private final GetProductsService getProductsService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> getProducts(
            @RequestParam(required = false)
            Long sellerId,

            @RequestParam(defaultValue = "LATEST")
            ProductSortType sortType,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "상품 목록 조회: 페이지는 0 이상이어야 합니다.")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "상품 목록 조회: 페이지 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "상품 목록 조회: 페이지 크기는 100 이하여야 합니다.")
            int size
    ) {
        GetProductsCondition condition = new GetProductsCondition(
                sellerId,
                sortType,
                page,
                size
        );

        PageResponse<ProductSummaryResponse> response = getProductsService.getProducts(condition);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "상품 목록 조회: 상품 목록 조회에 성공했습니다.",
                        response
                )
        );
    }
}
