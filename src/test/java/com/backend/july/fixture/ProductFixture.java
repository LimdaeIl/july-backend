package com.backend.july.fixture;

import com.backend.july.product.domain.Product;
import com.backend.july.product.presentation.dto.request.CreateProductRequest;
import java.math.BigDecimal;
import org.springframework.test.util.ReflectionTestUtils;

public final class ProductFixture {

    public static final Long DEFAULT_PRODUCT_ID = 1L;
    public static final String DEFAULT_PRODUCT_NAME = "테스트 상품";
    public static final BigDecimal DEFAULT_PRODUCT_PRICE = BigDecimal.valueOf(10_000);
    public static final int DEFAULT_INITIAL_QUANTITY = 100;

    private ProductFixture() {
    }

    public static CreateProductRequest createProductRequest() {
        return createProductRequest(
                DEFAULT_PRODUCT_NAME,
                DEFAULT_PRODUCT_PRICE,
                DEFAULT_INITIAL_QUANTITY
        );
    }

    public static CreateProductRequest createProductRequest(String name, BigDecimal price,
            int initQuantity) {
        return new CreateProductRequest(name, price, initQuantity);
    }

    public static Product product() {
        return product(
                DEFAULT_PRODUCT_NAME,
                DEFAULT_PRODUCT_PRICE
        );
    }

    public static Product product(String name, BigDecimal price) {
        return Product.create(name, price);
    }

    public static Product productWithId() {
        return productWithId(
                DEFAULT_PRODUCT_ID,
                DEFAULT_PRODUCT_NAME,
                DEFAULT_PRODUCT_PRICE
        );
    }

    public static Product productWithId(Long productId) {
        return productWithId(
                productId,
                DEFAULT_PRODUCT_NAME,
                DEFAULT_PRODUCT_PRICE
        );
    }

    public static Product productWithId(Long productId, String name, BigDecimal price) {
        Product product = Product.create(name, price);
        ReflectionTestUtils.setField(product, "id", productId);

        return product;
    }





}
