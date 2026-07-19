package com.backend.july.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByStatus(
            ProductStatus status,
            Pageable pageable
    );

    Page<Product> findAllByStatusAndCreatedBy(
            ProductStatus status,
            Long createdBy,
            Pageable pageable
    );
}
