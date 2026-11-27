-- =====================================================================
-- ms-carrito · V2: cupones iniciales
-- GAMER10 y DUOC2026: 10 %, sin mínimo de compra ni vencimiento automático.
-- Administración puede desactivarlos (active = 0). UUID fijos para que
-- todos los entornos tengan los mismos identificadores.
-- =====================================================================

INSERT INTO coupons (coupon_id, code, discount_percent, active)
VALUES ('7a0f5f0e-6c1b-4d55-9f6e-0a1c2d3e4f01', 'GAMER10', 10, 1);

INSERT INTO coupons (coupon_id, code, discount_percent, active)
VALUES ('7a0f5f0e-6c1b-4d55-9f6e-0a1c2d3e4f02', 'DUOC2026', 10, 1);
