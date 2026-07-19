package com.backend.july.product.domain;

import com.backend.july.common.audit.BaseAuditEntity;
import com.backend.july.product.exception.ProductErrorCode;
import com.backend.july.product.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "v1_products")
@Entity
public class Product extends BaseAuditEntity {

    private static final int MAX_NAME_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;

    private Product(String name, BigDecimal price, ProductStatus status) {
        this.name = validateName(name);
        this.price = validatePrice(price);
        this.status = validateStatus(status);
    }

    public static Product create(String name, BigDecimal price, ProductStatus status) {
        return new Product(name, price, status);
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "name");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "name");
        }

        return trimmedName;
    }

    private BigDecimal validatePrice(BigDecimal price) {
        if (price == null) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "price");
        }

        if (price.signum() < 0) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "price");
        }

        return price;
    }

    private ProductStatus validateStatus(ProductStatus status) {
        if (status == null) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "status");
        }

        return status;
    }
}
