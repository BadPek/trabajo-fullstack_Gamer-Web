-- =====================================================================
-- ms-pedido · esquema propietario MS_PEDIDO · Oracle Database 21c
-- V1: pedidos sin pago, líneas (snapshot), cupón aplicado e idempotencia
--
-- Referencias: BACKEND_CONTRACT.md v0.6 (Pedidos, Pedido y carrito,
-- Retención de pedidos, Redondeo) y PLAN_ARQUITECTURA §4.
--
-- * La aplica Flyway con el usuario MS_PEDIDO. No ejecutarla a mano en ese
--   esquema: Flyway fallaría al encontrarlo con objetos.
-- * IDs: UUID textual en minúsculas, CHAR(36), generado por la aplicación.
-- * user_id (ms-auth) y product_id (ms-catalogo) son IDs externos: sin FK
--   entre esquemas.
-- * NO se guardan subtotal, descuento, IVA ni total: se calculan desde las
--   líneas, el cupón aplicado y pricing_policy_version (decisión aprobada).
-- * Los snapshots no contienen nombre, correo, RUT ni dirección del cliente.
-- * Los pedidos son inmutables: el servicio solo inserta. No hay updated_at
--   ni borrado (las FK sin CASCADE impiden borrar un pedido con líneas).
-- * Fechas en UTC.
-- =====================================================================

-- Pedidos. uq_orders_id_user es redundante a propósito: permite que
-- order_idempotency apunte al par (pedido, cliente) y la base garantice que
-- la clave de idempotencia pertenece al dueño del pedido.
CREATE TABLE orders (
    order_id               CHAR(36)          NOT NULL,
    user_id                CHAR(36)          NOT NULL,
    status                 VARCHAR2(30 CHAR) DEFAULT 'REGISTRADO_SIN_PAGO' NOT NULL,
    pricing_policy_version NUMBER(5)         DEFAULT 1 NOT NULL,
    created_at             TIMESTAMP(6)      DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (order_id),
    CONSTRAINT uq_orders_id_user UNIQUE (order_id, user_id),
    CONSTRAINT ck_orders_id CHECK (
        REGEXP_LIKE(order_id, '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_orders_user CHECK (
        REGEXP_LIKE(user_id, '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_orders_status CHECK (status IN ('REGISTRADO_SIN_PAGO')),
    CONSTRAINT ck_orders_politica CHECK (pricing_policy_version >= 1)
);

-- GET /orders/me: pedidos propios por fecha DESC.
CREATE INDEX ix_orders_user ON orders (user_id, created_at DESC, order_id);
-- GET /admin/orders: createdAt DESC con desempate por ID; filtros estado y fechas.
CREATE INDEX ix_orders_admin ON orders (created_at DESC, order_id);
CREATE INDEX ix_orders_admin_status ON orders (status, created_at DESC, order_id);

-- Líneas: snapshot del título y del precio unitario al confirmar. El
-- subtotal de línea es unit_price_clp * quantity (se calcula, no se guarda).
-- product_id puede quedar NULL si el producto externo deja de existir.
CREATE TABLE order_items (
    order_item_id  CHAR(36)           NOT NULL,
    order_id       CHAR(36)           NOT NULL,
    product_id     CHAR(36),
    title_snapshot VARCHAR2(150 CHAR) NOT NULL,
    unit_price_clp NUMBER(10)         NOT NULL,
    quantity       NUMBER(3)          NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (order_item_id),
    CONSTRAINT uq_order_items_producto UNIQUE (order_id, product_id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT ck_order_items_product CHECK (
        product_id IS NULL
        OR REGEXP_LIKE(product_id, '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_order_items_titulo CHECK (TRIM(title_snapshot) IS NOT NULL),
    CONSTRAINT ck_order_items_precio CHECK (unit_price_clp > 0),
    CONSTRAINT ck_order_items_cantidad CHECK (quantity BETWEEN 1 AND 99)
);

-- Cupón aplicado (0..1 por pedido): congela código y porcentaje, sin
-- depender de los cupones actuales de ms-carrito.
CREATE TABLE order_coupon_snapshots (
    order_id                  CHAR(36)          NOT NULL,
    coupon_code_snapshot      VARCHAR2(30 CHAR) NOT NULL,
    discount_percent_snapshot NUMBER(3)         NOT NULL,
    CONSTRAINT pk_order_coupon_snapshots PRIMARY KEY (order_id),
    CONSTRAINT fk_ocs_order FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT ck_ocs_codigo CHECK (REGEXP_LIKE(coupon_code_snapshot, '^[A-Z0-9]{3,30}$', 'c')),
    CONSTRAINT ck_ocs_descuento CHECK (discount_percent_snapshot BETWEEN 1 AND 100)
);

-- Idempotencia de POST /orders: Idempotency-Key UUID v4, única por cliente y
-- conservada mientras exista el pedido. Un pedido tiene exactamente una clave.
-- cart_version y cart_fingerprint permiten distinguir un reintento legítimo
-- (misma versión/cotización: devolver el pedido original) de una clave
-- reutilizada (409 IDEMPOTENCY_KEY_REUSED).
CREATE TABLE order_idempotency (
    user_id          CHAR(36)     NOT NULL,
    idempotency_key  CHAR(36)     NOT NULL,
    cart_version     NUMBER(10)   NOT NULL,
    cart_fingerprint CHAR(64)     NOT NULL,
    order_id         CHAR(36)     NOT NULL,
    created_at       TIMESTAMP(6) DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    CONSTRAINT pk_order_idempotency PRIMARY KEY (user_id, idempotency_key),
    CONSTRAINT uq_order_idempotency_order UNIQUE (order_id),
    CONSTRAINT fk_oi_order_user FOREIGN KEY (order_id, user_id) REFERENCES orders (order_id, user_id),
    CONSTRAINT ck_oi_idempotency_key CHECK (
        REGEXP_LIKE(idempotency_key, '^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_oi_version CHECK (cart_version >= 0)
);
