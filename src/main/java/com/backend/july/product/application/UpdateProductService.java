package com.backend.july.product.application;

import com.backend.july.product.domain.Product;
import com.backend.july.product.exception.ProductErrorCode;
import com.backend.july.product.exception.ProductException;
import com.backend.july.product.infrastructure.ProductRepository;
import com.backend.july.product.presentation.dto.request.UpdateProductRequest;
import com.backend.july.product.presentation.dto.response.UpdateProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UpdateProductService {

    private final ProductRepository productRepository;

    @CacheEvict(
            cacheNames = "productDetail",
            key = "#productId"
    )
    @Transactional
    public UpdateProductResponse update(Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
        product.validateNotDeleted();
        product.updateInformation(request.name(), request.price());

        return UpdateProductResponse.from(product);
    }
}
