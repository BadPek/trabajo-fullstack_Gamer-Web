-- =====================================================================
-- GamerZone · creación de esquemas en Oracle 21c (solo desarrollo)
--
-- Ejecutar en SQL Developer conectado como SYSTEM al servicio XEPDB1 (F5).
-- Antes, reemplazar cada contraseña por un valor local distinto.
-- No guardar las contraseñas reales en el repositorio ni compartirlas.
--
-- Si un usuario ya existe (por ejemplo MS_CONTACTO), su CREATE USER muestra
-- ORA-01920: es normal, el script sigue con el resto.
-- MS_CATALOGO no se incluye porque ya fue creado.
-- =====================================================================

-- 1) Esquemas propietarios: uno por microservicio. Los usan Flyway y la
--    aplicación. Mismos privilegios que MS_CATALOGO, sin permisos de DBA.
CREATE USER MS_AUTH IDENTIFIED BY "REEMPLAZAR_CLAVE_MS_AUTH"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA 500M ON USERS;

GRANT CREATE SESSION, CREATE TABLE, CREATE TRIGGER TO MS_AUTH;

CREATE USER MS_CARRITO IDENTIFIED BY "REEMPLAZAR_CLAVE_MS_CARRITO"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA 500M ON USERS;

GRANT CREATE SESSION, CREATE TABLE, CREATE TRIGGER TO MS_CARRITO;

CREATE USER MS_PEDIDO IDENTIFIED BY "REEMPLAZAR_CLAVE_MS_PEDIDO"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA 500M ON USERS;

GRANT CREATE SESSION, CREATE TABLE, CREATE TRIGGER TO MS_PEDIDO;

CREATE USER MS_CONTACTO IDENTIFIED BY "REEMPLAZAR_CLAVE_MS_CONTACTO"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA 500M ON USERS;

GRANT CREATE SESSION, CREATE TABLE, CREATE TRIGGER TO MS_CONTACTO;

-- 2) Esquema DESECHABLE para probar todas las migraciones a mano antes de
--    Flyway. Se borra al terminar las pruebas (paso 3).
CREATE USER GZ_PRUEBA_BACKEND IDENTIFIED BY "REEMPLAZAR_CLAVE_PRUEBA"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA 100M ON USERS;

GRANT CREATE SESSION, CREATE TABLE, CREATE TRIGGER TO GZ_PRUEBA_BACKEND;

-- Comprobación: deben aparecer los usuarios con estado OPEN.
SELECT username, account_status, default_tablespace
FROM dba_users
WHERE username IN ('MS_CATALOGO', 'MS_AUTH', 'MS_CARRITO', 'MS_PEDIDO', 'MS_CONTACTO', 'GZ_PRUEBA_BACKEND')
ORDER BY username;

-- 3) Cuando las pruebas pasen, borrar el esquema desechable (quitar el "--"):
-- DROP USER GZ_PRUEBA_BACKEND CASCADE;
