-- =====================================================================
-- ms-carrito · pruebas aisladas de V1 y V2 en Oracle 21c
--
-- Ejecutar conectado como GZ_PRUEBA_BACKEND (esquema desechable), DESPUÉS
-- de haber ejecutado ahí V1__create_cart_schema.sql y
-- V2__seed_cart_coupons.sql. Usar F5 y mirar "Script Output".
--
-- No ejecutar en MS_CARRITO. Los datos de prueba se revierten con ROLLBACK,
-- así que el script se puede repetir. Resultado esperado: "0 fallas".
-- =====================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED

DECLARE
    v_ok     PLS_INTEGER := 0;
    v_fallas PLS_INTEGER := 0;
    v_n      NUMBER;
    v_ts1    TIMESTAMP(6);
    v_ts2    TIMESTAMP(6);
    -- Identificadores ficticios (cuentas de ms-auth y productos de ms-catalogo)
    c_u1 CONSTANT VARCHAR2(36) := '11111111-1111-4111-8111-111111111111';
    c_u2 CONSTANT VARCHAR2(36) := '22222222-2222-4222-8222-222222222222';
    c_p1 CONSTANT VARCHAR2(36) := 'b0000000-0000-4000-8000-000000000001';
    c_p2 CONSTANT VARCHAR2(36) := 'b0000000-0000-4000-8000-000000000002';
    c_k1 CONSTANT VARCHAR2(36) := 'a0000000-0000-4000-8000-000000000001';
    c_k2 CONSTANT VARCHAR2(36) := 'a0000000-0000-4000-8000-000000000002';
    c_k4 CONSTANT VARCHAR2(36) := 'a0000000-0000-4000-8000-000000000004';
    c_k5 CONSTANT VARCHAR2(36) := 'a0000000-0000-4000-8000-000000000005';
    c_k9 CONSTANT VARCHAR2(36) := 'a0000000-0000-4000-8000-000000000009';
    c_q1 CONSTANT VARCHAR2(36) := 'c0000000-0000-4000-8000-000000000001';
    c_q2 CONSTANT VARCHAR2(36) := 'c0000000-0000-4000-8000-000000000002';
    c_r1 CONSTANT VARCHAR2(36) := 'd0000000-0000-4000-8000-000000000001';
    c_r2 CONSTANT VARCHAR2(36) := 'd0000000-0000-4000-8000-000000000002';
    c_r3 CONSTANT VARCHAR2(36) := 'd0000000-0000-4000-8000-000000000003';
    -- Claves de idempotencia UUID v4
    c_i1 CONSTANT VARCHAR2(36) := '11111111-1111-4111-8111-000000000001';
    c_i2 CONSTANT VARCHAR2(36) := '11111111-1111-4111-8111-000000000002';
    --
    PROCEDURE marcar(p_nombre VARCHAR2, p_paso BOOLEAN, p_detalle VARCHAR2 DEFAULT NULL) IS
    BEGIN
        IF p_paso THEN
            v_ok := v_ok + 1;
            DBMS_OUTPUT.PUT_LINE('OK     ' || p_nombre);
        ELSE
            v_fallas := v_fallas + 1;
            DBMS_OUTPUT.PUT_LINE('FALLA  ' || p_nombre
                || CASE WHEN p_detalle IS NOT NULL THEN '  ->  ' || p_detalle END);
        END IF;
    END;
    --
    PROCEDURE debe_aceptar(p_nombre VARCHAR2, p_sql VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE p_sql;
        marcar(p_nombre, TRUE);
    EXCEPTION
        WHEN OTHERS THEN
            marcar(p_nombre, FALSE, SQLERRM);
    END;
    --
    PROCEDURE debe_rechazar(p_nombre VARCHAR2, p_sql VARCHAR2, p_codigo PLS_INTEGER) IS
    BEGIN
        EXECUTE IMMEDIATE p_sql;
        marcar(p_nombre, FALSE,
               'se acepto y debia rechazarse con ORA' || TO_CHAR(p_codigo, 'FM00000'));
    EXCEPTION
        WHEN OTHERS THEN
            marcar(p_nombre, SQLCODE = p_codigo, 'error distinto al esperado: ' || SQLERRM);
    END;
    --
    FUNCTION lit(p_valor VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN '''' || REPLACE(p_valor, '''', '''''') || '''';
    END;
    --
    FUNCTION ins_item(p_cart VARCHAR2, p_product VARCHAR2, p_qty VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN 'INSERT INTO cart_items (cart_id, product_id, quantity) VALUES ('
            || p_cart || ', ' || p_product || ', ' || p_qty || ')';
    END;
    --
    FUNCTION ins_reserva(p_id VARCHAR2, p_cart VARCHAR2, p_key VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN 'INSERT INTO cart_reservations (reservation_id, cart_id, cart_version, idempotency_key, '
            || 'expires_at) VALUES (' || p_id || ', ' || p_cart || ', 0, ' || p_key
            || ', SYS_EXTRACT_UTC(SYSTIMESTAMP) + INTERVAL ''5'' MINUTE)';
    END;
BEGIN
    DBMS_OUTPUT.PUT_LINE('== Pruebas V1/V2 ms-carrito en el esquema ' || USER || ' ==');
    --
    -- 1. Cupones iniciales (V2)
    SELECT COUNT(*) INTO v_n FROM coupons
    WHERE code IN ('GAMER10', 'DUOC2026') AND discount_percent = 10 AND active = 1;
    marcar('semilla: GAMER10 y DUOC2026 activos al 10%', v_n = 2, 'encontrados: ' || v_n);
    --
    -- 2. Carritos y propietario
    debe_aceptar('acepta carrito de cuenta',
        'INSERT INTO carts (cart_id, user_id) VALUES (' || lit(c_k1) || ', ' || lit(c_u1) || ')');
    debe_aceptar('acepta carrito invitado (hash de cookie)',
        'INSERT INTO carts (cart_id, guest_token_hash) VALUES (' || lit(c_k2) || q'[, RPAD('a', 64, 'a'))]');
    debe_rechazar('rechaza carrito sin propietario',
        'INSERT INTO carts (cart_id) VALUES (' || lit(c_k9) || ')', -2290);
    debe_rechazar('rechaza carrito con cuenta y cookie a la vez',
        'INSERT INTO carts (cart_id, user_id, guest_token_hash) VALUES (' || lit(c_k9) || ', '
        || lit(c_u2) || q'[, RPAD('z', 64, 'z'))]', -2290);
    debe_rechazar('rechaza segundo carrito ACTIVE de la misma cuenta',
        'INSERT INTO carts (cart_id, user_id) VALUES (' || lit(c_k9) || ', ' || lit(c_u1) || ')', -1);
    debe_rechazar('rechaza segundo carrito ACTIVE con la misma cookie',
        'INSERT INTO carts (cart_id, guest_token_hash) VALUES (' || lit(c_k9) || q'[, RPAD('a', 64, 'a'))]', -1);
    debe_rechazar('rechaza estado de carrito invalido',
        'UPDATE carts SET status = ''VENDIDO'' WHERE cart_id = ' || lit(c_k1), -2290);
    debe_rechazar('rechaza user_id mal formado',
        'INSERT INTO carts (cart_id, user_id) VALUES (' || lit(c_k9) || q'[, 'usuario-1')]', -2290);
    debe_aceptar('marca el carrito invitado como MERGED (fusion)',
        'UPDATE carts SET status = ''MERGED'' WHERE cart_id = ' || lit(c_k2));
    debe_aceptar('un carrito MERGED no bloquea un carrito ACTIVE nuevo',
        'INSERT INTO carts (cart_id, guest_token_hash) VALUES (' || lit(c_k4) || q'[, RPAD('a', 64, 'a'))]');
    --
    -- 3. Líneas
    debe_aceptar('acepta linea con 99 unidades', ins_item(lit(c_k1), lit(c_p1), '99'));
    debe_rechazar('rechaza 100 unidades', ins_item(lit(c_k1), lit(c_p2), '100'), -2290);
    debe_rechazar('rechaza 0 unidades', ins_item(lit(c_k1), lit(c_p2), '0'), -2290);
    debe_rechazar('rechaza producto repetido en el carrito', ins_item(lit(c_k1), lit(c_p1), '1'), -1);
    debe_rechazar('rechaza product_id mal formado', ins_item(lit(c_k1), q'['abc']', '1'), -2290);
    debe_rechazar('rechaza linea de un carrito inexistente', ins_item(lit(c_k9), lit(c_p2), '1'), -2291);
    --
    -- 4. Cupones
    debe_aceptar('aplica GAMER10 al carrito',
        'UPDATE carts SET coupon_id = (SELECT coupon_id FROM coupons WHERE code = ''GAMER10'') '
        || 'WHERE cart_id = ' || lit(c_k1));
    debe_rechazar('rechaza cupon inexistente en el carrito',
        'UPDATE carts SET coupon_id = ' || lit(c_k9) || ' WHERE cart_id = ' || lit(c_k1), -2291);
    debe_rechazar('rechaza codigo de cupon en minusculas',
        'INSERT INTO coupons (coupon_id, code, discount_percent) VALUES (' || lit(c_k9) || q'[, 'gamer20', 20)]', -2290);
    debe_rechazar('rechaza descuento 0',
        'INSERT INTO coupons (coupon_id, code, discount_percent) VALUES (' || lit(c_k9) || q'[, 'GAMER0', 0)]', -2290);
    debe_rechazar('rechaza codigo de cupon repetido',
        'INSERT INTO coupons (coupon_id, code, discount_percent) VALUES (' || lit(c_k9) || q'[, 'GAMER10', 10)]', -1);
    debe_rechazar('no permite borrar un cupon en uso',
        'DELETE FROM coupons WHERE code = ''GAMER10''', -2292);
    --
    -- 5. Cotizaciones
    debe_aceptar('acepta cotizacion (solo hash del token)',
        'INSERT INTO cart_price_quotes (quote_id, cart_id, cart_version, price_fingerprint, token_hash, '
        || 'expires_at) VALUES (' || lit(c_q1) || ', ' || lit(c_k1)
        || q'[, 0, RPAD('f', 64, 'f'), RPAD('c', 64, 'c'), SYS_EXTRACT_UTC(SYSTIMESTAMP) + INTERVAL '10' MINUTE)]');
    debe_rechazar('rechaza token de cotizacion repetido',
        'INSERT INTO cart_price_quotes (quote_id, cart_id, cart_version, price_fingerprint, token_hash, '
        || 'expires_at) VALUES (' || lit(c_q2) || ', ' || lit(c_k1)
        || q'[, 0, RPAD('f', 64, 'f'), RPAD('c', 64, 'c'), SYS_EXTRACT_UTC(SYSTIMESTAMP) + INTERVAL '10' MINUTE)]', -1);
    debe_rechazar('rechaza cotizacion que vence antes de crearse',
        'INSERT INTO cart_price_quotes (quote_id, cart_id, cart_version, price_fingerprint, token_hash, '
        || 'expires_at) VALUES (' || lit(c_q2) || ', ' || lit(c_k1)
        || q'[, 0, RPAD('f', 64, 'f'), RPAD('d', 64, 'd'), SYS_EXTRACT_UTC(SYSTIMESTAMP) - INTERVAL '1' MINUTE)]', -2290);
    --
    -- 6. Reservas para crear pedidos
    debe_aceptar('acepta reserva ACTIVE', ins_reserva(lit(c_r1), lit(c_k1), lit(c_i1)));
    debe_rechazar('rechaza segunda reserva ACTIVE del mismo carrito',
        ins_reserva(lit(c_r2), lit(c_k1), lit(c_i2)), -1);
    debe_rechazar('rechaza COMPLETED sin completed_at',
        'UPDATE cart_reservations SET status = ''COMPLETED'' WHERE reservation_id = ' || lit(c_r1), -2290);
    debe_aceptar('libera la reserva (RELEASED)',
        'UPDATE cart_reservations SET status = ''RELEASED'' WHERE reservation_id = ' || lit(c_r1));
    debe_aceptar('acepta nueva reserva despues de liberar',
        ins_reserva(lit(c_r2), lit(c_k1), lit(c_i2)));
    debe_aceptar('completa la reserva con completed_at',
        'UPDATE cart_reservations SET status = ''COMPLETED'', completed_at = SYS_EXTRACT_UTC(SYSTIMESTAMP) '
        || 'WHERE reservation_id = ' || lit(c_r2));
    debe_rechazar('rechaza repetir la clave de idempotencia en el mismo carrito',
        ins_reserva(lit(c_r3), lit(c_k1), lit(c_i1)), -1);
    debe_rechazar('rechaza clave que no es UUID v4',
        ins_reserva(lit(c_r3), lit(c_k1), q'['11111111-1111-1111-1111-111111111111']'), -2290);
    --
    -- 7. Triggers de updated_at
    debe_aceptar('acepta fechas explicitas (preparacion de triggers)',
        'INSERT INTO carts (cart_id, user_id, created_at, updated_at) VALUES (' || lit(c_k5) || ', '
        || lit(c_u2) || q'[, TIMESTAMP '2020-01-01 00:00:00', TIMESTAMP '2020-01-01 00:00:00')]');
    BEGIN
        UPDATE carts SET version = version + 1 WHERE cart_id = c_k5;
        SELECT created_at, updated_at INTO v_ts1, v_ts2 FROM carts WHERE cart_id = c_k5;
        marcar('trigger de carts renueva updated_at',
            v_ts2 > TIMESTAMP '2020-01-01 00:00:00' AND v_ts1 = TIMESTAMP '2020-01-01 00:00:00',
            'created_at: ' || TO_CHAR(v_ts1) || ' updated_at: ' || TO_CHAR(v_ts2));
        INSERT INTO cart_items (cart_id, product_id, quantity, created_at, updated_at)
        VALUES (c_k5, c_p2, 1, TIMESTAMP '2020-01-01 00:00:00', TIMESTAMP '2020-01-01 00:00:00');
        UPDATE cart_items SET quantity = 2 WHERE cart_id = c_k5 AND product_id = c_p2;
        SELECT updated_at INTO v_ts2 FROM cart_items WHERE cart_id = c_k5 AND product_id = c_p2;
        marcar('trigger de cart_items renueva updated_at',
            v_ts2 > TIMESTAMP '2020-01-01 00:00:00', 'updated_at: ' || TO_CHAR(v_ts2));
    EXCEPTION
        WHEN OTHERS THEN marcar('triggers de updated_at', FALSE, SQLERRM);
    END;
    --
    -- 8. Borrado en cascada
    BEGIN
        DELETE FROM carts WHERE cart_id = c_k1;
        SELECT (SELECT COUNT(*) FROM cart_items WHERE cart_id = c_k1)
             + (SELECT COUNT(*) FROM cart_price_quotes WHERE cart_id = c_k1)
             + (SELECT COUNT(*) FROM cart_reservations WHERE cart_id = c_k1)
        INTO v_n FROM dual;
        marcar('borrar un carrito elimina lineas, cotizaciones y reservas', v_n = 0, 'quedaron: ' || v_n);
    EXCEPTION
        WHEN OTHERS THEN marcar('borrado en cascada', FALSE, SQLERRM);
    END;
    --
    -- 9. Estructura creada por V1
    SELECT COUNT(*) INTO v_n FROM user_objects WHERE status <> 'VALID';
    marcar('no hay objetos invalidos en el esquema', v_n = 0, v_n || ' objetos invalidos');
    SELECT COUNT(*) INTO v_n FROM user_triggers
    WHERE trigger_name IN ('TRG_COUPONS_UPDATED_AT', 'TRG_CARTS_UPDATED_AT', 'TRG_CART_ITEMS_UPDATED_AT')
      AND status = 'ENABLED';
    marcar('los 3 triggers de carrito estan habilitados', v_n = 3, 'encontrados: ' || v_n);
    SELECT COUNT(*) INTO v_n FROM user_indexes
    WHERE index_name IN ('UQ_CARTS_USER_ACTIVE', 'UQ_CARTS_GUEST_ACTIVE', 'UQ_CART_RESERVATIONS_ACTIVA')
      AND uniqueness = 'UNIQUE';
    marcar('existen los 3 indices unicos condicionales', v_n = 3, 'encontrados: ' || v_n);
    --
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('== Resultado: ' || v_ok || ' OK, ' || v_fallas
        || ' fallas (datos de prueba revertidos) ==');
    IF v_fallas = 0 THEN
        DBMS_OUTPUT.PUT_LINE('V1/V2 de ms-carrito validadas.');
    END IF;
END;
/
