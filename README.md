# JULY

> **상품·주문·재고 흐름에서 발생하는 동시성 문제와 조회 병목을 직접 재현하고 개선한 이커머스 백엔드 프로젝트**

JULY는 상품 조회부터 장바구니, 주문, 재고 관리까지 이커머스의 핵심 비즈니스 흐름을 구현한 개인 백엔드 프로젝트입니다.

기능 구현에 그치지 않고 실제 서비스에서 발생할 수 있는 **재고 동시성 문제, 반복 조회에 따른 DB 부하, 데이터 증가에 따른 조회 성능 저하**를 직접 재현하고 분석하는 데 중점을 두었습니다.

200개의 동시 주문을 통해 **Lost Update**를 재현하고 JPA 비관적 락과 일관된 Lock Ordering을 적용했으며, 상품 상세 조회에는 **Redis Cache-Aside** 전략을 적용했습니다. 또한 약 10만 건의 테스트 데이터를 기반으로 **MySQL EXPLAIN**을 분석하여 실제 조회 패턴에 맞는 인덱스 전략을 검토했습니다.

---

## Highlights

### 🔒 재고 동시성 제어

초기 재고가 10개인 상품에 **200개의 동시 주문**을 발생시켜 재고는 0이지만 99건 이상의 주문이 성공하는 Lost Update 문제를 재현했습니다.

JUnit과 `CountDownLatch`를 활용해 경쟁 조건을 테스트하고, Inventory 조회에 **JPA Pessimistic Lock**을 적용했습니다.

그 결과 동일 조건에서 **10건의 주문만 성공하고 190건은 재고 부족으로 실패**하도록 만들어 재고 정합성을 보장했습니다.

다중 상품 주문에서는 상품 ID를 정렬한 뒤 항상 동일한 순서로 Row Lock을 획득하도록 구성해 Deadlock 위험도 줄였습니다.

---

### ⚡ Redis 기반 상품 조회 최적화

Read-heavy 특성을 가진 상품 상세 조회에 **Redis Cache-Aside** 전략을 적용했습니다.

MySQL을 Single Source of Truth로 유지하기 위해 상품 수정 시 캐시 값을 갱신하는 대신 Evict하고, DB 트랜잭션이 정상적으로 Commit된 이후 캐시가 제거되도록 캐시 무효화 시점을 동기화했습니다.

k6 `constant-arrival-rate`를 이용해 동일한 **200 RPS** 조건에서 성능을 비교했습니다.

| Metric             | No Cache | Warm Cache |       Result |
| ------------------ | -------: | ---------: | -----------: |
| Avg Latency        |   5.19ms |     1.71ms | **약 67% 감소** |
| p99 Latency        |  11.33ms |     6.82ms | **약 40% 감소** |
| Dropped Iterations |       27 |          0 |       **0건** |

Warm Cache 조건을 3회 반복 측정했을 때도 평균 응답시간 `1.63~1.76ms`, p99 `6.62~6.95ms`, Dropped Iterations `0건`으로 유사한 결과를 확인했습니다.

---

### 📊 MySQL 조회 성능 분석

소량 데이터에서는 드러나지 않는 조회 병목을 확인하기 위해 **약 10만 건 규모의 테스트 데이터**를 구성했습니다.

MySQL `EXPLAIN`의 `type`, `key`, `rows`, `Extra`를 기준으로 Full Scan과 Filesort 여부를 분석하고, 실제 API의 `WHERE` 조건과 `ORDER BY` 패턴을 기준으로 복합 인덱스 전략을 검토했습니다.

단순히 인덱스를 추가하는 것이 아니라,

**조회 패턴 분석 → EXPLAIN 확인 → 인덱스 설계 → 동일 쿼리 EXPLAIN 재검증**

순서로 데이터 접근 경로를 분석했습니다.

---

## Tech Stack

**Backend**

`Java` · `Spring Boot` · `Spring Security` · `Spring Data JPA`

**Database & Cache**

`MySQL` · `Redis`

**Test & Performance**

`JUnit` · `CountDownLatch` · `k6`

**Infrastructure & Tools**

`Docker` · `GitHub` · `Swagger`

---

## Architecture

프로젝트는 비즈니스 도메인을 기준으로 패키지를 분리하고, 각 도메인 내부를 Application · Domain · Infrastructure · Presentation 계층으로 구성했습니다.

```text
com.backend.july
├── auth
│   ├── application
│   ├── infrastructure
│   └── presentation
│
├── member
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── presentation
│
├── product
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── presentation
│
├── inventory
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── presentation
│
├── cart
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── presentation
│
├── order
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── presentation
│
├── payment
│   ├── domain
│   ├── infrastructure
│   └── presentation
│
└── common
    ├── audit
    ├── config
    ├── exception
    └── response
```

### Domain Flow

```text
Member
  │
  ├── Product 조회
  │
  ├── Cart
  │
  └── Order
        │
        ├── OrderItem
        ├── Inventory
        └── Payment
```

주문 생성 과정에서는 재고 검증과 차감이 하나의 트랜잭션 안에서 수행되며, 동일 Inventory에 대한 동시 변경은 비관적 락을 통해 직렬화합니다.

