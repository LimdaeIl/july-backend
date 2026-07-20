package com.backend.july.inventory.presentation.dto.response;

import com.backend.july.inventory.domain.Inventory;

public record ChangeInventoryResponse(Long inventoryId, Long productId, int quantity) {

    public static ChangeInventoryResponse from(Inventory inventory) {
        return new ChangeInventoryResponse(inventory.getId(), inventory.getProduct().getId(),
                inventory.getQuantity());
    }
}
