package com.backend.july.order.infrastructure;


import com.backend.july.order.domain.OrderStatus;
import com.backend.july.order.domain.PurchaseOrder;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
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

    @Query("""
            SELECT o.id
            FROM PurchaseOrder o
            WHERE o.member.id = :memberId
              AND (:status IS NULL OR o.status = :status)
              AND (:cursor IS NULL OR o.id < :cursor)
              AND (
                    :keyword IS NULL
                    OR LOWER(o.orderNumber)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR EXISTS (
                        SELECT 1
                        FROM OrderItem oi
                        WHERE oi.order = o
                          AND LOWER(oi.productName)
                              LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
              )
            ORDER BY o.id DESC
            """)
    List<Long> findOrderIdsByCursor(
            @Param("memberId") Long memberId,
            @Param("status") OrderStatus status,
            @Param("keyword") String keyword,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT o
            FROM PurchaseOrder o
            LEFT JOIN FETCH o.orderItems oi
            WHERE o.id IN :orderIds
            """)
    List<PurchaseOrder> findAllWithItemsByIdIn(
            @Param("orderIds") List<Long> orderIds
    );
}
