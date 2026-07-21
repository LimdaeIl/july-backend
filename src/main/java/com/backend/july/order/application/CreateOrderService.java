package com.backend.july.order.application;

import com.backend.july.inventory.domain.Inventory;
import com.backend.july.inventory.exception.InventoryErrorCode;
import com.backend.july.inventory.exception.InventoryException;
import com.backend.july.inventory.infrastructure.InventoryRepository;
import com.backend.july.member.domain.Member;
import com.backend.july.member.exception.MemberErrorCode;
import com.backend.july.member.exception.MemberException;
import com.backend.july.member.infrastructure.MemberRepository;
import com.backend.july.order.domain.OrderItem;
import com.backend.july.order.domain.PurchaseOrder;
import com.backend.july.order.exception.OrderErrorCode;
import com.backend.july.order.exception.OrderException;
import com.backend.july.order.infrastructure.PurchaseOrderRepository;
import com.backend.july.order.presentation.dto.request.CreateOrderRequest.OrderItemRequest;
import com.backend.july.order.presentation.dto.response.CreateOrderResponse;
import com.backend.july.product.domain.Product;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOrderService {

    private static final long ORDER_EXPIRATION_MINUTES = 10L;

    private final MemberRepository memberRepository;
    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final Clock clock;

    @Transactional
    public CreateOrderResponse create(
            Long memberId,
            List<OrderItemRequest> requestedItems
    ) {
        validateRequestedItems(requestedItems);
        validateDuplicateProducts(requestedItems);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
                );

        List<Long> productIds = requestedItems.stream()
                .map(OrderItemRequest::productId)
                .sorted()
                .toList();

        List<Inventory> inventories =
                inventoryRepository.findAllByProductIdInForUpdate(productIds);

        validateAllInventoriesExist(productIds, inventories);

        Map<Long, Inventory> inventoryByProductId = createInventoryMap(inventories);

        String orderNumber = orderNumberGenerator.generate();

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiresAt =
                now.plusMinutes(ORDER_EXPIRATION_MINUTES);

        PurchaseOrder order = PurchaseOrder.create(
                orderNumber,
                member,
                expiresAt
        );

        for (OrderItemRequest requestedItem : requestedItems) {
            Inventory inventory =
                    inventoryByProductId.get(requestedItem.productId());

            Product product = inventory.getProduct();

            product.validateOrderable();
            inventory.decrease(requestedItem.quantity());

            OrderItem orderItem = OrderItem.create(
                    product,
                    requestedItem.quantity()
            );

            order.addItem(orderItem);
        }

        order.validateOrderReady();

        PurchaseOrder savedOrder =
                purchaseOrderRepository.save(order);

        return CreateOrderResponse.from(savedOrder);
    }

    private static void validateRequestedItems(
            List<OrderItemRequest> requestedItems
    ) {
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new OrderException(
                    OrderErrorCode.ORDER_ITEMS_EMPTY
            );
        }
    }

    private static void validateDuplicateProducts(
            List<OrderItemRequest> requestedItems
    ) {
        Set<Long> productIds = new HashSet<>();

        for (OrderItemRequest requestedItem : requestedItems) {
            if (!productIds.add(requestedItem.productId())) {
                throw new OrderException(
                        OrderErrorCode.DUPLICATE_ORDER_ITEM
                );
            }
        }
    }

    private static void validateAllInventoriesExist(
            List<Long> requestedProductIds,
            List<Inventory> inventories
    ) {
        if (inventories.size() != requestedProductIds.size()) {
            throw new InventoryException(
                    InventoryErrorCode.INVENTORY_NOT_FOUND
            );
        }
    }

    private static Map<Long, Inventory> createInventoryMap(
            List<Inventory> inventories
    ) {
        Map<Long, Inventory> inventoryByProductId = new HashMap<>();

        for (Inventory inventory : inventories) {
            inventoryByProductId.put(
                    inventory.getProduct().getId(),
                    inventory
            );
        }

        return inventoryByProductId;
    }
}
