-- =====================================================================
-- ms-auth · esquema propietario MS_AUTH · Oracle Database 21c
-- V1: cuentas, catálogos de región/comuna/rol, sesiones (refresh) y CSRF
--
-- Referencias: BACKEND_CONTRACT.md v0.6 (Perfil, Normalización, RUT,
-- Contraseña, JWT, CSRF, Baja/anonimización) y PLAN_ARQUITECTURA §4.
--
-- * La aplica Flyway con el usuario MS_AUTH. No ejecutarla a mano en ese
--   esquema: Flyway fallaría al encontrarlo con objetos.
-- * IDs de cuentas: UUID textual en minúsculas, CHAR(36), generado por la
--   aplicación; es el "sub" del JWT.
-- * Tokens y contextos: solo hash SHA-256 en hexadecimal, CHAR(64).
-- * Fechas en UTC. updated_at de users lo mantiene el trigger del final.
-- * Los datos de referencia (roles, regiones, comunas) van en V2.
-- * El administrador inicial NO se crea por SQL: lo crea ms-auth al arrancar
--   con BOOTSTRAP_ADMIN_EMAIL / BOOTSTRAP_ADMIN_PASSWORD.
-- =====================================================================

-- Regiones. code es el valor que envía el formulario ('metropolitana').
CREATE TABLE regions (
    region_id NUMBER(5)          NOT NULL,
    code      VARCHAR2(30 CHAR)  NOT NULL,
    name      VARCHAR2(100 CHAR) NOT NULL,
    CONSTRAINT pk_regions PRIMARY KEY (region_id),
    CONSTRAINT uq_regions_code UNIQUE (code),
    CONSTRAINT uq_regions_name UNIQUE (name)
);

-- Comunas. La región de una cuenta se obtiene por su comuna, por eso users
-- no guarda la región (evita la dependencia transitiva, 3FN).
CREATE TABLE communes (
    commune_id NUMBER(5)          NOT NULL,
    region_id  NUMBER(5)          NOT NULL,
    name       VARCHAR2(100 CHAR) NOT NULL,
    CONSTRAINT pk_communes PRIMARY KEY (commune_id),
    CONSTRAINT uq_communes_region_name UNIQUE (region_id, name),
    CONSTRAINT fk_communes_region FOREIGN KEY (region_id) REFERENCES regions (region_id)
);

-- Roles controlados (CLIENTE, ADMINISTRADOR). El visitante anónimo no es un rol.
CREATE TABLE roles (
    role_code VARCHAR2(20 CHAR) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (role_code)
);

-- Cuentas. Los datos personales son anulables SOLO para la baja/anonimización:
--   * una cuenta normal debe tener el perfil completo;
--   * una cuenta anonimizada queda INACTIVE, sin RUT, nombre, apellidos,
--     dirección, comuna ni hash, y con correo 'deleted+<id>@invalid.local'.
-- RUT normalizado: 7-8 dígitos y verificador (0-9 o K), sin puntos ni guion.
-- password_hash admite el formato codificado de Argon2id (o BCrypt).
CREATE TABLE users (
    id               CHAR(36)           NOT NULL,
    rut_normalized   VARCHAR2(9 CHAR),
    nombre           VARCHAR2(50 CHAR),
    apellidos        VARCHAR2(100 CHAR),
    email_normalized VARCHAR2(100 CHAR) NOT NULL,
    password_hash    VARCHAR2(255 CHAR),
    commune_id       NUMBER(5),
    direccion        VARCHAR2(300 CHAR),
    role_code        VARCHAR2(20 CHAR)  NOT NULL,
    status           VARCHAR2(10 CHAR)  DEFAULT 'ACTIVE' NOT NULL,
    anonymized_at    TIMESTAMP(6),
    created_at       TIMESTAMP(6)       DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    updated_at       TIMESTAMP(6)       DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email_normalized),
    CONSTRAINT uq_users_rut UNIQUE (rut_normalized),
    CONSTRAINT fk_users_commune FOREIGN KEY (commune_id) REFERENCES communes (commune_id),
    CONSTRAINT fk_users_role FOREIGN KEY (role_code) REFERENCES roles (role_code),
    CONSTRAINT ck_users_id CHECK (
        REGEXP_LIKE(id, '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', 'c')
    ),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_users_rut_formato CHECK (
        rut_normalized IS NULL OR REGEXP_LIKE(rut_normalized, '^[0-9]{7,8}[0-9K]$', 'c')
    ),
    CONSTRAINT ck_users_email CHECK (
        email_normalized = LOWER(TRIM(email_normalized))
        AND INSTR(email_normalized, '@') > 1
    ),
    CONSTRAINT ck_users_textos CHECK (
        (nombre IS NULL OR TRIM(nombre) IS NOT NULL)
        AND (apellidos IS NULL OR TRIM(apellidos) IS NOT NULL)
        AND (direccion IS NULL OR TRIM(direccion) IS NOT NULL)
    ),
    CONSTRAINT ck_users_perfil_completo CHECK (
        anonymized_at IS NOT NULL
        OR (rut_normalized IS NOT NULL AND nombre IS NOT NULL AND apellidos IS NOT NULL
            AND password_hash IS NOT NULL AND commune_id IS NOT NULL AND direccion IS NOT NULL)
    ),
    CONSTRAINT ck_users_anonimizado CHECK (
        anonymized_at IS NULL
        OR (status = 'INACTIVE' AND rut_normalized IS NULL AND nombre IS NULL AND apellidos IS NULL
            AND password_hash IS NULL AND commune_id IS NULL AND direccion IS NULL)
    )
);

