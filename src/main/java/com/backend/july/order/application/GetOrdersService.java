package com.backend.july.order.application;

import com.backend.july.common.response.CursorResponse;
import com.backend.july.order.domain.OrderStatus;
import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.order.infrastructure.PurchaseOrderRepository;
import com.backend.july.order.presentation.dto.response.OrderSummaryResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
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
        List<PurchaseOrder> orders = purchaseOrderRepository.findAllWithItemsByIdIn(contentOrderIds);

        /*
         * IN 절 조회 결과는 순서가 보장되지 않으므로,
         * ID를 Key로 Map을 만든 뒤 contentOrderIds 순서대로 재정렬한다.
         */
        Map<Long, PurchaseOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(PurchaseOrder::getId, Function.identity()));

        List<OrderSummaryResponse> content = contentOrderIds.stream()
                .map(orderMap::get)
                .filter(Objects::nonNull) // null 안전 처리
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