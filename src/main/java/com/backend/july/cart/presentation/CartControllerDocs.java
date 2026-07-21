package com.backend.july.cart.presentation;

import com.backend.july.auth.infrastructure.security.principal.LoginMember;
import com.backend.july.cart.presentation.dto.request.AddCartItemRequest;
import com.backend.july.cart.presentation.dto.request.UpdateCartItemQuantityRequest;
import com.backend.july.cart.presentation.dto.response.AddCartItemResponse;
import com.backend.july.cart.presentation.dto.response.CartResponse;
import com.backend.july.cart.presentation.dto.response.ClearCartResponse;
import com.backend.july.cart.presentation.dto.response.DeleteCartItemResponse;
import com.backend.july.cart.presentation.dto.response.UpdateCartItemQuantityResponse;
import com.backend.july.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "장바구니 API",
        description = "인증된 회원의 장바구니 상품 추가, 조회, 수량 변경 및 삭제를 처리합니다."
)
public interface CartControllerDocs {

    @Operation(
            summary = "장바구니 상품 추가",
            description = """
                    로그인한 회원의 장바구니에 상품을 추가합니다.

                    동일한 상품이 이미 담겨 있으면 기존 수량에 요청 수량을 더합니다.
                    장바구니에 상품을 추가하는 시점에는 재고를 차감하지 않습니다.
                    판매 중인 상품만 새로 담을 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "장바구니 상품 추가 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "상품 ID 또는 수량이 유효하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회원 또는 상품을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "현재 판매 중인 상품이 아님"
            )
    })
    ResponseEntity<ApiResponse<AddCartItemResponse>> addItem(
            @Parameter(hidden = true)
            LoginMember loginMember,

            @Valid
            AddCartItemRequest request
    );

    @Operation(
            summary = "내 장바구니 조회",
            description = """
                    로그인한 회원의 장바구니를 조회합니다.

                    상품별 현재 가격, 현재 판매 상태, 수량 및 합계 금액을 반환합니다.
                    장바구니가 아직 생성되지 않은 경우 빈 장바구니를 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "장바구니 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    ResponseEntity<ApiResponse<CartResponse>> getCart(
            @Parameter(hidden = true)
            LoginMember loginMember
    );

    @Operation(
            summary = "장바구니 상품 수량 변경",
            description = "로그인한 회원의 장바구니 상품 수량을 지정한 수량으로 변경합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "장바구니 상품 수량 변경 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "장바구니 상품 ID 또는 수량이 유효하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "장바구니 상품을 찾을 수 없음"
            )
    })
    ResponseEntity<ApiResponse<UpdateCartItemQuantityResponse>>
    updateItemQuantity(
            @Parameter(hidden = true)
            LoginMember loginMember,

            @Parameter(
                    name = "cartItemId",
                    description = "수량을 변경할 장바구니 상품 ID입니다.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1"
            )
            @Positive(message = "장바구니 상품 ID는 양수여야 합니다.")
            Long cartItemId,

            @Valid
            UpdateCartItemQuantityRequest request
    );

    @Operation(
            summary = "장바구니 상품 삭제",
            description = "로그인한 회원의 장바구니에서 지정한 상품을 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "장바구니 상품 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "장바구니 상품 ID가 유효하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "장바구니 상품을 찾을 수 없음"
            )
    })
    ResponseEntity<ApiResponse<DeleteCartItemResponse>> deleteItem(
            @Parameter(hidden = true)
            LoginMember loginMember,

            @Parameter(
                    name = "cartItemId",
                    description = "삭제할 장바구니 상품 ID입니다.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1"
            )
            @Positive(message = "장바구니 상품 ID는 양수여야 합니다.")
            Long cartItemId
    );

    @Operation(
            summary = "장바구니 전체 비우기",
            description = "로그인한 회원의 장바구니 상품을 모두 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "장바구니 비우기 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    ResponseEntity<ApiResponse<ClearCartResponse>> clearCart(
            @Parameter(hidden = true)
            LoginMember loginMember
    );
}
