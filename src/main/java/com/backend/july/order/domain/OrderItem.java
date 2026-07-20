package com.backend.july.order.domain;

import com.backend.july.order.exception.OrderErrorCode;
import com.backend.july.order.exception.OrderException;
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
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v1_order_items",
        indexes = {
                @Index(name = "idx_order_item_order_id", columnList = "order_id"),
                @Index(name = "idx_order_item_product_id", columnList = "product_id")
        }
)
@Entity
public class OrderItem {

    private static final int MIN_ORDER_QUANTITY = 1;
    private static final BigDecimal MIN_PRICE = BigDecimal.ZERO;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "order_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal orderPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineAmount;

    private OrderItem(Product product, String productName, BigDecimal orderPrice, int quantity) {
        validateProduct(product);
        validateProductName(productName);
        validateOrderPrice(orderPrice);
        validateQuantity(quantity);

        this.product = product;
        this.productName = productName;
        this.orderPrice = orderPrice;
        this.quantity = quantity;
        this.lineAmount = calculateLineAmount(orderPrice, quantity);
    }

    public static OrderItem create(Product product, String productName, BigDecimal orderPrice,
            int quantity
    ) {
        return new OrderItem(product, productName, orderPrice, quantity);
    }

    public static OrderItem create(Product product, int quantity) {
        validateProduct(product);
        return new OrderItem(product, product.getName(), product.getPrice(), quantity);
    }

    void assignOrder(PurchaseOrder order) {
        validateOrder(order);

        if (this.order != null && this.order != order) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_ALREADY_ASSIGNED);
        }

        this.order = order;
    }

    public Long getProductId() {
        return product.getId();
    }

    public boolean hasSameProduct(Long productId) {
        if (productId == null) {
            return false;
        }

        return productId.equals(product.getId());
    }

    public boolean belongsTo(PurchaseOrder order) {
        if (order == null || this.order == null) {
            return false;
        }

        return this.order == order;
    }

    private static BigDecimal calculateLineAmount(BigDecimal orderPrice, int quantity) {
        try {
            return orderPrice.multiply(
                    BigDecimal.valueOf(quantity)
            );
        } catch (ArithmeticException exception) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_AMOUNT_OVERFLOW);
        }
    }

    private static void validateOrder(PurchaseOrder order) {
        if (order == null) {
            throw new OrderException(OrderErrorCode.ORDER_REQUIRED);
        }
    }

    private static void validateProduct(Product product) {
        if (product == null) {
            throw new OrderException(OrderErrorCode.PRODUCT_REQUIRED);
        }
    }

    private static void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new OrderException(OrderErrorCode.PRODUCT_NAME_REQUIRED);
        }
    }

    private static void validateOrderPrice(BigDecimal orderPrice) {
        if (orderPrice == null) {
            throw new OrderException(OrderErrorCode.ORDER_PRICE_REQUIRED);
        }

        if (orderPrice.compareTo(MIN_PRICE) < 0) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_PRICE);
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity < MIN_ORDER_QUANTITY) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_QUANTITY);
        }
    }
}
