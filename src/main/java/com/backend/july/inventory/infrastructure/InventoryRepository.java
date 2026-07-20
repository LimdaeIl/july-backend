package com.backend.july.inventory.infrastructure;

import com.backend.july.inventory.domain.Inventory;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    List<Inventory> findAllByProductIdIn(Collection<Long> productIds);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM Inventory i
            JOIN FETCH i.product
            WHERE i.product.id = :productId
            """)
    Optional<Inventory> findByProductIdForUpdate(
            @Param("productId") Long productId
    );

}
