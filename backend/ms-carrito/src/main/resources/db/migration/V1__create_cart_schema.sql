-- =====================================================================
-- ms-carrito · esquema propietario MS_CARRITO · Oracle Database 21c
-- V1: cupones, carritos (invitado y cliente), líneas, cotizaciones y reservas
--
-- Referencias: BACKEND_CONTRACT.md v0.6 (Carritos, Carrito invitado, Fusión,
-- Cupones, Pedido y carrito) y PLAN_ARQUITECTURA §4.
--
-- * La aplica Flyway con el usuario MS_CARRITO. No ejecutarla a mano en ese
--   esquema: Flyway fallaría al encontrarlo con objetos.
-- * IDs: UUID textual en minúsculas, CHAR(36), generado por la aplicación.
-- * user_id (ms-auth) y product_id (ms-catalogo) son IDs externos: sin FK
--   entre esquemas.
-- * Cookie de invitado, token de cotización y fingerprint: solo hash SHA-256
--   en hexadecimal, CHAR(64). Nunca el valor en claro.
-- * No se guardan precios ni totales: se calculan consultando el catálogo.
-- * Fechas en UTC. updated_at lo mantienen los triggers del final.
-- * Los cupones iniciales van en V2.
-- =====================================================================

-- Cupones. Código normalizado (mayúsculas, sin espacios); descuento fijo.
-- Sin compra mínima ni vencimiento automático en v1.
CREATE TABLE coupons (
    coupon_id        CHAR(36)          NOT NULL,
    code             VARCHAR2(30 CHAR) NOT NULL,
    discount_percent NUMBER(3)         NOT NULL,
    active           NUMBER(1)         DEFAULT 1 NOT NULL,
    created_at       TIMESTAMP(6)      DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    updated_at       TIMESTAMP(6)      DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    CONSTRAINT pk_coupons PRIMARY KEY (coupon_id),
    CONSTRAINT uq_coupons_code UNIQUE (code),
    CONSTRAINT ck_coupons_code CHECK (REGEXP_LIKE(code, '^[A-Z0-9]{3,30}$', 'c')),
    CONSTRAINT ck_coupons_descuento CHECK (discount_percent BETWEEN 1 AND 100),
    CONSTRAINT ck_coupons_active CHECK (active IN (0, 1))
);

