# GUÍA OFICIAL DE PRESENTACIÓN - EVALUACIÓN PARCIAL N° 1
## DESARROLLO FULLSTACK II (DSY1104) - DUOC UC
**Proyecto:** GamerZone - E-Commerce de Videojuegos y Accesorios  
**Equipo:** 
- **Kamen (BadPek):** Líder de Proyecto, Autenticación, Carrito, LocalStorage y Estilos Generales.
- **Daniel Rivas (dapanipiepel):** Catálogo de Juegos, Ficha Técnica Estilo Steam, Videos y Requisitos.
- **Sara Tigreros (isatigreros):** Páginas Institucionales (Nosotros, Noticias), Formulario de Contacto y Footer.

---

## 🎯 ESTRATEGIA GENERAL DE LA PRESENTACIÓN
* **Duración total estimada:** 10 a 12 minutos (3 a 4 minutos por integrante).
* **Orden sugerido:**
  1. **Sara:** Introducción institucional, semántica HTML5, nosotros, blog y formulario de contacto.
  2. **Daniel:** Catálogo interactivo de productos, ficha técnica estilo Steam, multimedia y requisitos de hardware.
  3. **Kamen:** Registro y Login con validaciones avanzadas (RUT Módulo 11), LocalStorage, Carrito dinámico, Modal promocional y Git colaborativo (ramas y Pull Requests).

---

## 👤 INTEGRANTE 1: SARA TIGREROS
**Foco de Evaluación según Rúbrica:** 
* `IE1.1.3`: Semántica HTML5 actual y estructura de páginas informativas.
* `IE1.1.4`: Consistencia visual con hoja de estilos externa (`css/style.css`).
* `IE1.2.2`: Validación de formulario de contacto con mensajes dinámicos en JS.

### 📋 Guión Paso a Paso:
1. **Saludo e Introducción al Proyecto (1 min):**
   > *"Buenos días profesor y compañeros. Nuestro proyecto es **GamerZone**, una tienda web moderna orientada a la venta de videojuegos y hardware. Para asegurar accesibilidad y buenas prácticas de desarrollo web moderno, implementamos una estructura estrictamente semántica basada en el estándar HTML5."*
2. **Estructura Semántica y Páginas Institucionales (1.5 min):**
   * **Qué mostrar en pantalla:** Abrir en el navegador `nosotros.html` y `blog.html`. Luego abrir en VS Code el código de `nosotros.html`.
   * **Qué explicar:**
     > *"En páginas como `nosotros.html` y `blog.html` evitamos el uso excesivo de `<div>` genéricos. Usamos etiquetas semánticas con propósito:*
     > * *`<header>` y `<nav>` para la barra de navegación accesible y responsiva.*
     > * *`<main>` para albergar el contenido central único de la página.*
     > * *`<section>` para segmentar bloques lógicos como la Misión, Visión y Valores.*
     > * *`<article>` en el blog para cada noticia independiente.*
     > * *`<footer>` informativo unificado con redes sociales (Instagram, Discord, YouTube) y enlaces de navegación.*
     > * *Todos los estilos visuales provienen de nuestra hoja externa `css/style.css`, aplicando el sistema de variables CSS (`--purple-primary`) y clases de utilidad de Bootstrap 5 para garantizar una arquitectura limpia y fácil de mantener."*
3. **Formulario de Contacto con Validaciones JS (1.5 min):**
   * **Qué mostrar en pantalla:** Abrir `contacto.html` y en VS Code `js/contacto.js`.
   * **Prueba en vivo:** 
     1. Presionar "Enviar Mensaje" con los campos vacíos -> Mostrar cómo aparecen los mensajes de error en rojo debajo de cada campo sin recargar la página (`event.preventDefault()`).
     2. Escribir un correo con dominio inválido (ej: `@hotmail.com`) -> Mostrar error de dominios autorizados.
     3. Mostrar el autocompletado y sugerencias dinámicas (`<datalist>` con `@duoc.cl`, `@profesor.duoc.cl`, `@gmail.com`).
     4. Llenar los datos correctamente y enviar -> Mostrar alerta verde de confirmación y reseteo del formulario.

---

## 👤 INTEGRANTE 2: DANIEL RIVAS
**Foco de Evaluación según Rúbrica:**
* `IE1.1.3`: Navegación fluida por hipervínculos, imágenes locales y videos embebidos/locales.
* `IE1.1.4`: Diseño responsivo de cards y componentes interactivos.
* `IE1.2.1`: Renderizado dinámico de datos mediante arreglos y manipulación del DOM en JavaScript.

