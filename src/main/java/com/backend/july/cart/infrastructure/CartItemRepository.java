package com.backend.july.cart.infrastructure;

import com.backend.july.cart.domain.CartItem;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ci
            FROM CartItem ci
            JOIN FETCH ci.cart c
            JOIN FETCH ci.product
            WHERE ci.id = :cartItemId
              AND c.member.id = :memberId
            """)
    Optional<CartItem> findByIdAndMemberIdForUpdate(
            @Param("cartItemId") Long cartItemId,
            @Param("memberId") Long memberId
    );
}
