package com.backend.july.product.application;

import com.backend.july.common.response.PageResponse;
import com.backend.july.product.domain.Product;
import com.backend.july.product.domain.ProductRepository;
import com.backend.july.product.domain.ProductStatus;
import com.backend.july.product.presentation.dto.condition.GetProductsCondition;
import com.backend.july.product.presentation.dto.response.ProductSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GetProductsService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getProducts(GetProductsCondition condition) {
        Pageable pageable = PageRequest.of(
                condition.page(),
                condition.size(),
                condition.sortType().toSort()
        );

        Page<Product> products = findProducts(
                condition.sellerId(),
                pageable
        );

        return PageResponse.from(products, ProductSummaryResponse::from);
    }

    private Page<Product> findProducts(Long sellerId, Pageable pageable) {
        if (sellerId == null) {
            return productRepository.findAllByStatus(ProductStatus.ON_SALE, pageable);
        }

        return productRepository.findAllByStatusAndCreatedBy(
                ProductStatus.ON_SALE,
                sellerId,
                pageable
        );
    }
}