-- GET /admin/users: createdAt DESC con desempate por ID; filtros rol/estado.
CREATE INDEX ix_users_created ON users (created_at DESC, id);
CREATE INDEX ix_users_role_status ON users (role_code, status);

-- Oracle no crea índices en las FK del lado hijo.
CREATE INDEX ix_users_commune ON users (commune_id);

-- Familia de sesión: una por inicio de sesión; es la dueña de la relación
-- con la cuenta. Revocar la familia invalida todos sus refresh tokens.
CREATE TABLE refresh_token_families (
    family_id           CHAR(36)     NOT NULL,
    user_id             CHAR(36)     NOT NULL,
    created_at          TIMESTAMP(6) DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    absolute_expires_at TIMESTAMP(6) NOT NULL,
    revoked_at          TIMESTAMP(6),
    CONSTRAINT pk_refresh_token_families PRIMARY KEY (family_id),
    CONSTRAINT fk_rtf_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_rtf_expiracion CHECK (absolute_expires_at > created_at)
);

CREATE INDEX ix_rtf_user ON refresh_token_families (user_id);
CREATE INDEX ix_rtf_expira ON refresh_token_families (absolute_expires_at);

-- Refresh tokens rotativos (solo hash). No repite user_id: se obtiene de la
-- familia (family_id -> user_id), sin dependencia transitiva.
CREATE TABLE refresh_tokens (
    id         CHAR(36)     NOT NULL,
    family_id  CHAR(36)     NOT NULL,
    token_hash CHAR(64)     NOT NULL,
    issued_at  TIMESTAMP(6) DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked_at TIMESTAMP(6),
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_family FOREIGN KEY (family_id)
        REFERENCES refresh_token_families (family_id) ON DELETE CASCADE,
    CONSTRAINT ck_refresh_tokens_expiracion CHECK (expires_at > issued_at)
);

CREATE INDEX ix_refresh_tokens_family ON refresh_tokens (family_id);
CREATE INDEX ix_refresh_tokens_expira ON refresh_tokens (expires_at);

-- Contexto CSRF (cookie gz_csrf_context), solo hash. Vigencia de 2 horas.
CREATE TABLE csrf_contexts (
    context_id   CHAR(36)     NOT NULL,
    context_hash CHAR(64)     NOT NULL,
    created_at   TIMESTAMP(6) DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    expires_at   TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_csrf_contexts PRIMARY KEY (context_id),
    CONSTRAINT uq_csrf_contexts_hash UNIQUE (context_hash),
    CONSTRAINT ck_csrf_contexts_expiracion CHECK (expires_at > created_at)
);

CREATE INDEX ix_csrf_contexts_expira ON csrf_contexts (expires_at);

-- Tokens CSRF de un contexto (solo hash). Se rotan sin duplicar el contexto.
CREATE TABLE csrf_tokens (
    token_id   CHAR(36)     NOT NULL,
    context_id CHAR(36)     NOT NULL,
    token_hash CHAR(64)     NOT NULL,
    created_at TIMESTAMP(6) DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked_at TIMESTAMP(6),
    CONSTRAINT pk_csrf_tokens PRIMARY KEY (token_id),
    CONSTRAINT uq_csrf_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_csrf_tokens_context FOREIGN KEY (context_id)
        REFERENCES csrf_contexts (context_id) ON DELETE CASCADE,
    CONSTRAINT ck_csrf_tokens_expiracion CHECK (expires_at > created_at)
);

CREATE INDEX ix_csrf_tokens_context ON csrf_tokens (context_id);
CREATE INDEX ix_csrf_tokens_expira ON csrf_tokens (expires_at);

-- Mantiene updated_at en cada actualización de una cuenta.
CREATE OR REPLACE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYS_EXTRACT_UTC(SYSTIMESTAMP);
END;
/
