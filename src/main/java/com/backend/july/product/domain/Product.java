package com.backend.july.product.domain;

import com.backend.july.common.audit.BaseAuditEntity;
import com.backend.july.inventory.domain.Inventory;
import com.backend.july.product.exception.ProductErrorCode;
import com.backend.july.product.exception.ProductException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
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
                @Index(
                        name = "idx_product_status_created_at_id",
                        columnList = "status, created_at, id"
                ),
                @Index(
                        name = "idx_product_status_price_id",
                        columnList = "status, price, id"
                )
        }
)
@Entity
public class Product extends BaseAuditEntity {

    private static final int MAX_NAME_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(name = "price", nullable = false, precision = 19, scale = 0)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProductStatus status;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Inventory inventory;

    private Product(String name, BigDecimal price) {
        this.name = validateName(name);
        this.price = validatePrice(price);
        this.status = ProductStatus.ON_SALE;
    }

    public static Product create(String name, BigDecimal price) {
        return new Product(name, price);
    }

    public void registerInventory(int quantity) {
        validateInventoryNotRegistered();

        this.inventory = Inventory.create(this, quantity);
    }

    public void updateInformation(String name, BigDecimal price) {
        this.name = validateName(name);
        this.price = validatePrice(price);
    }

    public void stopSale() {
        if (this.status == ProductStatus.HIDDEN) {
            return;
        }

        this.status = ProductStatus.HIDDEN;
    }

    public void resumeSale() {
        if (this.status == ProductStatus.ON_SALE) {
            return;
        }

        this.status = ProductStatus.ON_SALE;
    }

    public boolean isOnSale() {
        return this.status == ProductStatus.ON_SALE;
    }

    private void validateInventoryNotRegistered() {
        if (this.inventory != null) {
            throw new ProductException(ProductErrorCode.INVENTORY_ALREADY_REGISTERED);
        }
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "name");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "name");
        }

        return trimmedName;
    }

    private static BigDecimal validatePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "price");
        }

        BigDecimal normalized = price.stripTrailingZeros();

        if (normalized.scale() > 0) {
            throw new ProductException(ProductErrorCode.VALIDATE_FIELD, "price");
        }
        return normalized;
    }
}
