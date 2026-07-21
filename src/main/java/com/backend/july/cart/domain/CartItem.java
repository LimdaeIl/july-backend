package com.backend.july.cart.domain;

import com.backend.july.cart.exception.CartErrorCode;
import com.backend.july.cart.exception.CartException;
import com.backend.july.common.audit.BaseTimeEntity;
import com.backend.july.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v1_cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_v1_cart_items_cart_product",
                        columnNames = {"cart_id", "product_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_v1_cart_items_product_id",
                        columnList = "product_id"
                )
        }
)
@Entity
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    private CartItem(Product product, int quantity) {
        validateProduct(product);
        validateQuantity(quantity);

        this.product = product;
        this.quantity = quantity;
    }

    public static CartItem create(Product product, int quantity) {
        return new CartItem(product, quantity);
    }

    void assignCart(Cart cart) {
        validateCart(cart);

        if (this.cart != null) {
            throw new CartException(
                    CartErrorCode.CART_ITEM_ALREADY_ASSIGNED
            );
        }

        this.cart = cart;
    }

    public void changeQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    public void increaseQuantity(int additionalQuantity) {
        validateQuantity(additionalQuantity);

        if (this.quantity > Integer.MAX_VALUE - additionalQuantity) {
            throw new CartException(
                    CartErrorCode.CART_ITEM_QUANTITY_OVERFLOW
            );
        }

        this.quantity += additionalQuantity;
    }

    public Long getProductId() {
        return product.getId();
    }

    public boolean hasSameProduct(Long productId) {
        return productId != null
                && productId.equals(product.getId());
    }

    private static void validateCart(Cart cart) {
        if (cart == null) {
            throw new CartException(CartErrorCode.CART_REQUIRED);
        }
    }

    private static void validateProduct(Product product) {
        if (product == null) {
            throw new CartException(CartErrorCode.PRODUCT_REQUIRED);
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new CartException(
                    CartErrorCode.INVALID_CART_ITEM_QUANTITY
            );
        }
    }
}
