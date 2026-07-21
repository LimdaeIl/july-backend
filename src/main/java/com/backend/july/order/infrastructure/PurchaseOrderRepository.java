package com.backend.july.order.infrastructure;


import com.backend.july.order.domain.PurchaseOrder;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {


    @Query("""
        SELECT DISTINCT o
        FROM PurchaseOrder o
        JOIN FETCH o.orderItems oi
        WHERE o.id = :orderId
          AND o.member.id = :memberId
        """)
    Optional<PurchaseOrder> findDetailByIdAndMemberId(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT DISTINCT o
        FROM PurchaseOrder o
        JOIN FETCH o.orderItems oi
        WHERE o.id = :orderId
          AND o.member.id = :memberId
        """)
    Optional<PurchaseOrder> findByIdAndMemberIdForUpdate(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId
    );
}