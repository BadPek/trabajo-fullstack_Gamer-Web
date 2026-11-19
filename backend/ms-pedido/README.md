# ms-pedido

Servicio Spring Boot para crear y consultar pedidos autenticados. Swagger está disponible en `/swagger-ui.html`.
`POST /api/v1/pedidos` exige el rol `CLIENTE`, una clave `Idempotency-Key` y una dirección de despacho. Los ítems y
precios nunca se aceptan del cliente: el servicio consulta el carrito y luego ms-catalogo, calcula el total en CLP,
y guarda nombre, cantidad, precio unitario, subtotal y dirección como snapshots del pedido. Repetir una clave con
el mismo usuario y dirección devuelve el pedido guardado; reutilizarla con otra dirección devuelve 409.

## Configuración

Requiere `DB_USERNAME` y `DB_PASSWORD`; `DB_URL` apunta por defecto a Oracle 21c en
`jdbc:oracle:thin:@//localhost:1521/ORCLPDB1`. `DB_SCHEMA` es independiente para el esquema de pedidos y por defecto
usa el usuario Oracle. `JWT_ISSUER_URI` debe coincidir con `ms-auth`; el claim `authorities` es prefijado con
`ROLE_`. `CARRITO_BASE_URL` y `CATALOGO_BASE_URL` permiten configurar los endpoints internos.

## Pendientes de contratos y migración

No hay DDL aprobado de Andrés para el esquema de pedidos. Las entidades `Pedido`/`PedidoItem`, tablas
`GZ_ORDERS`/`GZ_ORDER_ITEMS` y restricción de idempotencia son un mapeo de trabajo explícitamente provisional. No
se agrega SQL Flyway hasta recibir y cotejar el contrato aprobado. `ddl-auto=validate` evita que Hibernate cree o
modifique tablas y el servicio no debe desplegarse hasta que la migración acordada esté disponible.

La integración está aislada detrás de `CarritoClient` y `CatalogoClient`. Se consume el contrato visible actualmente
en este repositorio (`GET /api/v1/carrito` con `items[{productoId,cantidad}]` y
`GET /api/v1/products/{id}` con `{id,titulo,precio}`). Se debe confirmar con Daniel la versión final de ambos
contratos, el manejo de errores/timeout, moneda y reglas para cerrar o vaciar el carrito después de confirmar el
pedido. Actualmente no se vacía el carrito; tampoco se hace una reserva de inventario ni se procesa el pago.

La idempotencia se respalda en lectura previa y unicidad `(CUSTOMER_ID, IDEMPOTENCY_KEY)` en el esquema esperado.
La migración aprobada debe conservar esa unicidad para que la protección también cubra solicitudes concurrentes.
