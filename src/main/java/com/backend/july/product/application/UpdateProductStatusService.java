package com.backend.july.product.application;

import com.backend.july.product.domain.Product;
import com.backend.july.product.domain.ProductStatus;
import com.backend.july.product.exception.ProductErrorCode;
import com.backend.july.product.exception.ProductException;
import com.backend.july.product.infrastructure.ProductRepository;
import com.backend.july.product.presentation.dto.request.UpdateProductStatusRequest;
import com.backend.july.product.presentation.dto.response.UpdateProductStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UpdateProductStatusService {

    private final ProductRepository productRepository;

    @Transactional
    public UpdateProductStatusResponse update(Long productId, UpdateProductStatusRequest request) {
        Product product = findProduct(productId);

        product.changeStatus(
                switch (request.status()) {
                    case ON_SALE -> ProductStatus.ON_SALE;
                    case HIDDEN -> ProductStatus.HIDDEN;
                }
        );

        return UpdateProductStatusResponse.from(product);
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
}