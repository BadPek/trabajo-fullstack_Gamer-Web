ALTER TABLE order_idempotency
    ADD reservation_id CHAR(36);

ALTER TABLE order_idempotency
    ADD CONSTRAINT ck_oi_reservation_id CHECK (
        reservation_id IS NULL
        OR REGEXP_LIKE(
            reservation_id,
            '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$',
            'c'
        )
    );
