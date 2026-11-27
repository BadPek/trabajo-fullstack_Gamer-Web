-- Bootstrap administrators are service operators and may initially have no customer profile.
ALTER TABLE users DROP CONSTRAINT ck_users_perfil_completo;

ALTER TABLE users ADD CONSTRAINT ck_users_perfil_completo CHECK (
    anonymized_at IS NOT NULL
    OR (
        role_code = 'ADMINISTRADOR'
        AND rut_normalized IS NULL
        AND nombre IS NULL
        AND apellidos IS NULL
        AND commune_id IS NULL
        AND direccion IS NULL
        AND password_hash IS NOT NULL
    )
    OR (
        rut_normalized IS NOT NULL
        AND nombre IS NOT NULL
        AND apellidos IS NOT NULL
        AND password_hash IS NOT NULL
        AND commune_id IS NOT NULL
        AND direccion IS NOT NULL
    )
);
