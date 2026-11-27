# ms-auth

Servicio Spring Boot para registro, sesiones autenticadas y emisión de JWT RS256. Expone Swagger en
`/swagger-ui.html`, OpenID discovery en `/.well-known/openid-configuration` y las claves públicas en
`/.well-known/jwks.json`. Los access JWT usan el claim `roles` y una audiencia por cada Resource Server.

## Configuración

Requiere `DB_USERNAME`, `DB_PASSWORD`, `JWT_PRIVATE_KEY` (PEM PKCS#8 RSA), `JWT_PUBLIC_KEY` (PEM X.509 RSA) y
`JWT_KEY_ID`.
`DB_URL` apunta por defecto a `jdbc:oracle:thin:@//localhost:1521/XEPDB1`; `DB_SCHEMA` selecciona el esquema
independiente y por defecto usa el usuario Oracle. `JWT_ISSUER_URI` debe ser idéntico al issuer publicado.
Las claves deben ser pares RSA persistentes de al menos 2048 bits; no se generan claves efímeras. Para una rotación
planificada, `JWT_PREVIOUS_PUBLIC_KEYS_JSON` admite un arreglo JSON con los `kid` y PEM públicos que aún verifican
tokens vigentes. El JWKS publica la clave activa y las anteriores; la aplicación nunca expone una operación web de
rotación. Mantener la pública anterior 660 segundos como mínimo (10 minutos de vida del JWT más 60 segundos de skew).
En producción, inyectar claves desde un gestor externo de secretos.
`FRONTEND_ORIGIN` agrega un origen CORS exacto además de `http://localhost:5173`; `AUTH_COOKIE_SECURE=true`
activa `Secure` para cookies en HTTPS. Los refresh tienen 7 días de inactividad y 30 días absolutos por defecto.
`CONTACT_SERVICE_URL` configura el servicio de contacto usado al anonimizar una cuenta.

En una base sin usuarios, `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD` son obligatorios. Se crea un admin
sin perfil personal; si ya hay usuarios, el bootstrap no modifica cuentas. El admin debe completar el perfil antes
de degradarlo al rol `CLIENTE`.

## Persistencia

Flyway crea el esquema con `db/migration/V1__create_auth_schema.sql` (tablas `users`, `regions`, `communes`,
`roles`, `refresh_token_families`, `refresh_tokens`, `csrf_contexts`, `csrf_tokens`) y siembra roles, regiones y
comunas con `V2__seed_auth_reference_data.sql`; `V3__allow_bootstrap_administrator.sql` permite un perfil vacío
solo para un admin. Hibernate valida el esquema (`ddl-auto=validate`).

`Usuario` se mapea a `users`: el RUT se guarda normalizado sin puntos ni guion (`190110222`), el correo en
minúsculas y la región se obtiene desde la comuna (`commune_id`). Los refresh tokens y valores CSRF se generan con
32 bytes aleatorios; Oracle almacena únicamente sus hashes SHA-256. El refresh rota por familia y una reutilización
revoca la familia completa. La limpieza programada elimina familias y contextos CSRF expirados.

## API

- `GET /api/v1/auth/csrf`: crea/reutiliza un contexto CSRF HttpOnly y devuelve el token sincronizado; las escrituras
  con cookies requieren `X-CSRF-TOKEN` y un `Origin` permitido.
- `POST /api/v1/auth/register`: crea un `CLIENTE`, verifica RUT, correo permitido, región/comuna y contraseña de
  12–128 caracteres; responde con access JWT/perfil y establece refresh cookie HttpOnly.
- `POST /api/v1/auth/login`: verifica credenciales y emite una nueva familia de refresh; cuentas inactivas o
  anonimizadas no pueden iniciar sesión.
- `POST /api/v1/auth/refresh`: rota el refresh token; la reutilización de un token revocado invalida toda su familia.
- `POST /api/v1/auth/logout`: revoca la familia y elimina las cookies refresh/CSRF.
- `GET /api/v1/auth/me`: devuelve el perfil mínimo de la sesión con JWT, sin RUT, dirección ni credenciales.
- `GET /api/v1/admin/users[?role=...&status=...]` y `GET /api/v1/admin/users/{id}`: perfiles administrativos
  paginados/detallados, sin hash de contraseña; requieren `ADMINISTRADOR`.
- `PATCH /api/v1/admin/users/{id}`: edición parcial de perfil; rol/estado tienen rutas separadas.
- `PATCH /api/v1/admin/users/{id}/status` y `/role`: activar/desactivar y cambiar rol; se protege al último
  administrador activo y se revocan sus sesiones al desactivar o cambiar rol.
- `DELETE /api/v1/admin/users/{id}`: revoca refresh, elimina mensajes asociados en `ms-contacto` y anonimiza la
  cuenta, conservando el identificador de pedidos.
- `GET /api/v1/health`, discovery OIDC y JWKS son públicos.

La baja de cuenta depende de `CONTACT_SERVICE_URL`; si el servicio de contacto no completa la limpieza, auth devuelve
`502` y no anonimiza el perfil. La recuperación de administrador no tiene endpoint web. En un entorno de operador,
definir `AUTH_ADMIN_RECOVERY_EMAIL` y `AUTH_ADMIN_RECOVERY_PASSWORD` y ejecutar:

```shell
java -jar auth.jar --spring.main.web-application-type=none --auth.admin-recovery=true
```

El CLI registra el evento de recuperación sin registrar la nueva contraseña.

El frontend actual guarda cuentas en session storage y no está conectado a este API; su integración sigue pendiente.
