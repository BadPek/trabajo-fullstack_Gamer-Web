CREATE TABLE cart_quote_items (
    quote_id       CHAR(36) NOT NULL,
    product_id     CHAR(36) NOT NULL,
    quantity       NUMBER(3) NOT NULL,
    title_snapshot VARCHAR2(150 CHAR) NOT NULL,
    unit_price_clp NUMBER(10) NOT NULL,
    CONSTRAINT pk_cart_quote_items PRIMARY KEY (quote_id, product_id),
    CONSTRAINT fk_cqi_quote FOREIGN KEY (quote_id)
        REFERENCES cart_price_quotes (quote_id) ON DELETE CASCADE,
    CONSTRAINT ck_cqi_product CHECK (
        REGEXP_LIKE(product_id, '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_cqi_quantity CHECK (quantity BETWEEN 1 AND 99),
    CONSTRAINT ck_cqi_title CHECK (TRIM(title_snapshot) IS NOT NULL),
    CONSTRAINT ck_cqi_price CHECK (unit_price_clp > 0)
);

CREATE TABLE cart_quote_coupon_snapshots (
    quote_id                  CHAR(36) NOT NULL,
    coupon_code_snapshot      VARCHAR2(30 CHAR) NOT NULL,
    discount_percent_snapshot NUMBER(3) NOT NULL,
    CONSTRAINT pk_cart_quote_coupon_snapshots PRIMARY KEY (quote_id),
    CONSTRAINT fk_cqcs_quote FOREIGN KEY (quote_id)
        REFERENCES cart_price_quotes (quote_id) ON DELETE CASCADE,
    CONSTRAINT ck_cqcs_code CHECK (
        REGEXP_LIKE(coupon_code_snapshot, '^[A-Z0-9]{3,30}$', 'c')
    ),
    CONSTRAINT ck_cqcs_discount CHECK (
        discount_percent_snapshot BETWEEN 1 AND 100
    )
);
