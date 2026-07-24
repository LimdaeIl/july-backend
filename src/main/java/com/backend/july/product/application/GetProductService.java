package com.backend.july.product.application;

import com.backend.july.inventory.domain.Inventory;
import com.backend.july.inventory.exception.InventoryErrorCode;
import com.backend.july.inventory.exception.InventoryException;
import com.backend.july.inventory.infrastructure.InventoryRepository;
import com.backend.july.product.domain.Product;
import com.backend.july.product.domain.ProductStatus;
import com.backend.july.product.exception.ProductErrorCode;
import com.backend.july.product.exception.ProductException;
import com.backend.july.product.infrastructure.ProductRepository;
import com.backend.july.product.presentation.dto.response.ProductDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GetProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Cacheable(
            cacheNames = "productDetail",
            key = "#productId"
    )
    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(Long productId) {
        Product product = productRepository
                .findByIdAndStatus(productId, ProductStatus.ON_SALE)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        return ProductDetailResponse.of(product, inventory);
    }
}
