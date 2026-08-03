EXPLAIN
SELECT id,
       name,
       price,
       status
FROM v1_products
WHERE status = 'DELETED';
