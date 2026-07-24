package com.backend.july.order.application;

import static com.backend.july.fixture.InventoryFixture.inventory;
import static com.backend.july.fixture.MemberFixture.member;
import static com.backend.july.fixture.OrderFixture.orderItems;
import static com.backend.july.fixture.ProductFixture.product;
import static org.assertj.core.api.Assertions.assertThat;

import com.backend.july.fixture.ProductFixture;
import com.backend.july.inventory.domain.Inventory;
import com.backend.july.inventory.exception.InventoryException;
import com.backend.july.inventory.infrastructure.InventoryRepository;
import com.backend.july.member.domain.Member;
import com.backend.july.member.infrastructure.MemberRepository;
import com.backend.july.order.infrastructure.PurchaseOrderRepository;
import com.backend.july.product.domain.Product;
import com.backend.july.product.infrastructure.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev")
@SpringBootTest
@DisplayName("주문 생성 동시성 통합 테스트")
class CreateOrderConcurrencyIntegrationTest {

    private static final int INITIAL_INVENTORY = 10;
    private static final int CONCURRENT_REQUESTS = 200;
    private static final int ORDER_QUANTITY = 1;

    /*
     * 동시성 문제는 비결정적이므로 여러 번 실행한다.
     * 너무 크게 설정하면 테스트 시간이 길어질 수 있다.
     */
    private static final int MAX_ROUNDS = 10;

    private static final long READY_TIMEOUT_SECONDS = 10;
    private static final long COMPLETION_TIMEOUT_SECONDS = 30;

    private final CreateOrderService createOrderService;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    CreateOrderConcurrencyIntegrationTest(
            CreateOrderService createOrderService,
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            MemberRepository memberRepository,
            PurchaseOrderRepository purchaseOrderRepository
    ) {
        this.createOrderService = createOrderService;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Disabled
    @Test
    @DisplayName("동시성 제어가 없으면 주문 수량과 재고의 불변식이 깨질 수 있다.")
    void stock_consistency_can_be_broken_without_concurrency_control() throws Exception {

        boolean inconsistencyDetected = false;
        List<ConcurrencyResult> roundResults = new ArrayList<>();

        for (int round = 1; round <= MAX_ROUNDS; round++) {
            ConcurrencyTestData testData = createTestData(round);
            ConcurrencyResult result = executeConcurrentOrders(testData);

            roundResults.add(result);
            printResult(round, result);

            assertThat(result.savedOrderCount())
                    .as("저장된 주문 수는 성공 응답 수와 같아야 한다.")
                    .isEqualTo(result.successCount());

            assertThat(result.orderedQuantity())
                    .as("요청당 수량이 1이면 주문 수량 합계와 성공 수가 같아야 한다.")
                    .isEqualTo((long) result.successCount() * ORDER_QUANTITY);

            if (!result.isInventoryConsistent()) {
                inconsistencyDetected = true;
                break;
            }
        }

        assertThat(inconsistencyDetected)
                .as("""
                                %d회 실행하는 동안 lost update가 재현되지 않았습니다.
                                
                                실행 결과:
                                %s
                                
                                동시성 오류는 실행 타이밍에 따라 재현되지 않을 수 있습니다.
                                재현되지 않으면 DB 커넥션 풀을 늘리거나,
                                일반 재고 조회 직후 테스트용 지연을 추가하십시오.
                                """,
                        MAX_ROUNDS,
                        roundResults
                )
                .isTrue();
    }

    private ConcurrencyTestData createTestData(int round) {
        Product savedProduct = productRepository.save(
                product("동시성 테스트 상품-" + round, ProductFixture.DEFAULT_PRODUCT_PRICE)
        );

        Inventory savedInventory = inventoryRepository.save(inventory(savedProduct, INITIAL_INVENTORY));

        List<Member> savedMembers = createMembers(round, CONCURRENT_REQUESTS);

        return new ConcurrencyTestData(
                savedProduct.getId(),
                savedInventory.getId(),
                savedMembers.stream()
                        .map(Member::getId)
                        .toList()
        );
    }

    private List<Member> createMembers(int round, int count) {
        List<Member> members = new ArrayList<>(count);

        for (int index = 0; index < count; index++) {
            /*
             * round까지 넘기는 이유:
             * 여러 라운드를 실행해도 회원의 unique 필드가 충돌하지 않게 하기 위함.
             *
             * MemberFixture의 실제 시그니처에 따라
             * member(round, index) 형태로 만들어도 된다.
             */
            int sequence = round * 10_000 + index;

            members.add(memberRepository.save(member(sequence)));
        }

        return members;
    }

    private ConcurrencyResult executeConcurrentOrders(ConcurrencyTestData testData) throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);

        CountDownLatch readyLatch = new CountDownLatch(CONCURRENT_REQUESTS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_REQUESTS);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger inventoryFailureCount = new AtomicInteger();

        ConcurrentLinkedQueue<Throwable> unexpectedExceptions = new ConcurrentLinkedQueue<>();