-- Carritos. Exactamente un propietario: cuenta (user_id) o invitado (hash
-- de la cookie gz_guest_cart). Solo un carrito ACTIVE por identidad (índices
-- de abajo); MERGED/EXPIRED conservan el historial. Al consumirse en un pedido
-- el carrito sigue ACTIVE, se vacía y sube su versión.
CREATE TABLE carts (
    cart_id          CHAR(36)          NOT NULL,
    user_id          CHAR(36),
    guest_token_hash CHAR(64),
    coupon_id        CHAR(36),
    status           VARCHAR2(10 CHAR) DEFAULT 'ACTIVE' NOT NULL,
    version          NUMBER(10)        DEFAULT 0 NOT NULL,
    last_activity_at TIMESTAMP(6)      DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    created_at       TIMESTAMP(6)      DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    updated_at       TIMESTAMP(6)      DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    CONSTRAINT pk_carts PRIMARY KEY (cart_id),
    CONSTRAINT fk_carts_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (coupon_id),
    CONSTRAINT ck_carts_id CHECK (
        REGEXP_LIKE(cart_id, '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_carts_user CHECK (
        user_id IS NULL
        OR REGEXP_LIKE(user_id, '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_carts_status CHECK (status IN ('ACTIVE', 'MERGED', 'EXPIRED')),
    CONSTRAINT ck_carts_version CHECK (version >= 0),
    CONSTRAINT ck_carts_un_propietario CHECK (
        (user_id IS NOT NULL AND guest_token_hash IS NULL)
        OR (user_id IS NULL AND guest_token_hash IS NOT NULL)
    )
);

-- Un solo carrito ACTIVE por cuenta y por cookie de invitado. Las filas no
-- activas dan NULL y Oracle no las indexa, así que conservan su historial.
CREATE UNIQUE INDEX uq_carts_user_active
    ON carts (CASE WHEN status = 'ACTIVE' THEN user_id END);
CREATE UNIQUE INDEX uq_carts_guest_active
    ON carts (CASE WHEN status = 'ACTIVE' THEN guest_token_hash END);

-- Limpieza de carritos invitados tras 30 días sin actividad.
CREATE INDEX ix_carts_actividad ON carts (last_activity_at);
CREATE INDEX ix_carts_coupon ON carts (coupon_id);

-- Líneas: una por producto, cantidad 1 a 99. Los máximos de 50 líneas y 500
-- unidades por carrito dependen de varias filas: los aplica el servicio
-- dentro de la transacción (bloqueando la fila del carrito y subiendo version).
CREATE TABLE cart_items (
    cart_id    CHAR(36)     NOT NULL,
    product_id CHAR(36)     NOT NULL,
    quantity   NUMBER(3)    NOT NULL,
    created_at TIMESTAMP(6) DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    updated_at TIMESTAMP(6) DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    CONSTRAINT pk_cart_items PRIMARY KEY (cart_id, product_id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (cart_id) ON DELETE CASCADE,
    CONSTRAINT ck_cart_items_product CHECK (
        REGEXP_LIKE(product_id, '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_cart_items_cantidad CHECK (quantity BETWEEN 1 AND 99)
);

-- Cotizaciones de precio: token HMAC-SHA-256 firmado por ms-carrito, TTL de
-- 10 minutos, ligado a carrito/versión/fingerprint. Solo se guarda su hash.
CREATE TABLE cart_price_quotes (
    quote_id          CHAR(36)     NOT NULL,
    cart_id           CHAR(36)     NOT NULL,
    cart_version      NUMBER(10)   NOT NULL,
    price_fingerprint CHAR(64)     NOT NULL,
    token_hash        CHAR(64)     NOT NULL,
    created_at        TIMESTAMP(6) DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    expires_at        TIMESTAMP(6) NOT NULL,
    consumed_at       TIMESTAMP(6),
    CONSTRAINT pk_cart_price_quotes PRIMARY KEY (quote_id),
    CONSTRAINT uq_cart_price_quotes_token UNIQUE (token_hash),
    CONSTRAINT fk_cpq_cart FOREIGN KEY (cart_id) REFERENCES carts (cart_id) ON DELETE CASCADE,
    CONSTRAINT ck_cpq_expiracion CHECK (expires_at > created_at)
);

CREATE INDEX ix_cpq_cart_version ON cart_price_quotes (cart_id, cart_version);
CREATE INDEX ix_cpq_expira ON cart_price_quotes (expires_at);

-- Reservas para crear un pedido: 5 minutos, bloquean las mutaciones del
-- carrito. idempotency_key es la clave UUID v4 del POST /orders.
-- Estados provisionales: Sara define expiración y reintentos.
CREATE TABLE cart_reservations (
    reservation_id  CHAR(36)          NOT NULL,
    cart_id         CHAR(36)          NOT NULL,
    cart_version    NUMBER(10)        NOT NULL,
    idempotency_key CHAR(36)          NOT NULL,
    status          VARCHAR2(10 CHAR) DEFAULT 'ACTIVE' NOT NULL,
    created_at      TIMESTAMP(6)      DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    expires_at      TIMESTAMP(6)      NOT NULL,
    completed_at    TIMESTAMP(6),
    CONSTRAINT pk_cart_reservations PRIMARY KEY (reservation_id),
    CONSTRAINT uq_cr_cart_key UNIQUE (cart_id, idempotency_key),
    CONSTRAINT fk_cr_cart FOREIGN KEY (cart_id) REFERENCES carts (cart_id) ON DELETE CASCADE,
    CONSTRAINT ck_cr_idempotency_key CHECK (
        REGEXP_LIKE(idempotency_key, '^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_cr_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'RELEASED', 'EXPIRED')),
    CONSTRAINT ck_cr_expiracion CHECK (expires_at > created_at),
    CONSTRAINT ck_cr_completada CHECK (
        (status = 'COMPLETED' AND completed_at IS NOT NULL)
        OR (status <> 'COMPLETED' AND completed_at IS NULL)
    )
);

-- A lo más UNA reserva ACTIVE por carrito, garantizado por la base aunque
-- lleguen dos solicitudes a la vez.
CREATE UNIQUE INDEX uq_cart_reservations_activa
    ON cart_reservations (CASE WHEN status = 'ACTIVE' THEN cart_id END);
CREATE INDEX ix_cr_expira ON cart_reservations (status, expires_at);

-- Mantienen updated_at en cada actualización.
CREATE OR REPLACE TRIGGER trg_coupons_updated_at
BEFORE UPDATE ON coupons
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYS_EXTRACT_UTC(SYSTIMESTAMP);
END;
/

CREATE OR REPLACE TRIGGER trg_carts_updated_at
BEFORE UPDATE ON carts
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYS_EXTRACT_UTC(SYSTIMESTAMP);
END;
/

CREATE OR REPLACE TRIGGER trg_cart_items_updated_at
BEFORE UPDATE ON cart_items
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYS_EXTRACT_UTC(SYSTIMESTAMP);
END;
/
