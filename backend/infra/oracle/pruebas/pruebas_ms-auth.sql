-- =====================================================================
-- ms-auth · pruebas aisladas de V1 y V2 en Oracle 21c
--
-- Ejecutar conectado como GZ_PRUEBA_BACKEND (esquema desechable), DESPUÉS
-- de haber ejecutado ahí V1__create_auth_schema.sql y
-- V2__seed_auth_reference_data.sql. Usar F5 y mirar "Script Output".
--
-- No ejecutar en MS_AUTH. Los datos de prueba se revierten con ROLLBACK,
-- así que el script se puede repetir. Resultado esperado: "0 fallas".
-- =====================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED

DECLARE
    v_ok     PLS_INTEGER := 0;
    v_fallas PLS_INTEGER := 0;
    v_n      NUMBER;
    v_ts1    TIMESTAMP(6);
    v_ts2    TIMESTAMP(6);
    -- Identificadores ficticios
    c_u1 CONSTANT VARCHAR2(36) := '11111111-1111-4111-8111-111111111111';
    c_u2 CONSTANT VARCHAR2(36) := '22222222-2222-4222-8222-222222222222';
    c_u3 CONSTANT VARCHAR2(36) := '33333333-3333-4333-8333-333333333333';
    c_u4 CONSTANT VARCHAR2(36) := '44444444-4444-4444-8444-444444444444';
    c_f1 CONSTANT VARCHAR2(36) := 'f0000000-0000-4000-8000-000000000001';
    c_r1 CONSTANT VARCHAR2(36) := 'e0000000-0000-4000-8000-000000000001';
    c_r2 CONSTANT VARCHAR2(36) := 'e0000000-0000-4000-8000-000000000002';
    c_x1 CONSTANT VARCHAR2(36) := 'c0000000-0000-4000-8000-000000000001';
    c_t1 CONSTANT VARCHAR2(36) := 'd0000000-0000-4000-8000-000000000001';
    c_t2 CONSTANT VARCHAR2(36) := 'd0000000-0000-4000-8000-000000000002';
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
    -- Arma un INSERT de cuenta. Cada parámetro es una expresión SQL.
    FUNCTION ins_user(p_id VARCHAR2, p_rut VARCHAR2, p_nombre VARCHAR2, p_email VARCHAR2,
                      p_commune VARCHAR2 DEFAULT '101',
                      p_role VARCHAR2 DEFAULT '''CLIENTE''',
                      p_status VARCHAR2 DEFAULT '''ACTIVE''') RETURN VARCHAR2 IS
    BEGIN
        RETURN 'INSERT INTO users (id, rut_normalized, nombre, apellidos, email_normalized, '
            || 'password_hash, commune_id, direccion, role_code, status) VALUES ('
            || p_id || ', ' || p_rut || ', ' || p_nombre || ', ''Prueba'', ' || p_email
            || ', ''hash-de-prueba'', ' || p_commune || ', ''Calle 123'', '
            || p_role || ', ' || p_status || ')';
    END;