### 📋 Guión Paso a Paso:
1. **Catálogo Dinámico de Productos (1 min):**
   * **Qué mostrar en pantalla:** Abrir `productos.html` y en VS Code la función `obtenerJuegos()` en `js/app.js`.
   * **Qué explicar:**
     > *"Para el catálogo de productos implementamos una arquitectura orientada a datos. En vez de crear tarjetas estáticas en el HTML, los datos de los videojuegos residen en un arreglo de objetos en JavaScript (`obtenerJuegos()`).*
     > *Al cargar la página, el script recorre el arreglo con `forEach` y genera de manera dinámica las tarjetas Bootstrap, asignando precios en pesos chilenos formateados, categorías temáticas, disponibilidad de stock y enlaces con parámetros URL hacia la ficha de detalle (`detalle.html?id=X`)."*
2. **Ficha Técnica Estilo Steam con Galería Multimedia (2 min):**
   * **Qué mostrar en pantalla:** Hacer clic en "Ver Detalle" de **Baldur's Gate 3** o **Elden Ring**.
   * **Qué explicar y probar en vivo:**
     > *"Diseñamos la página de detalle (`detalle.html`) inspirada en plataformas líderes como Steam y G2A:*
     > * * **Visualizador multimedia interactivo:** Cuenta con un botón de **TRAILER** que reproduce el video oficial (video local en formato WebM para Baldur's Gate y trailers oficiales de Steam CDN).*
     > * * **Galería de miniaturas:** Al hacer clic en las miniaturas inferiores, el visor cambia dinámicamente entre el video y las capturas de pantalla de alta resolución mediante funciones JS (`mostrarVideoPrincipal` y `cambiarFotoPrincipal`).*
     > * * **Ficha informativa:** Renderiza automáticamente desarrollador, editor, fecha de lanzamiento y modos de juego."*
3. **Requisitos del Sistema (Mínimos y Recomendados) (1 min):**
   * **Qué mostrar en pantalla:** Bajar al bloque inferior de `detalle.html`.
   * **Qué explicar:**
     > *"Cumpliendo con la rúbrica de contenido especializado de videojuegos, cada juego incluye sus especificaciones técnicas de hardware divididas en dos columnas contrastadas: Requisitos Mínimos y Recomendados (Sistema Operativo, Procesador, Memoria RAM, Tarjeta Gráfica, DirectX y Almacenamiento SSD). Todos estos datos son inyectados dinámicamente según el ID del juego seleccionado."*

---

## 👤 INTEGRANTE 3: KAMEN (LÍDER DEL PROYECTO)
**Foco de Evaluación según Rúbrica:**
* `IE1.2.2`: Validaciones avanzadas en JavaScript (Módulo 11 de RUT, dominios de correo, sugerencias).
* `IE1.2.1`: Gestión de estado con `localStorage` (Carrito de compras y Autenticación de sesiones).
* `IE1.3.2`: Flujo de trabajo colaborativo en GitHub (Ramas, Commits semánticos y Pull Requests integrados).

### 📋 Guión Paso a Paso:
1. **Modal Promocional y Experiencia de Usuario (1 min):**
   * **Qué mostrar en pantalla:** Abrir `index.html` en una pestaña nueva o en incógnito para que salte el Modal a los 800ms.
   * **Qué explicar:**
     > *"Para potenciar la retención de usuarios creamos un modal emergente temporizado con Bootstrap 5 que se activa a los 800 milisegundos de ingresar a la tienda. Este modal obsequia el cupón promocional **GAMER10** para un 10% de descuento e invita directamente al registro de cuenta."*
2. **Validación Avanzada de Formularios y LocalStorage (2 min):**
   * **Qué mostrar en pantalla:** Abrir `registro.html`, luego `login.html` y la pestaña *Application -> LocalStorage* en las DevTools de Chrome (F12).
   * **Qué explicar y probar en vivo:**
     > *"En el formulario de registro (`registro.html`) aplicamos validaciones rigurosas requeridas en la pauta:*
     > * * **Algoritmo Módulo 11 oficial para RUT Chileno:** Valida matemáticamente el dígito verificador multiplicando por la secuencia 2, 3, 4, 5, 6, 7 y manejando dígitos del 0 al 9 y 'K'. Limpia automáticamente puntos y guiones para mayor comodidad del usuario.*
     > * * **Autocompletado y Sugerencias de correo:** Conectamos un elemento `<datalist>` dinámico en JavaScript que sugiere al vuelo los dominios institucionales (`@duoc.cl`, `@profesor.duoc.cl`, `@gmail.com`).*
     > * * **Selectores dependientes:** Al elegir una Región (Metropolitana, Valparaíso, Biobío), el selector de Comunas se habilita y carga dinámicamente las comunas correspondientes.*
     > * * **Persistencia de Usuarios y Sesión:** Al pulsar 'Crear mi Cuenta', el usuario se guarda en `localStorage` bajo la clave `gz_usuarios`, validando que no existan correos duplicados. Además, se inicia sesión automáticamente, transformando los botones de la barra de navegación en un menú desplegable con el nombre del usuario y la opción de Cerrar Sesión."*
3. **Carrito de Compras con Persistencia (1.5 min):**
   * **Qué mostrar en pantalla:** Abrir `carrito.html`.
   * **Qué explicar y probar en vivo:**
     * Agregar un juego desde el detalle o catálogo.
     * Mostrar cómo el contador del navbar se actualiza en tiempo real.
     * Probar los botones de aumentar (+) y disminuir (-) cantidad, o eliminar producto con el basurero.
     * Aplicar el cupón **GAMER10** o **DUOC2026**.
     * Mostrar cómo los datos sobreviven si recargamos la página gracias a `localStorage.getItem('gz_carrito')`.
4. **Metodología y Repositorio Colaborativo Git (1.5 min):**
   * **Qué mostrar en pantalla:** Abrir en el navegador el repositorio GitHub: `https://github.com/BadPek/trabajo-fullstack_Gamer-Web`.
   * **Pestañas clave de GitHub para mostrar:**
     * **Insights / Network o Commit History:** Mostrar la rama `main` y las ramas de trabajo de cada miembro:
       * `feature/contacto` (Sara)
       * `feature/catalogo` (Daniel)
       * `feature/integracion-final` (Kamen)
     * **Pull Requests cerrados (#1, #2, #3):** Mostrar cómo cada integrante trabajó de forma aislada en su propia rama, creó su Pull Request con mensajes claros y descriptivos (`feat:`, `fix:`, `chore:`), y luego se realizaron los Merge a la rama principal `main` sin perder trabajo ni sobreescribir código.
   * **Conclusión:**
     > *"Esta estructura de ramas y Pull Requests garantizó que 3 desarrolladores pudiéramos colaborar en paralelo sin pisarnos el código, resolviendo conflictos de integración de manera profesional y manteniendo siempre la rama `main` estable y lista para producción."*

---

## 💡 PREGUNTAS FRECUENTES DEL DOCENTE (CÓMO RESPONDER CON NOTA 7.0)

### ❓ Pregunta 1: *"¿Por qué es importante usar HTML5 semántico en lugar de solo `<div>`?"*
* **Respuesta:** *"Porque las etiquetas semánticas (`<header>`, `<nav>`, `<main>`, `<section>`, `<article>`, `<footer>`) aportan significado tanto al navegador como a los motores de búsqueda (SEO) y a tecnologías de accesibilidad para personas con discapacidad visual (lectores de pantalla). Además, hace que el código sea mucho más legible y mantenible para el equipo."*

### ❓ Pregunta 2: *"¿Cómo funciona el cálculo del dígito verificador del RUT con Módulo 11?"*
* **Respuesta:** *"Se toma el cuerpo numérico del RUT de derecha a izquierda y se multiplica cada dígito por una serie numérica cíclica de 2 a 7. Se suman todos los productos, se calcula el residuo con respecto a 11 (`suma % 11`), y se le resta a 11 (`11 - resto`). Si el resultado es 11 el dígito es '0', si es 10 es 'K', y si es entre 1 y 9 corresponde al número resultante. Nuestro código en `js/app.js` implementa exactamente esta fórmula matemática."*

### ❓ Pregunta 3: *"¿Qué ventaja tiene usar LocalStorage frente a variables globales normales de JavaScript?"*
* **Respuesta:** *"Las variables en memoria se destruyen y reinician cada vez que el usuario refresca la página o navega a otra URL. `localStorage` permite persistencia en el navegador del cliente con hasta 5MB por dominio, permitiendo que el carrito de compras y la sesión del usuario permanezcan intactos incluso si se cierra el navegador."*

### ❓ Pregunta 4: *"¿Por qué utilizaron ramas (feature branches) en Git en vez de subir todo a main?"*
* **Respuesta:** *"Trabajar directamente sobre `main` en equipos genera colisiones y sobreescrituras constantes. Con ramas temáticas (`feature/catalogo`, `feature/contacto`), cada integrante desarrolla y prueba su funcionalidad de forma segura. Luego, mediante Pull Requests en GitHub, se realiza una revisión de código controlada antes de fusionarlo con `main`."*
