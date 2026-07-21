package com.backend.july.order.application;

import com.backend.july.common.response.CursorResponse;
import com.backend.july.order.domain.OrderStatus;
import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.order.infrastructure.PurchaseOrderRepository;
import com.backend.july.order.presentation.dto.response.OrderSummaryResponse;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GetOrdersService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    @Transactional(readOnly = true)
    public CursorResponse<OrderSummaryResponse, Long> get(Long memberId, OrderStatus status,
            String keyword, Long cursor, int size) {
        String normalizedKeyword = normalizeKeyword(keyword);

        /*
         * 요청한 크기보다 1개 더 조회해서
         * 다음 페이지 존재 여부를 판단한다.
         */
        List<Long> fetchedOrderIds = purchaseOrderRepository.findOrderIdsByCursor(memberId, status,
                normalizedKeyword, cursor, PageRequest.of(0, size + 1));

        boolean hasNext = fetchedOrderIds.size() > size;

        List<Long> contentOrderIds = hasNext
                ? fetchedOrderIds.subList(0, size)
                : fetchedOrderIds;

        if (contentOrderIds.isEmpty()) {
            return CursorResponse.of(List.of(), null, false);
        }

        /*
         * 주문 ID 조회와 OrderItem fetch join 조회를 분리한다.
         * 컬렉션 fetch join에 직접 페이징을 적용하는 문제를 방지한다.
         */
        List<PurchaseOrder> orders = purchaseOrderRepository.findAllWithItemsByIdIn(
                contentOrderIds);

        /*
         * IN 절의 결과 순서는 보장되지 않으므로
         * 최초 커서 조회 기준과 동일하게 재정렬한다.
         */
        orders.sort(Comparator.comparing(PurchaseOrder::getId).reversed());

        List<OrderSummaryResponse> content = orders.stream()
                .map(OrderSummaryResponse::from)
                .toList();

        Long nextCursor = hasNext
                ? contentOrderIds.get(contentOrderIds.size() - 1)
                : null;

        return CursorResponse.of(content, nextCursor, hasNext);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }
}