BEGIN
    DBMS_OUTPUT.PUT_LINE('== Pruebas V1/V2 ms-auth en el esquema ' || USER || ' ==');
    --
    -- 1. Datos de referencia (V2)
    SELECT COUNT(*) INTO v_n FROM roles WHERE role_code IN ('CLIENTE', 'ADMINISTRADOR');
    marcar('semilla: roles CLIENTE y ADMINISTRADOR', v_n = 2, 'encontrados: ' || v_n);
    SELECT COUNT(*) INTO v_n FROM regions;
    marcar('semilla: 3 regiones', v_n = 3, 'encontradas: ' || v_n);
    SELECT COUNT(*) INTO v_n FROM communes;
    marcar('semilla: 16 comunas', v_n = 16, 'encontradas: ' || v_n);
    SELECT COUNT(*) INTO v_n FROM communes c JOIN regions r ON r.region_id = c.region_id
    WHERE r.code = 'metropolitana';
    marcar('semilla: 6 comunas en metropolitana', v_n = 6, 'encontradas: ' || v_n);
    --
    -- 2. Altas válidas
    debe_aceptar('acepta cuenta CLIENTE completa',
        ins_user(lit(c_u1), q'['190110222']', q'['Ana']', q'['ana@gmail.com']'));
    debe_aceptar('acepta cuenta ADMINISTRADOR con RUT terminado en K',
        ins_user(lit(c_u2), q'['12345678K']', q'['Beto']', q'['beto@duoc.cl']', '201', q'['ADMINISTRADOR']'));
    --
    -- 3. Unicidad
    debe_rechazar('rechaza correo repetido',
        ins_user(lit(c_u3), q'['111111111']', q'['Carla']', q'['ana@gmail.com']'), -1);
    debe_rechazar('rechaza RUT repetido',
        ins_user(lit(c_u3), q'['190110222']', q'['Carla']', q'['carla@gmail.com']'), -1);
    --
    -- 4. Formato y largos
    debe_rechazar('rechaza RUT con guion',
        ins_user(lit(c_u3), q'['1234567-5']', q'['Carla']', q'['carla@gmail.com']'), -2290);
    debe_rechazar('rechaza RUT con k minuscula',
        ins_user(lit(c_u3), q'['12345678k']', q'['Carla']', q'['carla@gmail.com']'), -2290);
    debe_rechazar('rechaza RUT con puntos (mas de 9 caracteres)',
        ins_user(lit(c_u3), q'['12.345.678-5']', q'['Carla']', q'['carla@gmail.com']'), -12899);
    debe_rechazar('rechaza correo con mayusculas',
        ins_user(lit(c_u3), q'['111111111']', q'['Carla']', q'['Carla@Gmail.com']'), -2290);
    debe_rechazar('rechaza correo sin arroba',
        ins_user(lit(c_u3), q'['111111111']', q'['Carla']', q'['carlagmail.com']'), -2290);
    debe_rechazar('rechaza nombre solo con espacios',
        ins_user(lit(c_u3), q'['111111111']', q'['   ']', q'['carla@gmail.com']'), -2290);
    debe_rechazar('rechaza nombre de 51 caracteres',
        ins_user(lit(c_u3), q'['111111111']', q'[RPAD('a', 51, 'a')]', q'['carla@gmail.com']'), -12899);
    debe_rechazar('rechaza ID en mayusculas',
        ins_user(q'['AAAAAAAA-1111-4111-8111-111111111111']', q'['111111111']', q'['Carla']', q'['carla@gmail.com']'), -2290);
    --
    -- 5. Referencias, estado y perfil
    debe_rechazar('rechaza comuna inexistente',
        ins_user(lit(c_u3), q'['111111111']', q'['Carla']', q'['carla@gmail.com']', '999'), -2291);
    debe_rechazar('rechaza rol inexistente',
        ins_user(lit(c_u3), q'['111111111']', q'['Carla']', q'['carla@gmail.com']', '101', q'['SUPERADMIN']'), -2291);
    debe_rechazar('rechaza estado invalido',
        ins_user(lit(c_u3), q'['111111111']', q'['Carla']', q'['carla@gmail.com']', '101', q'['CLIENTE']', q'['BLOQUEADO']'), -2290);
    debe_rechazar('rechaza perfil incompleto sin anonimizar (sin direccion)',
        'INSERT INTO users (id, rut_normalized, nombre, apellidos, email_normalized, password_hash, '
        || 'commune_id, role_code) VALUES (' || lit(c_u3)
        || q'[, '111111111', 'Carla', 'Prueba', 'carla@gmail.com', 'hash', 101, 'CLIENTE')]', -2290);
    debe_rechazar('no permite borrar una comuna en uso',
        'DELETE FROM communes WHERE commune_id = 101', -2292);
    --
    -- 6. Sesiones (refresh tokens)
    debe_aceptar('acepta familia de sesion',
        'INSERT INTO refresh_token_families (family_id, user_id, absolute_expires_at) VALUES ('
        || lit(c_f1) || ', ' || lit(c_u1) || q'[, SYS_EXTRACT_UTC(SYSTIMESTAMP) + INTERVAL '30' DAY)]');
    debe_aceptar('acepta refresh token (solo hash)',
        'INSERT INTO refresh_tokens (id, family_id, token_hash, expires_at) VALUES ('
        || lit(c_r1) || ', ' || lit(c_f1) || q'[, RPAD('a', 64, 'a'), SYS_EXTRACT_UTC(SYSTIMESTAMP) + INTERVAL '7' DAY)]');
    debe_rechazar('rechaza refresh token con hash repetido',
        'INSERT INTO refresh_tokens (id, family_id, token_hash, expires_at) VALUES ('
        || lit(c_r2) || ', ' || lit(c_f1) || q'[, RPAD('a', 64, 'a'), SYS_EXTRACT_UTC(SYSTIMESTAMP) + INTERVAL '7' DAY)]', -1);
    debe_rechazar('rechaza familia que vence antes de crearse',
        'INSERT INTO refresh_token_families (family_id, user_id, absolute_expires_at) VALUES ('
        || lit(c_r2) || ', ' || lit(c_u1) || q'[, SYS_EXTRACT_UTC(SYSTIMESTAMP) - INTERVAL '1' DAY)]', -2290);
    debe_rechazar('rechaza familia de una cuenta inexistente',
        'INSERT INTO refresh_token_families (family_id, user_id, absolute_expires_at) VALUES ('
        || lit(c_r2) || ', ' || lit(c_u3) || q'[, SYS_EXTRACT_UTC(SYSTIMESTAMP) + INTERVAL '30' DAY)]', -2291);
    BEGIN
        DELETE FROM refresh_token_families WHERE family_id = c_f1;
        SELECT COUNT(*) INTO v_n FROM refresh_tokens WHERE family_id = c_f1;
        marcar('borrar la familia elimina sus refresh tokens', v_n = 0, 'quedaron: ' || v_n);
    EXCEPTION
        WHEN OTHERS THEN marcar('borrado de familia', FALSE, SQLERRM);
    END;
    --
    -- 7. CSRF
    debe_aceptar('acepta contexto CSRF',
        'INSERT INTO csrf_contexts (context_id, context_hash, expires_at) VALUES ('
        || lit(c_x1) || q'[, RPAD('b', 64, 'b'), SYS_EXTRACT_UTC(SYSTIMESTAMP) + INTERVAL '2' HOUR)]');
    debe_aceptar('acepta token CSRF del contexto',
        'INSERT INTO csrf_tokens (token_id, context_id, token_hash, expires_at) VALUES ('
        || lit(c_t1) || ', ' || lit(c_x1) || q'[, RPAD('c', 64, 'c'), SYS_EXTRACT_UTC(SYSTIMESTAMP) + INTERVAL '2' HOUR)]');
    debe_rechazar('rechaza token CSRF con hash repetido',
        'INSERT INTO csrf_tokens (token_id, context_id, token_hash, expires_at) VALUES ('
        || lit(c_t2) || ', ' || lit(c_x1) || q'[, RPAD('c', 64, 'c'), SYS_EXTRACT_UTC(SYSTIMESTAMP) + INTERVAL '2' HOUR)]', -1);
    BEGIN
        DELETE FROM csrf_contexts WHERE context_id = c_x1;
        SELECT COUNT(*) INTO v_n FROM csrf_tokens WHERE context_id = c_x1;
        marcar('borrar el contexto elimina sus tokens CSRF', v_n = 0, 'quedaron: ' || v_n);
    EXCEPTION
        WHEN OTHERS THEN marcar('borrado de contexto CSRF', FALSE, SQLERRM);
    END;
    --
    -- 8. Baja / anonimización
    debe_rechazar('rechaza anonimizar a medias',
        'UPDATE users SET anonymized_at = SYS_EXTRACT_UTC(SYSTIMESTAMP) WHERE id = ' || lit(c_u1), -2290);
    debe_aceptar('acepta anonimizacion completa',
        q'[UPDATE users SET anonymized_at = SYS_EXTRACT_UTC(SYSTIMESTAMP), status = 'INACTIVE', ]'
        || q'[rut_normalized = NULL, nombre = NULL, apellidos = NULL, password_hash = NULL, ]'
        || q'[commune_id = NULL, direccion = NULL, email_normalized = 'deleted+' || id || '@invalid.local' ]'
        || 'WHERE id = ' || lit(c_u1));
    debe_aceptar('el correo y el RUT de la cuenta anonimizada quedan libres',
        ins_user(lit(c_u3), q'['190110222']', q'['Carla']', q'['ana@gmail.com']'));
    --
    -- 9. Trigger de updated_at
    debe_aceptar('acepta fechas explicitas (preparacion del trigger)',
        'INSERT INTO users (id, rut_normalized, nombre, apellidos, email_normalized, password_hash, '
        || 'commune_id, direccion, role_code, created_at, updated_at) VALUES (' || lit(c_u4)
        || q'[, '98765432', 'Dani', 'Prueba', 'dani@gmail.com', 'hash', 301, 'Calle 1', 'CLIENTE', ]'
        || q'[TIMESTAMP '2020-01-01 00:00:00', TIMESTAMP '2020-01-01 00:00:00')]');
    BEGIN
        UPDATE users SET nombre = 'Daniel' WHERE id = c_u4;
        SELECT created_at, updated_at INTO v_ts1, v_ts2 FROM users WHERE id = c_u4;
        marcar('el trigger renueva updated_at al actualizar',
            v_ts2 > TIMESTAMP '2020-01-01 00:00:00', 'updated_at: ' || TO_CHAR(v_ts2));
        marcar('created_at no cambia al actualizar',
            v_ts1 = TIMESTAMP '2020-01-01 00:00:00', 'created_at: ' || TO_CHAR(v_ts1));
    EXCEPTION
        WHEN OTHERS THEN marcar('trigger de updated_at', FALSE, SQLERRM);
    END;
    --
    -- 10. Estructura creada por V1
    SELECT COUNT(*) INTO v_n FROM user_objects WHERE status <> 'VALID';
    marcar('no hay objetos invalidos en el esquema', v_n = 0, v_n || ' objetos invalidos');
    SELECT COUNT(*) INTO v_n FROM user_triggers
    WHERE trigger_name = 'TRG_USERS_UPDATED_AT' AND status = 'ENABLED';
    marcar('trigger de users habilitado', v_n = 1);
    SELECT COUNT(*) INTO v_n FROM user_indexes WHERE table_name = 'USERS';
    marcar('users tiene 6 indices', v_n = 6, 'encontrados: ' || v_n);
    SELECT COUNT(*) INTO v_n FROM user_constraints
    WHERE table_name = 'USERS' AND constraint_name LIKE 'CK\_USERS\_%' ESCAPE '\';
    marcar('users tiene las 7 restricciones CHECK con nombre', v_n = 7, 'encontradas: ' || v_n);
    --
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('== Resultado: ' || v_ok || ' OK, ' || v_fallas
        || ' fallas (datos de prueba revertidos) ==');
    IF v_fallas = 0 THEN
        DBMS_OUTPUT.PUT_LINE('V1/V2 de ms-auth validadas.');
    END IF;
END;
/
