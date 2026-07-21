package com.backend.july.order.presentation;

import com.backend.july.auth.infrastructure.security.principal.LoginMember;
import com.backend.july.common.response.ApiResponse;
import com.backend.july.common.response.CursorResponse;
import com.backend.july.order.domain.OrderStatus;
import com.backend.july.order.presentation.dto.request.CreateOrderRequest;
import com.backend.july.order.presentation.dto.response.CancelOrderResponse;
import com.backend.july.order.presentation.dto.response.CreateOrderResponse;
import com.backend.july.order.presentation.dto.response.OrderDetailResponse;
import com.backend.july.order.presentation.dto.response.OrderSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "주문 API",
        description = "인증된 회원의 상품 주문 생성, 조회 및 취소를 처리하는 API입니다."
)
public interface OrderControllerDocs {

    @Operation(
            summary = "주문 생성",
            description = """
                    로그인한 회원이 상품을 주문합니다.

                    주문할 상품 ID와 수량을 요청 본문으로 전달합니다.
                    주문 생성 과정에서 상품의 판매 가능 여부와 재고 수량을 확인합니다.

                    주문이 정상적으로 생성되면 주문 정보와 주문 상태를 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "주문 생성 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 유효하지 않거나 주문 수량이 잘못됨"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주문할 상품을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "상품을 주문할 수 없거나 재고가 부족함"
            )
    })
    ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @Parameter(hidden = true)
            LoginMember loginMember,

            @Valid
            CreateOrderRequest request
    );

    @Operation(
            summary = "주문 상세 조회",
            description = """
                    로그인한 회원의 주문 상세 정보를 조회합니다.

                    본인이 생성한 주문만 조회할 수 있습니다.
                    주문 상품, 주문 수량, 결제 금액, 주문 상태 등의 상세 정보를 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주문 상세 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "주문 ID가 유효하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "해당 주문에 대한 조회 권한이 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주문을 찾을 수 없음"
            )
    })
    ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @Parameter(hidden = true)
            LoginMember loginMember,

            @Parameter(
                    name = "orderId",
                    description = "조회할 주문의 ID입니다.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1"
            )
            @Positive(message = "주문 상세 조회: 주문 ID는 양수여야 합니다.")
            Long orderId
    );

    @Operation(
            summary = "내 주문 목록 조회",
            description = """
                    로그인한 회원의 주문 목록을 커서 기반으로 조회합니다.

                    주문 상태를 지정하면 해당 상태의 주문만 조회할 수 있습니다.
                    검색어를 전달하면 주문 상품 등 검색 대상에 해당하는 주문을 조회합니다.

                    최초 조회 시 커서를 전달하지 않으며,
                    다음 조회부터 이전 응답에 포함된 마지막 주문 ID를 커서로 전달합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주문 목록 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "조회 조건, 커서 또는 조회 크기가 유효하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    ResponseEntity<CursorResponse<OrderSummaryResponse, Long>> getOrders(
            @Parameter(hidden = true)
            LoginMember loginMember,

            @Parameter(
                    name = "status",
                    description = "조회할 주문 상태입니다. 전달하지 않으면 전체 상태의 주문을 조회합니다.",
                    in = ParameterIn.QUERY,
                    example = "CREATED"
            )
            OrderStatus status,

            @Parameter(
                    name = "keyword",
                    description = "주문 목록을 검색할 키워드입니다.",
                    in = ParameterIn.QUERY,
                    example = "키보드"
            )
            String keyword,

            @Parameter(
                    name = "cursor",
                    description = "이전 조회 결과의 마지막 주문 ID입니다. 최초 조회 시 전달하지 않습니다.",
                    in = ParameterIn.QUERY,
                    example = "100"
            )
            @Positive(message = "주문 목록 조회: 커서는 양수여야 합니다.")
            Long cursor,

            @Parameter(
                    name = "size",
                    description = "한 번에 조회할 주문 수입니다. 1 이상 100 이하만 가능합니다.",
                    in = ParameterIn.QUERY,
                    example = "20"
            )
            @Min(value = 1, message = "주문 목록 조회: 조회 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "주문 목록 조회: 조회 크기는 100 이하여야 합니다.")
            int size
    );

    @Operation(
            summary = "주문 취소",
            description = """
                    로그인한 회원이 본인의 주문을 취소합니다.

                    주문 상태가 취소 가능한 상태인 경우에만 처리할 수 있습니다.
                    주문 취소가 완료되면 취소된 주문 정보와 변경된 주문 상태를 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "주문 취소 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "주문 ID가 유효하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "해당 주문에 대한 취소 권한이 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "취소할 주문을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "현재 주문 상태에서는 취소할 수 없음"
            )
    })
    ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @Parameter(hidden = true)
            LoginMember loginMember,

            @Parameter(
                    name = "orderId",
                    description = "취소할 주문의 ID입니다.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1"
            )
            @Positive(message = "주문 취소: 주문 ID는 양수여야 합니다.")
            Long orderId
    );
}