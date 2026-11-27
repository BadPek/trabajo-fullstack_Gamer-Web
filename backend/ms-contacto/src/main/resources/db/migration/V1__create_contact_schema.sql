-- =====================================================================
-- ms-contacto · esquema propietario MS_CONTACTO · Oracle Database 21c
-- V1: mensajes del formulario de contacto
--
-- Referencias: BACKEND_CONTRACT.md v0.6 (Contacto, Retención de contacto,
-- Baja/anonimización) y PLAN_ARQUITECTURA_MICROSERVICIOS.md §4.
--
-- * Esta migración la aplica Flyway con el usuario MS_CONTACTO. No ejecutarla
--   a mano en ese esquema: Flyway fallaría al encontrarlo con objetos.
-- * IDs: UUID textual en minúsculas, CHAR(36), generado por la aplicación.
-- * user_id es el ID de la cuenta en ms-auth: sin FK entre esquemas.
-- * Fechas en UTC. updated_at lo mantiene el trigger del final.
-- * Nombre y correo son lo que la persona escribió al enviar (snapshot),
--   no una copia del perfil: user_id no los determina (3FN).
-- * Sin datos semilla.
-- =====================================================================

-- Restricciones de la tabla:
--   * Largos del contrato: nombre 100, correo 100, asunto 120, mensaje 500
--     (en caracteres, no bytes).
--   * Ningún texto puede quedar vacío ni solo con espacios.
--   * El correo se guarda sin espacios externos y en minúsculas; la lista de
--     dominios permitidos la valida el servicio.
--   * Estados RECIBIDO, LEIDO y CERRADO.
CREATE TABLE contact_messages (
    message_id            CHAR(36)           NOT NULL,
    user_id               CHAR(36),
    sender_name_snapshot  VARCHAR2(100 CHAR) NOT NULL,
    sender_email_snapshot VARCHAR2(100 CHAR) NOT NULL,
    subject               VARCHAR2(120 CHAR) NOT NULL,
    message_text          VARCHAR2(500 CHAR) NOT NULL,
    status                VARCHAR2(10 CHAR)  DEFAULT 'RECIBIDO' NOT NULL,
    created_at            TIMESTAMP(6)       DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    updated_at            TIMESTAMP(6)       DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    CONSTRAINT pk_contact_messages PRIMARY KEY (message_id),
    CONSTRAINT ck_contact_messages_id CHECK (
        REGEXP_LIKE(message_id, '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_contact_messages_user CHECK (
        user_id IS NULL
        OR REGEXP_LIKE(user_id, '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_contact_messages_nombre CHECK (TRIM(sender_name_snapshot) IS NOT NULL),
    CONSTRAINT ck_contact_messages_correo CHECK (
        TRIM(sender_email_snapshot) IS NOT NULL
        AND sender_email_snapshot = LOWER(TRIM(sender_email_snapshot))
        AND INSTR(sender_email_snapshot, '@') > 1
    ),
    CONSTRAINT ck_contact_messages_asunto CHECK (TRIM(subject) IS NOT NULL),
    CONSTRAINT ck_contact_messages_mensaje CHECK (TRIM(message_text) IS NOT NULL),
    CONSTRAINT ck_contact_messages_status CHECK (status IN ('RECIBIDO', 'LEIDO', 'CERRADO'))
);

-- GET /admin/contact-messages: createdAt DESC con desempate por ID ascendente.
CREATE INDEX ix_contact_messages_admin ON contact_messages (created_at DESC, message_id);

-- Mismo listado filtrado por estado (y rango from/to sobre created_at).
CREATE INDEX ix_contact_messages_status ON contact_messages (status, created_at DESC, message_id);

-- Baja/anonimización de una cuenta: borrar sus mensajes asociados.
CREATE INDEX ix_contact_messages_user ON contact_messages (user_id);

COMMENT ON TABLE contact_messages IS 'Mensajes del formulario de contacto (ms-contacto). Sin envio de correo.';
COMMENT ON COLUMN contact_messages.message_id IS 'UUID en minusculas generado por la aplicacion';
COMMENT ON COLUMN contact_messages.user_id IS 'ID de la cuenta en ms-auth si el remitente tenia sesion; sin FK entre esquemas';
COMMENT ON COLUMN contact_messages.sender_name_snapshot IS 'Nombre escrito en el formulario al enviar';
COMMENT ON COLUMN contact_messages.sender_email_snapshot IS 'Correo escrito en el formulario, normalizado a minusculas';
COMMENT ON COLUMN contact_messages.subject IS 'Asunto, maximo 120 caracteres';
COMMENT ON COLUMN contact_messages.message_text IS 'Mensaje, maximo 500 caracteres';
COMMENT ON COLUMN contact_messages.status IS 'RECIBIDO, LEIDO o CERRADO';
COMMENT ON COLUMN contact_messages.created_at IS 'Fecha de envio en UTC';
COMMENT ON COLUMN contact_messages.updated_at IS 'Ultimo cambio en UTC; lo mantiene el trigger';

-- Mantiene updated_at en cada actualización (por ejemplo, el cambio de estado).
CREATE OR REPLACE TRIGGER trg_contact_messages_updated_at
BEFORE UPDATE ON contact_messages
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYS_EXTRACT_UTC(SYSTIMESTAMP);
END;
/
