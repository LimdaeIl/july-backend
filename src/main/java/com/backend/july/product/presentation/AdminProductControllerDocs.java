package com.backend.july.product.presentation;

import com.backend.july.common.response.ApiResponse;
import com.backend.july.inventory.presentation.dto.request.ChangeInventoryRequest;
import com.backend.july.inventory.presentation.dto.response.ChangeInventoryResponse;
import com.backend.july.product.presentation.dto.request.CreateProductRequest;
import com.backend.july.product.presentation.dto.request.UpdateProductRequest;
import com.backend.july.product.presentation.dto.request.UpdateProductStatusRequest;
import com.backend.july.product.presentation.dto.response.CreateProductResponse;
import com.backend.july.product.presentation.dto.response.UpdateProductResponse;
import com.backend.july.product.presentation.dto.response.UpdateProductStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
        name = "관리자 상품 API",
        description = "관리자 권한으로 상품 정보, 판매 상태 및 재고를 관리하는 API입니다."
)
public interface AdminProductControllerDocs {

    @Operation(
            summary = "상품 생성",
            description = """
                    새로운 상품을 생성합니다.
                    
                    관리자 권한이 필요하며, 상품명, 가격 등 상품 생성에 필요한 정보를 전달해야 합니다.
                    상품 생성이 완료되면 생성된 상품 정보를 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "상품 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패 또는 잘못된 상품 정보"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 상품 등 상품 생성 충돌")})
    ResponseEntity<ApiResponse<CreateProductResponse>> create(
            @RequestBody @Valid CreateProductRequest request
    );

    @Operation(
            summary = "상품 정보 수정",
            description = """
                    상품의 기본 정보를 수정합니다.
                    
                    경로 변수로 수정할 상품 ID를 전달하고,
                    요청 본문으로 변경할 상품 정보를 전달합니다.
                    
                    관리자 권한이 필요합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 정보 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패 또는 잘못된 상품 정보"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "수정할 상품을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<UpdateProductResponse>> update(
            @Parameter(name = "productId", description = "수정할 상품의 ID", required = true, in = ParameterIn.PATH, example = "1")
            Long productId,

            @RequestBody @Valid
            UpdateProductRequest request);

    @Operation(
            summary = "상품 판매 상태 수정",
            description = """
                    상품의 판매 상태를 수정합니다.
                    
                    상품을 판매 가능, 판매 중지 등의 상태로 변경할 때 사용합니다.
                    변경 가능한 상태 값은 UpdateProductStatusRequest의 상품 상태 필드를 따릅니다.
                    
                    관리자 권한이 필요합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 판매 상태 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 상품 상태 또는 요청 값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상태를 변경할 상품을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "현재 상품 상태에서는 요청한 상태로 변경할 수 없음", responseCode = "409")
    })
    ResponseEntity<ApiResponse<UpdateProductStatusResponse>> updateStatus(
            @Parameter(name = "productId", description = "판매 상태를 변경할 상품의 ID", required = true, in = ParameterIn.PATH, example = "1")
            Long productId,

            @RequestBody
            @Valid
            UpdateProductStatusRequest request);

    @Operation(
            summary = "상품 재고 증가",
            description = """
                    지정한 상품의 재고 수량을 증가시킵니다.
                    
                    경로 변수로 상품 ID를 전달하고,
                    요청 본문의 quantity 값만큼 현재 재고를 증가시킵니다.
                    
                    관리자 권한이 필요합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 재고 증가 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "증가 수량이 유효하지 않거나 요청 값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "재고를 변경할 상품을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "재고 증가 처리 중 현재 상태와 충돌")
    })
    ResponseEntity<ApiResponse<ChangeInventoryResponse>> increaseInventory(
            @Parameter(
                    name = "productId",
                    description = "재고를 증가시킬 상품의 ID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1"
            )
            Long productId,

            @RequestBody @Valid ChangeInventoryRequest request
    );

    @Operation(
            summary = "상품 재고 감소",
            description = """
                    지정한 상품의 재고 수량을 감소시킵니다.
                    
                    경로 변수로 상품 ID를 전달하고,
                    요청 본문의 quantity 값만큼 현재 재고를 차감합니다.
                    
                    현재 재고보다 많은 수량은 차감할 수 없습니다.
                    관리자 권한이 필요합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 재고 감소 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "감소 수량이 유효하지 않거나 요청 값 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "재고를 변경할 상품을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "재고가 부족하거나 재고 감소 처리 중 상태 충돌"
            )
    })
    ResponseEntity<ApiResponse<ChangeInventoryResponse>> decreaseInventory(
            @Parameter(
                    name = "productId",
                    description = "재고를 감소시킬 상품의 ID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1"
            )
            Long productId,

            @RequestBody @Valid ChangeInventoryRequest request
    );

    @Operation(
            summary = "상품 삭제",
            description = """
                    지정한 상품을 삭제 처리합니다.
                    
                    실제 데이터가 즉시 제거되는 물리 삭제가 아니라
                    상품을 삭제 상태로 변경하는 소프트 삭제 방식입니다.
                    
                    관리자 권한이 필요합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 상품 ID"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "삭제할 상품을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 삭제된 상품이거나 현재 상태에서는 삭제할 수 없음"
            )
    })
    ResponseEntity<ApiResponse<Void>> softDelete(
            @Parameter(
                    name = "productId",
                    description = "삭제할 상품의 ID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1"
            )
            @Positive Long productId
    );
}
