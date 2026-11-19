-- =====================================================================
-- ms-pedido · pruebas aisladas de V1 en Oracle 21c
--
-- Ejecutar conectado como GZ_PRUEBA_BACKEND (esquema desechable), DESPUÉS
-- de haber ejecutado ahí V1__create_order_schema.sql. Usar F5 y mirar
-- "Script Output".
--
-- No ejecutar en MS_PEDIDO. Los datos de prueba se revierten con ROLLBACK,
-- así que el script se puede repetir. Resultado esperado: "0 fallas".
-- =====================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED

DECLARE
    v_ok     PLS_INTEGER := 0;
    v_fallas PLS_INTEGER := 0;
    v_n      NUMBER;
    v_txt    VARCHAR2(100);
    v_sub    NUMBER;
    v_pct    NUMBER;
    v_desc   NUMBER;
    v_total  NUMBER;
    v_iva    NUMBER;
    -- Identificadores ficticios (cuentas de ms-auth y productos de ms-catalogo)
    c_u1  CONSTANT VARCHAR2(36) := '11111111-1111-4111-8111-111111111111';
    c_u2  CONSTANT VARCHAR2(36) := '22222222-2222-4222-8222-222222222222';
    c_p1  CONSTANT VARCHAR2(36) := 'b0000000-0000-4000-8000-000000000001';
    c_p2  CONSTANT VARCHAR2(36) := 'b0000000-0000-4000-8000-000000000002';
    c_o1  CONSTANT VARCHAR2(36) := 'a0000000-0000-4000-8000-000000000001';
    c_o2  CONSTANT VARCHAR2(36) := 'a0000000-0000-4000-8000-000000000002';
    c_o3  CONSTANT VARCHAR2(36) := 'a0000000-0000-4000-8000-000000000003';
    c_o4  CONSTANT VARCHAR2(36) := 'a0000000-0000-4000-8000-000000000004';
    c_o9  CONSTANT VARCHAR2(36) := 'a0000000-0000-4000-8000-000000000009';
    c_it1 CONSTANT VARCHAR2(36) := 'e0000000-0000-4000-8000-000000000001';
    c_it2 CONSTANT VARCHAR2(36) := 'e0000000-0000-4000-8000-000000000002';
    c_it9 CONSTANT VARCHAR2(36) := 'e0000000-0000-4000-8000-000000000009';
    -- Claves de idempotencia UUID v4
    c_i1  CONSTANT VARCHAR2(36) := '11111111-1111-4111-8111-000000000001';
    c_i2  CONSTANT VARCHAR2(36) := '11111111-1111-4111-8111-000000000002';
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
    FUNCTION ins_pedido(p_id VARCHAR2, p_user VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN 'INSERT INTO orders (order_id, user_id) VALUES (' || p_id || ', ' || p_user || ')';
    END;
    --
    -- Cada parámetro es una expresión SQL.
    FUNCTION ins_linea(p_id VARCHAR2, p_order VARCHAR2, p_product VARCHAR2, p_titulo VARCHAR2,
                       p_precio VARCHAR2, p_qty VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN 'INSERT INTO order_items (order_item_id, order_id, product_id, title_snapshot, '
            || 'unit_price_clp, quantity) VALUES (' || p_id || ', ' || p_order || ', ' || p_product
            || ', ' || p_titulo || ', ' || p_precio || ', ' || p_qty || ')';
    END;
    --
    FUNCTION ins_clave(p_user VARCHAR2, p_key VARCHAR2, p_order VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN 'INSERT INTO order_idempotency (user_id, idempotency_key, cart_version, cart_fingerprint, '
            || 'order_id) VALUES (' || p_user || ', ' || p_key || ', 7, RPAD(''f'', 64, ''f''), '
            || p_order || ')';
    END;
BEGIN
    DBMS_OUTPUT.PUT_LINE('== Pruebas V1 ms-pedido en el esquema ' || USER || ' ==');
    --
    -- 1. Pedido completo: Elden Ring x1 con GAMER10 (ejemplo del contrato)
    debe_aceptar('acepta pedido', ins_pedido(lit(c_o1), lit(c_u1)));
    debe_aceptar('acepta linea con snapshot de titulo y precio',
        ins_linea(lit(c_it1), lit(c_o1), lit(c_p1), q'['Elden Ring']', '59990', '1'));
    debe_aceptar('acepta snapshot del cupon aplicado',
        'INSERT INTO order_coupon_snapshots (order_id, coupon_code_snapshot, discount_percent_snapshot) '
        || 'VALUES (' || lit(c_o1) || q'[, 'GAMER10', 10)]');
    debe_aceptar('acepta clave de idempotencia del pedido', ins_clave(lit(c_u1), lit(c_i1), lit(c_o1)));
    --
    -- 2. Valores por defecto e importes derivados
    BEGIN
        SELECT status, pricing_policy_version INTO v_txt, v_n FROM orders WHERE order_id = c_o1;
        marcar('estado inicial REGISTRADO_SIN_PAGO y politica 1',
            v_txt = 'REGISTRADO_SIN_PAGO' AND v_n = 1, 'estado: ' || v_txt || ', politica: ' || v_n);
        SELECT SUM(unit_price_clp * quantity) INTO v_sub FROM order_items WHERE order_id = c_o1;
        SELECT discount_percent_snapshot INTO v_pct FROM order_coupon_snapshots WHERE order_id = c_o1;
        v_desc  := ROUND(v_sub * v_pct / 100);
        v_total := v_sub - v_desc;
        v_iva   := ROUND(v_total * 19 / 119);
        marcar('importes derivados = ejemplo del contrato (5999 / 53991 / 8620)',
            v_desc = 5999 AND v_total = 53991 AND v_iva = 8620,
            'descuento ' || v_desc || ', total ' || v_total || ', IVA ' || v_iva);
    EXCEPTION
        WHEN OTHERS THEN marcar('lectura del pedido', FALSE, SQLERRM);
    END;
    --
    -- 3. Pedido
    debe_rechazar('rechaza estado distinto de REGISTRADO_SIN_PAGO',
        'UPDATE orders SET status = ''PAGADO'' WHERE order_id = ' || lit(c_o1), -2290);
    debe_rechazar('rechaza user_id mal formado', ins_pedido(lit(c_o9), q'['usuario-1']'), -2290);
    debe_rechazar('no permite borrar un pedido con lineas',
        'DELETE FROM orders WHERE order_id = ' || lit(c_o1), -2292);
    --
    -- 4. Líneas
    debe_rechazar('rechaza precio 0',
        ins_linea(lit(c_it9), lit(c_o1), lit(c_p2), q'['Otro juego']', '0', '1'), -2290);
    debe_rechazar('rechaza 100 unidades',
        ins_linea(lit(c_it9), lit(c_o1), lit(c_p2), q'['Otro juego']', '1000', '100'), -2290);
    debe_rechazar('rechaza titulo solo con espacios',
        ins_linea(lit(c_it9), lit(c_o1), lit(c_p2), q'['   ']', '1000', '1'), -2290);
    debe_rechazar('rechaza titulo de 151 caracteres',
        ins_linea(lit(c_it9), lit(c_o1), lit(c_p2), q'[RPAD('a', 151, 'a')]', '1000', '1'), -12899);
    debe_rechazar('rechaza producto repetido en el pedido',
        ins_linea(lit(c_it9), lit(c_o1), lit(c_p1), q'['Elden Ring']', '59990', '1'), -1);
    debe_rechazar('rechaza linea de un pedido inexistente',
        ins_linea(lit(c_it9), lit(c_o9), lit(c_p2), q'['Otro juego']', '1000', '1'), -2291);
    debe_aceptar('acepta linea sin product_id (producto ya no existe)',
        ins_linea(lit(c_it2), lit(c_o1), 'NULL', q'['Juego retirado']', '1000', '1'));
    --
    -- 5. Cupón aplicado
    debe_rechazar('rechaza segundo cupon en el mismo pedido',
        'INSERT INTO order_coupon_snapshots (order_id, coupon_code_snapshot, discount_percent_snapshot) '
        || 'VALUES (' || lit(c_o1) || q'[, 'DUOC2026', 10)]', -1);
    debe_aceptar('acepta pedido de otro cliente', ins_pedido(lit(c_o2), lit(c_u2)));
    debe_rechazar('rechaza descuento 0 en el snapshot',
        'INSERT INTO order_coupon_snapshots (order_id, coupon_code_snapshot, discount_percent_snapshot) '
        || 'VALUES (' || lit(c_o2) || q'[, 'GAMER10', 0)]', -2290);
    --
    -- 6. Idempotencia
    debe_aceptar('acepta otro pedido del primer cliente', ins_pedido(lit(c_o3), lit(c_u1)));
    debe_aceptar('acepta otro pedido del segundo cliente', ins_pedido(lit(c_o4), lit(c_u2)));
    debe_rechazar('rechaza repetir la clave del mismo cliente',
        ins_clave(lit(c_u1), lit(c_i1), lit(c_o3)), -1);
    debe_aceptar('acepta la misma clave para otro cliente',
        ins_clave(lit(c_u2), lit(c_i1), lit(c_o2)));
    debe_rechazar('rechaza clave ligada a un pedido de otro cliente',
        ins_clave(lit(c_u1), lit(c_i2), lit(c_o4)), -2291);
    debe_rechazar('rechaza segunda clave para el mismo pedido',
        ins_clave(lit(c_u1), lit(c_i2), lit(c_o1)), -1);
    debe_rechazar('rechaza clave que no es UUID v4',
        ins_clave(lit(c_u1), q'['11111111-1111-1111-1111-111111111111']', lit(c_o3)), -2290);
    --
    -- 7. Estructura creada por V1
    SELECT COUNT(*) INTO v_n FROM user_objects WHERE status <> 'VALID';
    marcar('no hay objetos invalidos en el esquema', v_n = 0, v_n || ' objetos invalidos');
    SELECT COUNT(*) INTO v_n FROM user_constraints
    WHERE constraint_type = 'R'
      AND table_name IN ('ORDER_ITEMS', 'ORDER_COUPON_SNAPSHOTS', 'ORDER_IDEMPOTENCY');
    marcar('existen las 3 claves foraneas hacia orders', v_n = 3, 'encontradas: ' || v_n);
    SELECT COUNT(*) INTO v_n FROM user_indexes WHERE table_name = 'ORDERS';
    marcar('orders tiene 5 indices', v_n = 5, 'encontrados: ' || v_n);
    --
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('== Resultado: ' || v_ok || ' OK, ' || v_fallas
        || ' fallas (datos de prueba revertidos) ==');
    IF v_fallas = 0 THEN
        DBMS_OUTPUT.PUT_LINE('V1 de ms-pedido validada.');
    END IF;
END;
/
