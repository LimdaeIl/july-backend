package com.backend.july.product.infrastructure;

import com.backend.july.product.domain.Product;
import com.backend.july.product.domain.ProductStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.status = :status
              AND (
                    :sellerId IS NULL
                    OR p.createdBy = :sellerId
              )
              AND (
                    :cursorCreatedAt IS NULL
                    OR p.createdAt < :cursorCreatedAt
                    OR (
                        p.createdAt = :cursorCreatedAt
                        AND p.id < :cursorId
                    )
              )
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Product> findAllByLatestCursor(
            @Param("status")
            ProductStatus status,

            @Param("sellerId")
            Long sellerId,

            @Param("cursorCreatedAt")
            Instant cursorCreatedAt,

            @Param("cursorId")
            Long cursorId,

            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.status = :status
              AND (
                    :sellerId IS NULL
                    OR p.createdBy = :sellerId
              )
              AND (
                    :cursorPrice IS NULL
                    OR p.price > :cursorPrice
                    OR (
                        p.price = :cursorPrice
                        AND p.id > :cursorId
                    )
              )
            ORDER BY p.price ASC, p.id ASC
            """)
    List<Product> findAllByPriceLowCursor(
            @Param("status")
            ProductStatus status,

            @Param("sellerId")
            Long sellerId,

            @Param("cursorPrice")
            BigDecimal cursorPrice,

            @Param("cursorId")
            Long cursorId,

            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.status = :status
              AND (
                    :sellerId IS NULL
                    OR p.createdBy = :sellerId
              )
              AND (
                    :cursorPrice IS NULL
                    OR p.price < :cursorPrice
                    OR (
                        p.price = :cursorPrice
                        AND p.id < :cursorId
                    )
              )
            ORDER BY p.price DESC, p.id DESC
            """)
    List<Product> findAllByPriceHighCursor(
            @Param("status")
            ProductStatus status,

            @Param("sellerId")
            Long sellerId,

            @Param("cursorPrice")
            BigDecimal cursorPrice,

            @Param("cursorId")
            Long cursorId,

            Pageable pageable
    );

    Optional<Product> findByIdAndStatus(Long productId, ProductStatus status);

}
