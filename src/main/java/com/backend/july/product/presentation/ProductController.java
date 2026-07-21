package com.backend.july.product.presentation;

import com.backend.july.common.response.ApiResponse;
import com.backend.july.common.response.CursorResponse;
import com.backend.july.common.response.PageResponse;
import com.backend.july.product.application.GetProductService;
import com.backend.july.product.application.GetProductsService;
import com.backend.july.product.presentation.dto.ProductCursor;
import com.backend.july.product.presentation.dto.ProductSortType;
import com.backend.july.product.presentation.dto.condition.GetProductsCondition;
import com.backend.july.product.presentation.dto.condition.GetProductsCursorCondition;
import com.backend.july.product.presentation.dto.response.ProductDetailResponse;
import com.backend.july.product.presentation.dto.response.ProductSummaryResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@RestController
public class ProductController implements ProductControllerDocs {

    private final GetProductsService getProductsService;
    private final GetProductService getProductService;

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> getProductsByPage(
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

        PageResponse<ProductSummaryResponse> response = getProductsService.getProductsByPage(
                condition);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "상품 목록 조회: 상품 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @GetMapping("/cursor")
    public ResponseEntity<ApiResponse<CursorResponse<ProductSummaryResponse, ProductCursor>>> getProductsByCursor(
            @RequestParam(required = false)
            @Positive(message = "상품 목록 조회: 판매자 ID는 양수여야 합니다.")
            Long sellerId,

            @RequestParam(defaultValue = "LATEST")
            ProductSortType sortType,

            @RequestParam(required = false)
            @Positive(message = "상품 목록 조회: 커서 ID는 양수여야 합니다.")
            Long cursorId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant cursorCreatedAt,

            @RequestParam(required = false)
            @DecimalMin(value = "0", inclusive = true, message = "상품 목록 조회: 커서 가격은 0 이상이어야 합니다.")
            BigDecimal cursorPrice,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "상품 목록 조회: 조회 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "상품 목록 조회: 조회 크기는 100 이하여야 합니다.")
            int size
    ) {
        GetProductsCursorCondition condition =
                new GetProductsCursorCondition(
                        sellerId,
                        sortType,
                        cursorId,
                        cursorCreatedAt,
                        cursorPrice,
                        size
                );

        CursorResponse<ProductSummaryResponse, ProductCursor> response = getProductsService.getProductsByCursor(
                condition);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "상품 목록 조회: 상품 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @PathVariable @Positive(message = "상품 상세 조회: 상품 ID는 양수여야 합니다.") Long productId
    ) {
        ProductDetailResponse response = getProductService.getProduct(productId);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "상품 상세 조회: 상품 상세 조회에 성공했습니다.",
                        response
                )
        );
    }
}
