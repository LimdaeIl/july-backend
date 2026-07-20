package com.backend.july.inventory.application;

import com.backend.july.inventory.domain.Inventory;
import com.backend.july.inventory.exception.InventoryErrorCode;
import com.backend.july.inventory.exception.InventoryException;
import com.backend.july.inventory.infrastructure.InventoryRepository;
import com.backend.july.inventory.presentation.dto.response.ChangeInventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class IncreaseInventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public ChangeInventoryResponse increase(Long productId, int quantity) {
        Inventory inventory = findInventory(productId);
        inventory.increase(quantity);
        return ChangeInventoryResponse.from(inventory);
    }

    private Inventory findInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.INVENTORY_NOT_FOUND));
    }
}