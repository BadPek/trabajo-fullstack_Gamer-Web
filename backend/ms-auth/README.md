# ms-auth

Servicio Spring Boot para registro de clientes, autenticación BCrypt y emisión JWT RS256. Expone Swagger en
`/swagger-ui.html`, OpenID discovery en `/.well-known/openid-configuration` y las claves públicas en
`/.well-known/jwks.json`. El claim `authorities` contiene el rol (`CLIENTE`); los consumidores deben configurar
`JWT_AUTHORITIES_CLAIM=authorities` y `JWT_AUTHORITY_PREFIX=ROLE_`.

## Configuración

Requiere `DB_USERNAME`, `DB_PASSWORD`, `JWT_PRIVATE_KEY` (PEM PKCS#8 RSA) y `JWT_PUBLIC_KEY` (PEM X.509 RSA).
`DB_URL` apunta por defecto a `jdbc:oracle:thin:@//localhost:1521/XEPDB1`; `DB_SCHEMA` selecciona el esquema
independiente y por defecto usa el usuario Oracle. `JWT_ISSUER_URI` debe ser idéntico al issuer publicado.
Las claves deben ser pares RSA persistentes compartidos con los validadores; no se generan claves efímeras.

## Persistencia

Flyway crea el esquema con `db/migration/V1__create_auth_schema.sql` (tablas `users`, `regions`, `communes`,
`roles`, `refresh_token_families`, `refresh_tokens`, `csrf_contexts`, `csrf_tokens`) y siembra roles, regiones y
comunas con `V2__seed_auth_reference_data.sql`. Hibernate valida el esquema (`ddl-auto=validate`).

`Usuario` se mapea a `users`: el RUT se guarda normalizado sin puntos ni guion (`190110222`), el correo en
minúsculas y la región se obtiene desde la comuna (`commune_id`). Las tablas de refresh tokens y CSRF ya existen,
pero el servicio todavía no las usa.

## API

- `POST /api/v1/auth/register`: crea cliente; requiere RUT válido, correo permitido (`duoc.cl`,
  `profesor.duoc.cl`, `gmail.com`), región por código (`metropolitana`, `valparaiso`, `biobio`), una comuna de
  esa región y dirección. Una comuna que no pertenece a la región responde `400 INVALID_COMMUNE`.
- `POST /api/v1/auth/login`: devuelve `accessToken`, `expiresIn` y el perfil sin credenciales. Las cuentas
  inactivas o anonimizadas no pueden iniciar sesión.
- `GET /api/v1/health`, discovery OIDC y JWKS son públicos.

El frontend actual guarda cuentas en session storage y no está conectado a este API; su integración es un cambio
separado.
