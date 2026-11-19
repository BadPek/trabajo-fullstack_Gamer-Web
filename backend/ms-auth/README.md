# ms-auth

Servicio Spring Boot para registro de clientes, autenticación BCrypt y emisión JWT RS256. Expone Swagger en
`/swagger-ui.html`, OpenID discovery en `/.well-known/openid-configuration` y las claves públicas en
`/.well-known/jwks.json`. El claim `authorities` contiene el rol (`CLIENTE`); los consumidores deben configurar
`JWT_AUTHORITIES_CLAIM=authorities` y `JWT_AUTHORITY_PREFIX=ROLE_`.

## Configuración

Requiere `DB_USERNAME`, `DB_PASSWORD`, `JWT_PRIVATE_KEY` (PEM PKCS#8 RSA) y `JWT_PUBLIC_KEY` (PEM X.509 RSA).
`DB_URL` apunta por defecto a `jdbc:oracle:thin:@//localhost:1521/ORCLPDB1`; `DB_SCHEMA` selecciona el esquema
independiente y por defecto usa el usuario Oracle. `JWT_ISSUER_URI` debe ser idéntico al issuer publicado.
Las claves deben ser pares RSA persistentes compartidos con los validadores; no se generan claves efímeras.

## Persistencia pendiente de aprobación

No existe en el repositorio el DDL aprobado de Andrés ni una definición de columnas para autenticación. `Usuario` y
`GZ_AUTH_USERS` son un mapeo de trabajo explícitamente provisional; no se crea una migración Flyway ni se afirma
que ese nombre/modelo esté aprobado. Antes de desplegar se debe reemplazar/ajustar el mapeo y agregar la migración
versionada acordada para el esquema de autenticación. Hibernate valida el esquema (`ddl-auto=validate`).

## API

- `POST /api/v1/auth/register`: crea cliente; requiere RUT válido, correo permitido (`duoc.cl`,
  `profesor.duoc.cl`, `gmail.com`) y datos de despacho.
- `POST /api/v1/auth/login`: devuelve `accessToken`, `expiresIn` y el perfil sin credenciales.
- `GET /api/v1/health`, discovery OIDC y JWKS son públicos.

El frontend actual guarda cuentas en session storage y no está conectado a este API; su integración es un cambio
separado.
