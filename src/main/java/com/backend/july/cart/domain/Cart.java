package com.backend.july.cart.domain;

import com.backend.july.cart.exception.CartErrorCode;
import com.backend.july.cart.exception.CartException;
import com.backend.july.common.audit.BaseAuditEntity;
import com.backend.july.member.domain.Member;
import com.backend.july.product.domain.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v1_carts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_v1_carts_member_id",
                        columnNames = "member_id"
                )
        }
)
@Entity
public class Cart extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Getter(AccessLevel.NONE)
    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItem> items = new ArrayList<>();

    private Cart(Member member) {
        validateMember(member);
        this.member = member;
    }

    public static Cart create(Member member) {
        return new Cart(member);
    }

    public CartItem addProduct(Product product, int quantity) {
        validateProduct(product);
        validateQuantity(quantity);

        CartItem existingItem = findItemByProductId(
                product.getId()
        );

        if (existingItem != null) {
            existingItem.increaseQuantity(quantity);
            return existingItem;
        }

        CartItem newItem = CartItem.create(
                product,
                quantity
        );

        newItem.assignCart(this);
        items.add(newItem);

        return newItem;
    }

    public void removeItem(CartItem cartItem) {
        validateCartItem(cartItem);

        boolean removed = items.remove(cartItem);

        if (!removed) {
            throw new CartException(
                    CartErrorCode.CART_ITEM_NOT_OWNED
            );
        }
    }

    public int clear() {
        int deletedItemCount = items.size();

        items.clear();

        return deletedItemCount;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isOwnedBy(Long memberId) {
        return memberId != null
                && memberId.equals(member.getId());
    }

    private CartItem findItemByProductId(Long productId) {
        if (productId == null) {
            return null;
        }

        return items.stream()
                .filter(item -> item.hasSameProduct(productId))
                .findFirst()
                .orElse(null);
    }

    private static void validateMember(Member member) {
        if (member == null) {
            throw new CartException(
                    CartErrorCode.MEMBER_REQUIRED
            );
        }
    }

    private static void validateProduct(Product product) {
        if (product == null) {
            throw new CartException(
                    CartErrorCode.PRODUCT_REQUIRED
            );
        }
    }

    private static void validateCartItem(CartItem cartItem) {
        if (cartItem == null) {
            throw new CartException(
                    CartErrorCode.CART_ITEM_REQUIRED
            );
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
