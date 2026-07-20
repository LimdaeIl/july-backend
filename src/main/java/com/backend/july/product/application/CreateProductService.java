package com.backend.july.product.application;

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

    @Transactional
    public CreateProductResponse create(CreateProductRequest request) {
        Product product = Product.create(
                request.name(),
                request.price()
        );

        Product savedProduct = productRepository.save(product);

        return CreateProductResponse.from(savedProduct);
    }
}
