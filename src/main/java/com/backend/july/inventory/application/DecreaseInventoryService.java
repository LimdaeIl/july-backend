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
public class DecreaseInventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public ChangeInventoryResponse decrease(Long productId, int quantity) {
        Inventory inventory = findInventory(productId);
        inventory.decrease(quantity);
        return ChangeInventoryResponse.from(inventory);
    }

    private Inventory findInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException(InventoryErrorCode.INVENTORY_NOT_FOUND));
    }
}