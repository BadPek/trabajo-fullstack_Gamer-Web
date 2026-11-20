# ms-pedido

Servicio Spring Boot para crear y consultar pedidos autenticados. Swagger está disponible en `/swagger-ui.html`.
`POST /api/v1/pedidos` exige el rol `CLIENTE`, una `Idempotency-Key` UUID v4 y el cuerpo
`{ "cartVersion": 7, "priceQuoteToken": "..." }` (BACKEND_CONTRACT.md). Los ítems y precios nunca se aceptan del
cliente: el servicio consulta el carrito y luego ms-catalogo, y guarda título, precio unitario y cantidad como
snapshots. El pedido queda `REGISTRADO_SIN_PAGO`, no guarda dirección y su total se calcula desde las líneas.
Repetir una clave con la misma versión y cotización devuelve el pedido guardado; con otros datos devuelve 409.

## Configuración

Requiere `DB_USERNAME` y `DB_PASSWORD`; `DB_URL` apunta por defecto a Oracle 21c en
`jdbc:oracle:thin:@//localhost:1521/XEPDB1`. `DB_SCHEMA` es independiente para el esquema de pedidos y por defecto
usa el usuario Oracle. `JWT_ISSUER_URI` debe coincidir con `ms-auth`; el claim `authorities` es prefijado con
`ROLE_`. `CARRITO_BASE_URL` y `CATALOGO_BASE_URL` permiten configurar los endpoints internos.

## Persistencia

Flyway crea el esquema con `db/migration/V1__create_order_schema.sql`: `orders`, `order_items`,
`order_coupon_snapshots` y `order_idempotency`. Hibernate valida el esquema (`ddl-auto=validate`). `Pedido`,
`PedidoItem` y `PedidoIdempotencia` se mapean a esas tablas; la clave de idempotencia se guarda con la versión del
carrito y la huella SHA-256 de la cotización. El cupón aplicado (`order_coupon_snapshots`) aún no se usa porque
ms-carrito todavía no expone cupones.

## Pendientes de integración

La integración está aislada detrás de `CarritoClient` y `CatalogoClient`. Se consume el contrato visible actualmente
en este repositorio (`GET /api/v1/carrito` con `items[{productoId,cantidad}]` y
`GET /api/v1/products/{id}` con `{id,titulo,precio}`, donde `id` es un UUID). Se debe confirmar con Daniel la versión final de ambos
contratos, el manejo de errores/timeout, moneda y reglas para cerrar o vaciar el carrito después de confirmar el
pedido. Actualmente no se vacía el carrito; tampoco se hace una reserva de inventario ni se procesa el pago.

La idempotencia se respalda en lectura previa y en la clave primaria `(user_id, idempotency_key)` de
`order_idempotency`, que también protege solicitudes concurrentes. La cotización firmada todavía no se valida
contra ms-carrito (reserva y token HMAC pendientes de implementar en ese servicio).