---

## Core Features

| Domain    | Features                    |
| --------- | --------------------------- |
| Auth      | 회원가입, 로그인, 로그아웃, JWT 재발급    |
| Member    | 회원 정보 조회·수정, 회원 탈퇴          |
| Product   | 상품 등록·조회·수정·삭제, 상품 상태 관리    |
| Inventory | 재고 차감·복구 및 동시성 제어           |
| Cart      | 장바구니 조회, 상품 추가·수량 변경·삭제·초기화 |
| Order     | 주문 생성·조회·목록 조회·취소           |
| Payment   | 결제 도메인 및 결제 상태 관리           |

---

## Technical Challenges

이 프로젝트에서 중점적으로 다룬 문제는 다음 세 가지입니다.

### 1. 동시 주문에서 재고 정합성을 어떻게 보장할 것인가?

`@Transactional`만으로 서로 다른 트랜잭션이 동일한 Inventory Row를 동시에 읽고 변경하는 문제를 해결할 수 없었습니다.

동시성 테스트를 통해 Lost Update를 재현한 뒤 비관적 락을 적용했으며, 주문 생성뿐 아니라 **주문 취소에 따른 재고 복구와 관리자 재고 변경 등 동일 Inventory를 수정하는 모든 진입점이 같은 락 정책을 사용하도록 설계**했습니다.

동시성 테스트의 성공 기준 역시 특정 스레드의 성공 건수가 아니라 다음 비즈니스 불변식으로 정의했습니다.

```text
초기 재고 = 성공한 주문 수량 + 최종 재고
```

---

### 2. Redis를 사용하면서 DB와 캐시의 정합성을 어떻게 유지할 것인가?

캐시는 조회 성능을 위한 파생 데이터로 한정하고 **MySQL을 Single Source of Truth**로 유지했습니다.

```text
Cache Hit
Request → Redis → Response

Cache Miss
Request → Redis Miss → MySQL → Redis 저장 → Response

Product Update
DB Update → Transaction Commit → Cache Evict
```

캐시 값을 직접 수정하지 않고 삭제하는 전략을 사용해 DB와 Redis의 이중 갱신 문제를 피했으며, 트랜잭션이 정상적으로 Commit된 이후 캐시 작업이 수행되도록 구성했습니다.

---

### 3. 데이터가 증가했을 때 조회 병목을 어떻게 찾을 것인가?

약 10만 건의 데이터를 구성한 뒤 응답시간만 측정하지 않고 MySQL이 실제 데이터를 읽는 경로를 분석했습니다.

```text
EXPLAIN
   ↓
type / key / rows / Extra 분석
   ↓
Full Scan · Filesort 확인
   ↓
WHERE + ORDER BY 패턴 분석
   ↓
Composite Index 설계
   ↓
EXPLAIN 재검증
```

이를 통해 인덱스의 존재 여부보다 **Optimizer가 실제 쿼리에서 어떤 인덱스를 선택하고 얼마만큼의 데이터를 탐색하는지**를 최적화 기준으로 삼았습니다.

---

## Testing

비즈니스 로직 검증을 위한 단위·통합 테스트와 실제 API 흐름을 검증하기 위한 HTTP 시나리오를 구성했습니다.

```text
test
├── http
│   ├── auth.http
│   ├── full-business-scenario.http
│   └── product
│       ├── create-product.http
│       ├── get-products.http
│       ├── order-list-cursor-scenario.http
│       └── product-update.http
│
└── java
    └── com.backend.july
        ├── fixture
        ├── order
        │   └── application
        │       └── CreateOrderConcurrencyIntegrationTest.java
        └── product
            └── application
                └── CreateProductServiceTest.java
```

특히 주문 동시성 테스트에서는 `ExecutorService`와 `CountDownLatch`를 사용하여 다수의 요청이 동일한 Inventory Row에 경쟁하도록 구성했습니다.

---

## API Documentation

애플리케이션 실행 후 Swagger UI에서 API 명세를 확인하고 직접 요청할 수 있습니다.

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

---

## What I Learned

이 프로젝트를 통해 기능이 정상적으로 동작하는 것과 **동시 요청과 데이터 증가 상황에서도 시스템이 올바르게 동작하는 것은 다른 문제**라는 점을 확인했습니다.

재고 동시성 문제에서는 단순히 비관적 락을 적용하는 데 그치지 않고 Lock Ordering, Lock Timeout, Connection Pool과 같은 운영 비용까지 고려했습니다.

Redis 캐시에서는 응답시간 자체보다 DB와 캐시의 데이터 기준점과 트랜잭션 경계를 중요하게 다뤘으며, k6를 통해 동일한 입력 부하에서 개선 효과를 검증했습니다.

데이터베이스 최적화에서는 느린 쿼리를 감으로 수정하지 않고 대량 데이터를 구성한 뒤 EXPLAIN을 통해 접근 경로를 분석하고 다시 검증하는 과정을 경험했습니다.

결과적으로 이 프로젝트는 단순한 이커머스 API 구현보다 **데이터 정합성, 성능, 트랜잭션 경계를 실제 테스트를 통해 검증하고 개선하는 과정**에 초점을 맞췄습니다.
