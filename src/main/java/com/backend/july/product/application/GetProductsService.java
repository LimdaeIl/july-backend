package com.backend.july.product.application;

import com.backend.july.common.response.CursorResponse;
import com.backend.july.common.response.PageResponse;
import com.backend.july.inventory.domain.Inventory;
import com.backend.july.inventory.exception.InventoryErrorCode;
import com.backend.july.inventory.exception.InventoryException;
import com.backend.july.inventory.infrastructure.InventoryRepository;
import com.backend.july.product.domain.Product;
import com.backend.july.product.domain.ProductStatus;
import com.backend.july.product.infrastructure.ProductRepository;
import com.backend.july.product.presentation.dto.ProductCursor;
import com.backend.july.product.presentation.dto.ProductSortType;
import com.backend.july.product.presentation.dto.condition.GetProductsCondition;
import com.backend.july.product.presentation.dto.condition.GetProductsCursorCondition;
import com.backend.july.product.presentation.dto.response.ProductSummaryResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getProductsByPage(GetProductsCondition condition) {
        Pageable pageable = PageRequest.of(
                condition.page(),
                condition.size(),
                condition.sortType().toSort()
        );

        Page<Product> products = findProducts(
                condition.sellerId(),
                pageable
        );
        Map<Long, Inventory> inventoryMap = getInventoryMap(products.getContent());

        return PageResponse.from(
                products,
                product -> ProductSummaryResponse.of(
                        product,
                        getInventory(inventoryMap, product.getId())
                )
        );
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

    @Transactional(readOnly = true)
    public CursorResponse<ProductSummaryResponse, ProductCursor> getProductsByCursor(
            GetProductsCursorCondition condition
    ) {
        List<Product> queriedProducts = findProducts(condition);

        boolean hasNext = queriedProducts.size() > condition.size();

        List<Product> products = extractContent(queriedProducts, condition.size(), hasNext);
        ProductCursor nextCursor = createNextCursor(products, condition.sortType(), hasNext);

        Map<Long, Inventory> inventoryMap = getInventoryMap(products);

        List<ProductSummaryResponse> content = products.stream()
                .map(product -> ProductSummaryResponse.of(
                        product,
                        getInventory(inventoryMap, product.getId())
                ))
                .toList();

        return CursorResponse.of(content, nextCursor, hasNext);
    }

    private List<Product> findProducts(
            GetProductsCursorCondition condition
    ) {
        PageRequest limit = PageRequest.of(
                0,
                condition.size() + 1
        );

        return switch (condition.sortType()) {
            case LATEST -> productRepository.findAllByLatestCursor(
                    ProductStatus.ON_SALE,
                    condition.sellerId(),
                    condition.cursorCreatedAt(),
                    condition.cursorId(),
                    limit
            );

            case PRICE_LOW -> productRepository.findAllByPriceLowCursor(
                    ProductStatus.ON_SALE,
                    condition.sellerId(),
                    condition.cursorPrice(),
                    condition.cursorId(),
                    limit
            );

            case PRICE_HIGH -> productRepository.findAllByPriceHighCursor(
                    ProductStatus.ON_SALE,
                    condition.sellerId(),
                    condition.cursorPrice(),
                    condition.cursorId(),
                    limit
            );
        };
    }

    private List<Product> extractContent(List<Product> queriedProducts, int size, boolean hasNext) {
        if (!hasNext) {
            return queriedProducts;
        }

        return new ArrayList<>(queriedProducts.subList(0, size));
    }

    private ProductCursor createNextCursor(List<Product> products, ProductSortType sortType,
            boolean hasNext) {
        if (!hasNext || products.isEmpty()) {
            return null;
        }

        Product lastProduct = products.get(products.size() - 1);

        return switch (sortType) {
            case LATEST -> ProductCursor.latest(lastProduct.getId(), lastProduct.getCreatedAt());
            case PRICE_LOW, PRICE_HIGH ->
                    ProductCursor.price(lastProduct.getId(), lastProduct.getPrice());
        };
    }

    private Map<Long, Inventory> getInventoryMap(List<Product> products) {
        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();

        return inventoryRepository.findAllByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(
                        inventory -> inventory.getProduct().getId(),
                        Function.identity()
                ));
    }

    private Inventory getInventory(Map<Long, Inventory> inventoryMap, Long productId) {
        Inventory inventory = inventoryMap.get(productId);

        if (inventory == null) {
            throw new InventoryException(InventoryErrorCode.INVENTORY_NOT_FOUND);
        }

        return inventory;
    }
}
