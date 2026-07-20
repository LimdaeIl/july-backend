package com.backend.july.inventory.domain;

import com.backend.july.inventory.exception.InventoryErrorCode;
import com.backend.july.inventory.exception.InventoryException;
import com.backend.july.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v1_inventories",
        indexes = {
                @Index(
                        name = "idx_inventory_product_id",
                        columnList = "product_id",
                        unique = true
                )
        }
)
@Entity
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Version
    private Long version;

    private Inventory(Product product, int quantity) {
        validateProduct(product);
        validateQuantity(quantity);

        this.product = product;
        this.quantity = quantity;
    }

    public static Inventory create(Product product, int initialQuantity) {
        return new Inventory(product, initialQuantity);
    }

    public void decrease(int decreaseQuantity) {
        validatePositiveQuantity(decreaseQuantity);

        if (this.quantity < decreaseQuantity) {
            throw new InventoryException(InventoryErrorCode.INVENTORY_INSUFFICIENT, this.quantity,
                    decreaseQuantity);
        }
        this.quantity -= decreaseQuantity;
    }

    public void increase(int increaseQuantity) {
        validatePositiveQuantity(increaseQuantity);

        try {
            this.quantity = Math.addExact(
                    this.quantity,
                    increaseQuantity
            );
        } catch (ArithmeticException exception) {
            throw new InventoryException(
                    InventoryErrorCode.INVENTORY_OVERFLOW, this.quantity, increaseQuantity);
        }
    }

    // 운영자가 재고를 특정 값으로 직접 조정할 때 사용한다.
    public void adjust(int newQuantity) {
        validateQuantity(newQuantity);

        this.quantity = newQuantity;
    }

    public boolean isAvailable(int requestedQuantity) {
        if (requestedQuantity <= 0) {
            return false;
        }

        return this.quantity >= requestedQuantity;
    }

    public boolean isOutOfStock() {
        return this.quantity == 0;
    }

    public boolean belongsTo(Long productId) {
        if (productId == null) {
            return false;
        }

        return productId.equals(this.product.getId());
    }

    private static void validateProduct(Product product) {
        if (product == null) {
            throw new InventoryException(InventoryErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new InventoryException(InventoryErrorCode.INVALID_QUANTITY);
        }
    }

    private static void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InventoryException(InventoryErrorCode.INVALID_CHANGE_QUANTITY);
        }
    }
}
