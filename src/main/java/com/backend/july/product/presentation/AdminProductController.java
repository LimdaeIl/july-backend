package com.backend.july.product.presentation;


import com.backend.july.common.response.ApiResponse;
import com.backend.july.inventory.application.DecreaseInventoryService;
import com.backend.july.inventory.application.IncreaseInventoryService;
import com.backend.july.inventory.presentation.dto.request.ChangeInventoryRequest;
import com.backend.july.inventory.presentation.dto.response.ChangeInventoryResponse;
import com.backend.july.product.application.CreateProductService;
import com.backend.july.product.application.DeleteProductService;
import com.backend.july.product.application.UpdateProductService;
import com.backend.july.product.application.UpdateProductStatusService;
import com.backend.july.product.presentation.dto.request.CreateProductRequest;
import com.backend.july.product.presentation.dto.request.UpdateProductRequest;
import com.backend.july.product.presentation.dto.request.UpdateProductStatusRequest;
import com.backend.july.product.presentation.dto.response.CreateProductResponse;
import com.backend.july.product.presentation.dto.response.UpdateProductResponse;
import com.backend.july.product.presentation.dto.response.UpdateProductStatusResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/products")
@RestController
public class AdminProductController {

    private final CreateProductService createProductService;
    private final UpdateProductService updateProductService;
    private final UpdateProductStatusService updateProductStatusService;
    private final IncreaseInventoryService increaseInventoryService;
    private final DecreaseInventoryService decreaseInventoryService;
    private final DeleteProductService deleteProductService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateProductResponse>> create(
            @RequestBody @Valid CreateProductRequest request
    ) {
        CreateProductResponse response = createProductService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("상품 생성: 상품 생성에 성공했습니다.", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<UpdateProductResponse>> update(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        UpdateProductResponse response = updateProductService.update(productId, request);

        return ResponseEntity.ok(ApiResponse.ok("상품 수정: 상품 수정에 성공했습니다.", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<UpdateProductStatusResponse>> updateStatus(
            @PathVariable Long productId, @RequestBody @Valid UpdateProductStatusRequest request) {
        UpdateProductStatusResponse response = updateProductStatusService.update(productId,
                request);
        return ResponseEntity.ok(ApiResponse.ok("상품 상태 수정: 상품 상태 수정에 성공했습니다.", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{productId}/inventory/increase")
    public ResponseEntity<ApiResponse<ChangeInventoryResponse>> increaseInventory(
            @PathVariable Long productId, @RequestBody @Valid ChangeInventoryRequest request) {
        ChangeInventoryResponse response = increaseInventoryService.increase(productId,
                request.quantity());
        return ResponseEntity.ok(ApiResponse.ok("재고 증가: 상품 재고 증가에 성공했습니다.", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{productId}/inventory/decrease")
    public ResponseEntity<ApiResponse<ChangeInventoryResponse>> decreaseInventory(
            @PathVariable Long productId, @RequestBody @Valid ChangeInventoryRequest request) {
        ChangeInventoryResponse response = decreaseInventoryService.decrease(productId,
                request.quantity());
        return ResponseEntity.ok(ApiResponse.ok("재고 감소: 상품 재고 감소에 성공했습니다.", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @PathVariable @Positive Long productId
    ) {
        deleteProductService.softDelete(productId);

        return ResponseEntity.ok(
                ApiResponse.ok("상품 삭제: 상품 삭제에 성공했습니다.", null)
        );
    }

}
