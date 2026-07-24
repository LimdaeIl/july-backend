package com.backend.july.fixture;

import com.backend.july.inventory.domain.Inventory;
import com.backend.july.product.domain.Product;
import org.springframework.test.util.ReflectionTestUtils;

public final class InventoryFixture {

    public static final Long DEFAULT_INVENTORY_ID = 1L;
    public static final int DEFAULT_QUANTITY = 100;

    private InventoryFixture() {
    }

    public static Inventory inventory(Product product) {
        return inventory(product, DEFAULT_QUANTITY);
    }

    public static Inventory inventory(
            Product product,
            int quantity
    ) {
        return Inventory.create(product, quantity);
    }

    public static Inventory inventoryWithId(
            Long inventoryId,
            Product product,
            int quantity
    ) {
        Inventory inventory = Inventory.create(product, quantity);

        ReflectionTestUtils.setField(
                inventory,
                "id",
                inventoryId
        );

        return inventory;
    }
}
