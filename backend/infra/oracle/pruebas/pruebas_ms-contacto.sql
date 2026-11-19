-- =====================================================================
-- ms-contacto · pruebas aisladas de V1 en Oracle 21c
--
-- Ejecutar conectado como GZ_PRUEBA_BACKEND (esquema desechable), DESPUÉS
-- de haber ejecutado ahí V1__create_contact_schema.sql. Usar F5 (Run Script)
-- y mirar la pestaña "Script Output".
--
-- No ejecutar en MS_CONTACTO: ese esquema es de trabajo y lo maneja Flyway.
-- Todos los datos de prueba se revierten con ROLLBACK al final, así que el
-- script se puede repetir.
--
-- Resultado esperado: "0 fallas".
-- =====================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED

DECLARE
    v_ok     PLS_INTEGER := 0;
    v_fallas PLS_INTEGER := 0;
    v_n      NUMBER;
    v_txt    VARCHAR2(4000);
    v_ts1    TIMESTAMP(6);
    v_ts2    TIMESTAMP(6);
    v_id1    VARCHAR2(36);
    v_id2    VARCHAR2(36);
    v_id3    VARCHAR2(36);
    v_id4    VARCHAR2(36);
    v_id5    VARCHAR2(36);
    v_id6    VARCHAR2(36);
    -- Cuenta ficticia de ms-auth
    c_u1     CONSTANT VARCHAR2(36)  := 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1';
    -- Valores válidos por defecto, escritos como expresiones SQL
    e_nombre  CONSTANT VARCHAR2(100) := q'['Sara Morales']';
    e_correo  CONSTANT VARCHAR2(100) := q'['sara@gmail.com']';
    e_asunto  CONSTANT VARCHAR2(100) := q'['Consulta sobre catalogo']';
    e_mensaje CONSTANT VARCHAR2(100) := q'['Quisiera consultar por un juego.']';
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
    FUNCTION f_id(p_n PLS_INTEGER) RETURN VARCHAR2 IS
    BEGIN
        RETURN '00000000-0000-4000-8000-' || LPAD(TO_CHAR(p_n), 12, '0');
    END;
    --
    FUNCTION lit(p_valor VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN '''' || REPLACE(p_valor, '''', '''''') || '''';
    END;
    --
    FUNCTION ins(p_id VARCHAR2, p_user VARCHAR2, p_nombre VARCHAR2, p_correo VARCHAR2,
                 p_asunto VARCHAR2, p_mensaje VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN 'INSERT INTO contact_messages (message_id, user_id, sender_name_snapshot, '
            || 'sender_email_snapshot, subject, message_text) VALUES ('
            || p_id || ', ' || p_user || ', ' || p_nombre || ', ' || p_correo || ', '
            || p_asunto || ', ' || p_mensaje || ')';
    END;
BEGIN
    v_id1 := f_id(1);
    v_id2 := f_id(2);
    v_id3 := f_id(3);
    v_id4 := f_id(4);
    v_id5 := f_id(5);
    v_id6 := f_id(6);
    DBMS_OUTPUT.PUT_LINE('== Pruebas V1 ms-contacto en el esquema ' || USER || ' ==');
    --
    -- 1. Altas válidas
    debe_aceptar('acepta mensaje anonimo (sin cuenta)',
        ins(lit(v_id1), 'NULL', e_nombre, e_correo, e_asunto, e_mensaje));
    debe_aceptar('acepta mensaje asociado a una cuenta',
        ins(lit(v_id2), lit(c_u1), e_nombre, e_correo, e_asunto, e_mensaje));
    debe_aceptar('acepta textos en el limite exacto (100/100/120/500)',
        ins(lit(v_id3), 'NULL', q'[RPAD('a', 100, 'a')]', q'[RPAD('a', 90, 'a') || '@gmail.com']',
            q'[RPAD('a', 120, 'a')]', q'[RPAD('a', 500, 'a')]'));
    debe_aceptar('acepta 500 caracteres no ASCII en el mensaje (largo en CHAR, no bytes)',
        ins(lit(v_id4), 'NULL', e_nombre, e_correo, e_asunto,
            q'[RPAD(UNISTR('\00F1'), 500, UNISTR('\00F1'))]'));
    --
    -- 2. Largos máximos del contrato
    debe_rechazar('rechaza nombre de 101 caracteres',
        ins(lit(f_id(90)), 'NULL', q'[RPAD('a', 101, 'a')]', e_correo, e_asunto, e_mensaje), -12899);
    debe_rechazar('rechaza correo de 101 caracteres',
        ins(lit(f_id(91)), 'NULL', e_nombre, q'[RPAD('a', 91, 'a') || '@gmail.com']', e_asunto, e_mensaje), -12899);
    debe_rechazar('rechaza asunto de 121 caracteres',
        ins(lit(f_id(92)), 'NULL', e_nombre, e_correo, q'[RPAD('a', 121, 'a')]', e_mensaje), -12899);
    debe_rechazar('rechaza mensaje de 501 caracteres',
        ins(lit(f_id(93)), 'NULL', e_nombre, e_correo, e_asunto, q'[RPAD('a', 501, 'a')]'), -12899);
    --
    -- 3. Campos vacíos o solo con espacios
    debe_rechazar('rechaza nombre vacio',
        ins(lit(f_id(94)), 'NULL', q'['']', e_correo, e_asunto, e_mensaje), -1400);
    debe_rechazar('rechaza nombre solo con espacios',
        ins(lit(f_id(94)), 'NULL', q'['   ']', e_correo, e_asunto, e_mensaje), -2290);
    debe_rechazar('rechaza asunto solo con espacios',
        ins(lit(f_id(94)), 'NULL', e_nombre, e_correo, q'['   ']', e_mensaje), -2290);
    debe_rechazar('rechaza mensaje solo con espacios',
        ins(lit(f_id(94)), 'NULL', e_nombre, e_correo, e_asunto, q'['   ']'), -2290);
    --
    -- 4. Correo normalizado
    debe_rechazar('rechaza correo con mayusculas',
        ins(lit(f_id(95)), 'NULL', e_nombre, q'['Sara@Gmail.com']', e_asunto, e_mensaje), -2290);
    debe_rechazar('rechaza correo con espacios externos',
        ins(lit(f_id(95)), 'NULL', e_nombre, q'[' sara@gmail.com']', e_asunto, e_mensaje), -2290);
    debe_rechazar('rechaza correo sin arroba',
        ins(lit(f_id(95)), 'NULL', e_nombre, q'['saragmail.com']', e_asunto, e_mensaje), -2290);
    debe_rechazar('rechaza correo solo con espacios',
        ins(lit(f_id(95)), 'NULL', e_nombre, q'['   ']', e_asunto, e_mensaje), -2290);
    --
    -- 5. Identificadores
    debe_rechazar('rechaza ID repetido',
        ins(lit(v_id1), 'NULL', e_nombre, e_correo, e_asunto, e_mensaje), -1);
    debe_rechazar('rechaza ID en mayusculas',
        ins(q'['ABCDEF00-0000-4000-8000-000000000001']', 'NULL', e_nombre, e_correo, e_asunto, e_mensaje), -2290);
    debe_rechazar('rechaza ID mal formado',
        ins(q'['abc']', 'NULL', e_nombre, e_correo, e_asunto, e_mensaje), -2290);
    debe_rechazar('rechaza user_id mal formado',
        ins(lit(f_id(96)), q'['usuario-1']', e_nombre, e_correo, e_asunto, e_mensaje), -2290);
    --
    -- 6. Valores por defecto
    BEGIN
        SELECT status, created_at INTO v_txt, v_ts1
        FROM contact_messages WHERE message_id = v_id1;
        marcar('estado inicial es RECIBIDO', v_txt = 'RECIBIDO', 'estado: ' || v_txt);
        marcar('created_at se llena en UTC',
            v_ts1 BETWEEN SYS_EXTRACT_UTC(SYSTIMESTAMP) - INTERVAL '5' MINUTE
                      AND SYS_EXTRACT_UTC(SYSTIMESTAMP) + INTERVAL '1' MINUTE,
            'created_at: ' || TO_CHAR(v_ts1));
    EXCEPTION
        WHEN OTHERS THEN marcar('lectura del mensaje 1', FALSE, SQLERRM);
    END;
    --
    -- 7. Estados
    debe_aceptar('permite pasar a LEIDO',
        'UPDATE contact_messages SET status = ''LEIDO'' WHERE message_id = ' || lit(v_id1));
    debe_aceptar('permite pasar a CERRADO',
        'UPDATE contact_messages SET status = ''CERRADO'' WHERE message_id = ' || lit(v_id1));
    debe_rechazar('rechaza estado no permitido (ARCHIVADO)',
        'UPDATE contact_messages SET status = ''ARCHIVADO'' WHERE message_id = ' || lit(v_id1), -2290);
    debe_rechazar('rechaza estado nulo',
        'UPDATE contact_messages SET status = NULL WHERE message_id = ' || lit(v_id1), -1407);
    --
    -- 8. Trigger de updated_at
    debe_aceptar('acepta fechas explicitas (preparacion del trigger)',
        'INSERT INTO contact_messages (message_id, sender_name_snapshot, sender_email_snapshot, '
        || 'subject, message_text, created_at, updated_at) VALUES (' || lit(v_id5)
        || q'[, 'Ana', 'ana@gmail.com', 'Asunto', 'Mensaje', TIMESTAMP '2020-01-01 00:00:00', TIMESTAMP '2020-01-01 00:00:00')]');
    BEGIN
        UPDATE contact_messages SET status = 'LEIDO' WHERE message_id = v_id5;
        SELECT created_at, updated_at INTO v_ts1, v_ts2
        FROM contact_messages WHERE message_id = v_id5;
        marcar('el trigger renueva updated_at al actualizar',
            v_ts2 > TIMESTAMP '2020-01-01 00:00:00', 'updated_at: ' || TO_CHAR(v_ts2));
        marcar('created_at no cambia al actualizar',
            v_ts1 = TIMESTAMP '2020-01-01 00:00:00', 'created_at: ' || TO_CHAR(v_ts1));
    EXCEPTION
        WHEN OTHERS THEN marcar('trigger de updated_at', FALSE, SQLERRM);
    END;
    --
    -- 9. Borrados (baja de cuenta y eliminación manual del administrador)
    debe_aceptar('acepta segundo mensaje de la misma cuenta',
        ins(lit(v_id6), lit(c_u1), e_nombre, e_correo, e_asunto, e_mensaje));
    BEGIN
        DELETE FROM contact_messages WHERE user_id = c_u1;
        v_n := SQL%ROWCOUNT;
        marcar('borra los mensajes de una cuenta (baja de usuario)', v_n = 2, 'filas borradas: ' || v_n);
    EXCEPTION
        WHEN OTHERS THEN marcar('borrado por cuenta', FALSE, SQLERRM);
    END;
    debe_aceptar('permite eliminar un mensaje por ID',
        'DELETE FROM contact_messages WHERE message_id = ' || lit(v_id3));
    --
    -- 10. Estructura creada por V1
    SELECT COUNT(*) INTO v_n FROM user_objects WHERE status <> 'VALID';
    marcar('no hay objetos invalidos en el esquema', v_n = 0, v_n || ' objetos invalidos');
    SELECT COUNT(*) INTO v_n FROM user_triggers
    WHERE trigger_name = 'TRG_CONTACT_MESSAGES_UPDATED_AT' AND status = 'ENABLED';
    marcar('trigger de updated_at habilitado', v_n = 1);
    SELECT COUNT(*) INTO v_n FROM user_indexes WHERE table_name = 'CONTACT_MESSAGES';
    marcar('existen los 4 indices (PK + 3)', v_n = 4, 'indices encontrados: ' || v_n);
    SELECT COUNT(*) INTO v_n FROM user_constraints
    WHERE table_name = 'CONTACT_MESSAGES' AND constraint_name LIKE 'CK\_CONTACT\_MESSAGES\_%' ESCAPE '\';
    marcar('existen las 7 restricciones CHECK con nombre', v_n = 7, 'encontradas: ' || v_n);
    --
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('== Resultado: ' || v_ok || ' OK, ' || v_fallas
        || ' fallas (datos de prueba revertidos) ==');
    IF v_fallas = 0 THEN
        DBMS_OUTPUT.PUT_LINE('V1 de ms-contacto validada.');
    END IF;
END;
/
