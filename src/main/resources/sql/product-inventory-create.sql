-- MySQL 8.x
-- 상품 조회/캐시 성능 테스트용 더미 데이터
-- v1_products 100,000건 + v1_inventories 100,000건

SET @dummy_product_count = 100000;

-- 기본값은 보통 1,000이므로 100,000회 재귀 생성을 허용합니다.
SET SESSION cte_max_recursion_depth = 100001;

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS temp_sequence;

CREATE TEMPORARY TABLE temp_sequence (
                                         seq INT NOT NULL PRIMARY KEY
);

-- 0 ~ 99,999 생성
INSERT INTO temp_sequence (seq)
WITH RECURSIVE sequence_generator AS (
    SELECT 0 AS seq

    UNION ALL

    SELECT seq + 1
    FROM sequence_generator
    WHERE seq + 1 < @dummy_product_count
)
SELECT seq
FROM sequence_generator;

-- 상품 100,000건 생성
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
    CAST(((seq % 1000) + 1) * 1000 AS DECIMAL(19, 0)) AS price,
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
    CONCAT('성능 테스트 상품 ', LPAD(seq + 1, 6, '0')) AS name,
    CASE
        WHEN seq % 100 < 90 THEN 'ON_SALE'
        WHEN seq % 100 < 97 THEN 'HIDDEN'
        ELSE 'DELETED'
        END AS status
FROM temp_sequence
ORDER BY seq;

SET @dummy_product_first_id = LAST_INSERT_ID();
SET @dummy_product_last_id =
        @dummy_product_first_id + @dummy_product_count - 1;

-- 각 상품에 재고 1건 생성
INSERT INTO v1_inventories (
    quantity,
    product_id,
    version
)
SELECT
    seq % 1000 AS quantity,
    @dummy_product_first_id + seq AS product_id,
    0 AS version
FROM temp_sequence
ORDER BY seq;

COMMIT;

-- 결과 검증
SELECT
    @dummy_product_first_id AS first_product_id,
    @dummy_product_last_id AS last_product_id,
    COUNT(*) AS inserted_product_count
FROM v1_products
WHERE id BETWEEN @dummy_product_first_id AND @dummy_product_last_id;

SELECT
    status,
    COUNT(*) AS product_count
FROM v1_products
WHERE id BETWEEN @dummy_product_first_id AND @dummy_product_last_id
GROUP BY status
ORDER BY status;

SELECT
    COUNT(*) AS inserted_inventory_count,
    MIN(quantity) AS min_quantity,
    MAX(quantity) AS max_quantity,
    AVG(quantity) AS avg_quantity
FROM v1_inventories
WHERE product_id BETWEEN @dummy_product_first_id AND @dummy_product_last_id;

DROP TEMPORARY TABLE IF EXISTS temp_sequence;

update v1_products
set status = 'ON_SALE';


select * from v1_products;
