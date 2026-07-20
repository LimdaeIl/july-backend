package com.backend.july.product.application;

import com.backend.july.inventory.domain.Inventory;
import com.backend.july.inventory.infrastructure.InventoryRepository;
import com.backend.july.product.domain.Product;
import com.backend.july.product.infrastructure.ProductRepository;
import com.backend.july.product.presentation.dto.request.CreateProductRequest;
import com.backend.july.product.presentation.dto.response.CreateProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CreateProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public CreateProductResponse create(CreateProductRequest request) {
        Product product = Product.create(request.name(), request.price());
        Product savedProduct = productRepository.save(product);

        Inventory inventory = Inventory.create(savedProduct, request.initialQuantity());
        inventoryRepository.save(inventory);

        return CreateProductResponse.from(savedProduct);
    }
}
