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
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v1_products",
        indexes = {
                @Index(name = "idx_product_status_created_at_id", columnList = "status, created_at, id"),
                @Index(name = "idx_product_status_price_id", columnList = "status, price, id")
        }
)
@Entity
public class Product extends BaseAuditEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false, precision = 19, scale = 0)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProductStatus status;

    private Product(String name, BigDecimal price) {
        this.name = validateName(name);
        this.price = validatePrice(price);
        this.status = ProductStatus.ON_SALE;
    }

    public static Product create(String name, BigDecimal price) {
        return new Product(name, price);
    }

    public void updateInformation(String name, BigDecimal price) {
        validateNotDeleted();

        this.name = validateName(name);
        this.price = validatePrice(price);
    }


    public void changeStatus(ProductStatus status) {
        if (status == null) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "status");
        }

        validateNotDeleted();

        if (status == ProductStatus.DELETED) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATUS_BY_DELETED);
        }

        this.status = status;
    }

    public boolean isDeleted() {
        return this.status == ProductStatus.DELETED;
    }

    public void validateNotDeleted() {
        if (isDeleted()) {
            throw new ProductException(ProductErrorCode.ALREADY_DELETED_PRODUCT);
        }
    }


    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "name");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() > 100) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "name");
        }

        return trimmedName;
    }

    private static BigDecimal validatePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "price");
        }

        BigDecimal normalizedPrice = price.stripTrailingZeros();

        if (normalizedPrice.scale() > 0) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "price");
        }

        return normalizedPrice;
    }

    public void delete() {
        if (this.status == ProductStatus.DELETED) {
            throw new ProductException(ProductErrorCode.ALREADY_DELETED_PRODUCT);
        }

        this.status = ProductStatus.DELETED;
    }
}
