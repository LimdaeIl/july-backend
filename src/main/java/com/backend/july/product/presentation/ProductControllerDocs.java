package com.backend.july.product.presentation;

import com.backend.july.common.response.ApiResponse;
import com.backend.july.common.response.CursorResponse;
import com.backend.july.common.response.PageResponse;
import com.backend.july.product.presentation.dto.ProductCursor;
import com.backend.july.product.presentation.dto.ProductSortType;
import com.backend.july.product.presentation.dto.response.ProductDetailResponse;
import com.backend.july.product.presentation.dto.response.ProductSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "상품 API",
        description = "상품 목록 및 상품 상세 정보를 조회하는 API입니다."
)
public interface ProductControllerDocs {

    @Operation(
            summary = "상품 목록 페이지 조회",
            description = """
                    상품 목록을 페이지 기반으로 조회합니다.

                    판매자 ID를 전달하면 해당 판매자가 등록한 상품만 조회합니다.
                    정렬 기준을 지정하지 않으면 최신 등록순으로 조회합니다.
                    페이지 번호는 0부터 시작하며, 한 페이지에서 최대 100개의 상품을 조회할 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 목록 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "페이지 번호, 페이지 크기 또는 정렬 조건이 유효하지 않음"
            )
    })
    ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> getProductsByPage(
            @Parameter(
                    name = "sellerId",
                    description = "조회할 판매자의 ID입니다. 전달하지 않으면 전체 판매자의 상품을 조회합니다.",
                    in = ParameterIn.QUERY,
                    example = "1"
            )
            Long sellerId,

            @Parameter(
                    name = "sortType",
                    description = "상품 목록 정렬 기준입니다.",
                    in = ParameterIn.QUERY,
                    example = "LATEST"
            )
            ProductSortType sortType,

            @Parameter(
                    name = "page",
                    description = "조회할 페이지 번호입니다. 0부터 시작합니다.",
                    in = ParameterIn.QUERY,
                    example = "0"
            )
            @Min(value = 0, message = "상품 목록 조회: 페이지는 0 이상이어야 합니다.")
            int page,

            @Parameter(
                    name = "size",
                    description = "한 페이지에서 조회할 상품 수입니다. 1 이상 100 이하만 가능합니다.",
                    in = ParameterIn.QUERY,
                    example = "20"
            )
            @Min(value = 1, message = "상품 목록 조회: 페이지 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "상품 목록 조회: 페이지 크기는 100 이하여야 합니다.")
            int size
    );

    @Operation(
            summary = "상품 목록 커서 조회",
            description = """
                    상품 목록을 커서 기반으로 조회합니다.

                    판매자 ID를 전달하면 해당 판매자가 등록한 상품만 조회합니다.
                    최초 조회 시 커서 값을 전달하지 않으며, 다음 조회부터 이전 응답에 포함된 커서 정보를 전달합니다.

                    정렬 기준에 따라 사용하는 커서 값이 달라질 수 있습니다.
                    최신순 정렬에서는 상품 ID와 생성 시각을 사용하고,
                    가격순 정렬에서는 상품 ID와 가격을 커서로 사용할 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 목록 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "커서, 조회 크기 또는 정렬 조건이 유효하지 않음"
            )
    })
    ResponseEntity<ApiResponse<CursorResponse<ProductSummaryResponse, ProductCursor>>> getProductsByCursor(
            @Parameter(
                    name = "sellerId",
                    description = "조회할 판매자의 ID입니다. 전달하지 않으면 전체 판매자의 상품을 조회합니다.",
                    in = ParameterIn.QUERY,
                    example = "1"
            )
            @Positive(message = "상품 목록 조회: 판매자 ID는 양수여야 합니다.")
            Long sellerId,

            @Parameter(
                    name = "sortType",
                    description = "상품 목록 정렬 기준입니다.",
                    in = ParameterIn.QUERY,
                    example = "LATEST"
            )
            ProductSortType sortType,

            @Parameter(
                    name = "cursorId",
                    description = "이전 조회 결과의 마지막 상품 ID입니다. 최초 조회 시 전달하지 않습니다.",
                    in = ParameterIn.QUERY,
                    example = "100"
            )
            @Positive(message = "상품 목록 조회: 커서 ID는 양수여야 합니다.")
            Long cursorId,

            @Parameter(
                    name = "cursorCreatedAt",
                    description = "이전 조회 결과의 마지막 상품 생성 시각입니다. ISO-8601 형식으로 전달합니다.",
                    in = ParameterIn.QUERY,
                    example = "2026-07-21T03:00:00Z"
            )
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant cursorCreatedAt,

            @Parameter(
                    name = "cursorPrice",
                    description = "이전 조회 결과의 마지막 상품 가격입니다. 가격 정렬 시 사용합니다.",
                    in = ParameterIn.QUERY,
                    example = "15000"
            )
            @DecimalMin(
                    value = "0",
                    inclusive = true,
                    message = "상품 목록 조회: 커서 가격은 0 이상이어야 합니다."
            )
            BigDecimal cursorPrice,

            @Parameter(
                    name = "size",
                    description = "한 번에 조회할 상품 수입니다. 1 이상 100 이하만 가능합니다.",
                    in = ParameterIn.QUERY,
                    example = "20"
            )
            @Min(value = 1, message = "상품 목록 조회: 조회 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "상품 목록 조회: 조회 크기는 100 이하여야 합니다.")
            int size
    );

    @Operation(
            summary = "상품 상세 조회",
            description = """
                    상품 ID를 이용하여 상품 상세 정보를 조회합니다.

                    상품의 기본 정보, 가격, 판매 상태, 재고 및 판매자 정보를 반환합니다.
                    존재하지 않거나 조회할 수 없는 상품인 경우 오류가 발생합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 상세 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "상품 ID가 유효하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품을 찾을 수 없음"
            )
    })
    ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @Parameter(
                    name = "productId",
                    description = "조회할 상품의 ID입니다.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1"
            )
            @Positive(message = "상품 상세 조회: 상품 ID는 양수여야 합니다.")
            Long productId
    );
}