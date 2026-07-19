package com.backend.july.product.presentation;


import com.backend.july.common.response.ApiResponse;
import com.backend.july.product.application.CreateProductService;
import com.backend.july.product.presentation.dto.request.CreateProductRequest;
import com.backend.july.product.presentation.dto.response.CreateProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/products")
@RestController
public class AdminProductController {

    private final CreateProductService createProductService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateProductResponse>> create(
            @RequestBody @Valid CreateProductRequest request
    ) {
        CreateProductResponse response = createProductService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "상품 생성: 상품 생성에 성공했습니다.",
                        response)
                );
    }

}