        try {
            for (Long memberId : testData.memberIds()) {
                executorService.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        createOrderService.create(memberId,
                                orderItems(testData.productId(), ORDER_QUANTITY));

                        successCount.incrementAndGet();
                    } catch (InventoryException exception) {
                        inventoryFailureCount.incrementAndGet();
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        unexpectedExceptions.add(exception);
                    } catch (Throwable throwable) {
                        unexpectedExceptions.add(throwable);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            boolean allWorkersReady = readyLatch.await(READY_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            assertThat(allWorkersReady)
                    .as("모든 작업 스레드가 제한 시간 내 준비되어야 한다.")
                    .isTrue();

            long startedAt = System.nanoTime();

            startLatch.countDown();

            boolean allWorkersCompleted = doneLatch.await(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

            assertThat(allWorkersCompleted)
                    .as("모든 주문 요청이 제한 시간 내 완료되어야 한다.")
                    .isTrue();

            assertThat(unexpectedExceptions)
                    .as(
                            "예상하지 못한 예외가 없어야 한다: %s",
                            summarizeExceptions(unexpectedExceptions)
                    )
                    .isEmpty();

            /*
             * 테스트 실행 스레드에서는 주문 서비스의 영속성 컨텍스트를
             * 공유하지 않는다. Repository 재조회로 DB의 최종 값을 읽는다.
             */
            Inventory finalInventory = inventoryRepository.findByProductId(testData.productId())
                    .orElseThrow();

            long savedOrderCount = purchaseOrderRepository.countByProductId(testData.productId());

            long orderedQuantity = purchaseOrderRepository.sumOrderedQuantityByProductId(
                    testData.productId());

            return new ConcurrencyResult(
                    INITIAL_INVENTORY,
                    CONCURRENT_REQUESTS,
                    successCount.get(),
                    inventoryFailureCount.get(),
                    savedOrderCount,
                    orderedQuantity,
                    finalInventory.getQuantity(),
                    elapsedMillis
            );
        } finally {
            executorService.shutdownNow();
            boolean terminated = executorService.awaitTermination(5, TimeUnit.SECONDS);

            if (!terminated) {
                System.err.println("ExecutorService가 정상적으로 종료되지 않았습니다.");
            }
        }
    }

    private static String summarizeExceptions(ConcurrentLinkedQueue<Throwable> exceptions) {
        return exceptions.stream()
                .map(exception ->
                        exception.getClass().getSimpleName()
                                + ": "
                                + exception.getMessage()
                )
                .toList()
                .toString();
    }

    private static void printResult(int round, ConcurrencyResult result) {
        System.out.printf("""
                        
                        ===== 주문 동시성 테스트 %d회차 =====
                        초기 재고          : %d
                        동시 요청          : %d
                        성공 요청          : %d
                        재고 부족 실패     : %d
                        저장 주문          : %d
                        주문된 총수량      : %d
                        최종 재고          : %d
                        주문수량 + 재고    : %d
                        정합성 유지 여부   : %s
                        실행 시간          : %d ms
                        ====================================
                        """,
                round,
                result.initialInventory(),
                result.concurrentRequests(),
                result.successCount(),
                result.inventoryFailureCount(),
                result.savedOrderCount(),
                result.orderedQuantity(),
                result.finalInventory(),
                result.orderedQuantity()
                        + result.finalInventory(),
                result.isInventoryConsistent(),
                result.elapsedMillis()
        );
    }

    private record ConcurrencyTestData(
            Long productId,
            Long inventoryId,
            List<Long> memberIds
    ) {

    }

    private record ConcurrencyResult(
            int initialInventory,
            int concurrentRequests,
            int successCount,
            int inventoryFailureCount,
            long savedOrderCount,
            long orderedQuantity,
            int finalInventory,
            long elapsedMillis
    ) {

        boolean isInventoryConsistent() {
            return orderedQuantity + finalInventory
                    == initialInventory;
        }
    }

//    @Disabled
    @Test
    @DisplayName("비관적 락을 적용하면 동시 주문에서도 재고 정합성이 유지된다.")
    void stock_consistency_is_maintained_with_pessimistic_lock()
            throws Exception {

        for (int round = 1; round <= MAX_ROUNDS; round++) {
            ConcurrencyTestData testData = createTestData(round);

            ConcurrencyResult result =
                    executeConcurrentOrders(testData);

            printResult(round, result);

            assertThat(result.successCount())
                    .as("%d회차 성공 주문 수", round)
                    .isEqualTo(INITIAL_INVENTORY);

            assertThat(result.inventoryFailureCount())
                    .as("%d회차 재고 부족 실패 수", round)
                    .isEqualTo(CONCURRENT_REQUESTS - INITIAL_INVENTORY);

            assertThat(result.savedOrderCount())
                    .as("%d회차 저장 주문 수", round)
                    .isEqualTo(INITIAL_INVENTORY);

            assertThat(result.orderedQuantity())
                    .as("%d회차 총 주문 수량", round)
                    .isEqualTo(INITIAL_INVENTORY);

            assertThat(result.finalInventory())
                    .as("%d회차 최종 재고", round)
                    .isZero();

            assertThat(result.isInventoryConsistent())
                    .as("%d회차 재고 정합성이 유지되어야 한다.", round)
                    .isTrue();
        }
    }
}
