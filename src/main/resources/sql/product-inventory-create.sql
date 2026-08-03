-- MySQL 8.0.x
-- 상품 조회, 트랜잭션 격리 수준, 인덱스 및 캐시 성능 테스트 데이터
--
-- v1_products    : 1,000,000건
-- v1_inventories : 1,000,000건

SET @dummy_product_count = 1000000;

-- 실행 환경 확인
SELECT VERSION() AS mysql_version;
SELECT @@transaction_isolation AS session_isolation_level;
SELECT @@global.transaction_isolation AS global_isolation_level;
SELECT @@innodb_autoinc_lock_mode AS innodb_autoinc_lock_mode;

DROP TEMPORARY TABLE IF EXISTS temp_sequence;

CREATE TEMPORARY TABLE temp_sequence (
                                         seq INT NOT NULL,
                                         PRIMARY KEY (seq)
) ENGINE = InnoDB;

-- 0 ~ 999,999 생성
INSERT INTO temp_sequence (seq)
SELECT
    ones.n
        + tens.n * 10
        + hundreds.n * 100
        + thousands.n * 1000
        + ten_thousands.n * 10000
        + hundred_thousands.n * 100000 AS seq
FROM
    (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2
        UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
        UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
        UNION ALL SELECT 9
    ) AS ones
        CROSS JOIN
    (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2
        UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
        UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
        UNION ALL SELECT 9
    ) AS tens
        CROSS JOIN
    (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2
        UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
        UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
        UNION ALL SELECT 9
    ) AS hundreds
        CROSS JOIN
    (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2
        UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
        UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
        UNION ALL SELECT 9
    ) AS thousands
        CROSS JOIN
    (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2
        UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
        UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
        UNION ALL SELECT 9
    ) AS ten_thousands
        CROSS JOIN
    (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2
        UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
        UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
        UNION ALL SELECT 9
    ) AS hundred_thousands
WHERE
    ones.n
        + tens.n * 10
        + hundreds.n * 100
        + thousands.n * 1000
        + ten_thousands.n * 10000
        + hundred_thousands.n * 100000
        < @dummy_product_count
ORDER BY seq;

-- 시퀀스 생성 결과 확인
SELECT
    COUNT(*) AS sequence_count,
    MIN(seq) AS min_sequence,
    MAX(seq) AS max_sequence
FROM temp_sequence;

START TRANSACTION;

-- 상품 생성
INSERT INTO v1_products (
    price,
    created_at,
    created_by,
    updated_at,
    updated_by,
    name,
    status
)
SELECT
    CAST(
            CASE
                -- 50%: 1,000 ~ 50,000
                WHEN seq % 100 < 50
                    THEN ((seq % 50) + 1) * 1000

                -- 35%: 255,000 ~ 425,000
                WHEN seq % 100 < 85
                    THEN ((seq % 100) + 1) * 5000

                -- 12%: 860,000 ~ 970,000
                WHEN seq % 100 < 97
                    THEN ((seq % 100) + 1) * 10000

                -- 3%: 9,800,000 ~ 10,000,000
                ELSE
                    ((seq % 100) + 1) * 100000
                END
        AS DECIMAL(19, 0)
    ) AS price,

    TIMESTAMPADD(
            SECOND,
            seq % 86400,
            TIMESTAMPADD(DAY, -(seq % 365), NOW(6))
    ) AS created_at,

    1 AS created_by,

    TIMESTAMPADD(
            SECOND,
            seq % 86400,
            TIMESTAMPADD(DAY, -(seq % 365), NOW(6))
    ) AS updated_at,

    1 AS updated_by,

    CONCAT(
            '성능 테스트 상품 ',
            LPAD(seq + 1, 7, '0')
    ) AS name,

    CASE
        WHEN seq % 100 < 80 THEN 'ON_SALE'
        WHEN seq % 100 < 95 THEN 'HIDDEN'
        ELSE 'DELETED'
        END AS status
FROM temp_sequence
ORDER BY seq;

-- 다중 행 INSERT에서는 첫 번째로 생성된 AUTO_INCREMENT 값을 반환
SET @dummy_product_first_id = LAST_INSERT_ID();

SET @dummy_product_last_id =
        @dummy_product_first_id + @dummy_product_count - 1;

-- 상품별 재고 생성
INSERT INTO v1_inventories (
    quantity,
    product_id,
    version
)
SELECT
    CASE
        -- 10%: 품절
        WHEN seq % 100 < 10 THEN 0

        -- 20%: 소량 재고 1 ~ 10
        WHEN seq % 100 < 30 THEN (seq % 10) + 1

        -- 50%: 일반 재고 11 ~ 100
        WHEN seq % 100 < 80 THEN (seq % 90) + 11

        -- 20%: 대량 재고 101 ~ 1,000
        ELSE (seq % 900) + 101
        END AS quantity,

    @dummy_product_first_id + seq AS product_id,

    0 AS version
FROM temp_sequence
ORDER BY seq;

COMMIT;

-- 옵티마이저 통계 갱신
ANALYZE TABLE v1_products;
ANALYZE TABLE v1_inventories;

-- =========================================================
-- 삽입 결과 검증
-- =========================================================

SELECT
    @dummy_product_first_id AS first_product_id,
    @dummy_product_last_id AS last_product_id,
    COUNT(*) AS inserted_product_count,
    MIN(created_at) AS min_created_at,
    MAX(created_at) AS max_created_at
FROM v1_products
WHERE id BETWEEN @dummy_product_first_id
          AND @dummy_product_last_id;

SELECT
    status,
    COUNT(*) AS product_count,
    ROUND(
            COUNT(*) * 100.0 / @dummy_product_count,
            2
    ) AS ratio
FROM v1_products
WHERE id BETWEEN @dummy_product_first_id
          AND @dummy_product_last_id
GROUP BY status
ORDER BY status;

SELECT
    COUNT(*) AS inserted_inventory_count,
    SUM(quantity = 0) AS sold_out_count,
    MIN(quantity) AS min_quantity,
    MAX(quantity) AS max_quantity,
    ROUND(AVG(quantity), 2) AS avg_quantity
FROM v1_inventories
WHERE product_id BETWEEN @dummy_product_first_id
          AND @dummy_product_last_id;

-- 상품과 재고가 정확히 1:1로 생성됐는지 확인
SELECT
    COUNT(*) AS unmatched_product_count
FROM v1_products p
         LEFT JOIN v1_inventories i
                   ON i.product_id = p.id
WHERE p.id BETWEEN @dummy_product_first_id
    AND @dummy_product_last_id
  AND i.product_id IS NULL;

-- 일부 데이터 확인
SELECT
    p.id,
    p.name,
    p.price,
    p.status,
    p.created_at,
    i.quantity
FROM v1_products p
         JOIN v1_inventories i
              ON i.product_id = p.id
WHERE p.id BETWEEN @dummy_product_first_id
          AND @dummy_product_last_id
ORDER BY p.id
LIMIT 100;

DROP TEMPORARY TABLE IF EXISTS temp_sequence;