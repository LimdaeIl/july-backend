package com.backend.july.cart.infrastructure;

import com.backend.july.cart.domain.Cart;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByMemberId(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM Cart c
            WHERE c.member.id = :memberId
            """)
    Optional<Cart> findByMemberIdForUpdate(
            @Param("memberId") Long memberId
    );

    @Query("""
            SELECT DISTINCT c
            FROM Cart c
            LEFT JOIN FETCH c.items ci
            LEFT JOIN FETCH ci.product
            WHERE c.member.id = :memberId
            """)
    Optional<Cart> findDetailByMemberId(
            @Param("memberId") Long memberId
    );
}
