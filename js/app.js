// ===================================================
// LÓGICA PRINCIPAL JAVASCRIPT - GAMERZONE
// ===================================================

// Catálogo Enriquecido de Juegos con Información Estilo Steam
const obtenerJuegos = () => [
    {
        id: 1,
        titulo: 'Elden Ring',
        precio: 59990,
        categoria: 'Action RPG / Fantasía Oscura',
        imagen: 'src/img/elden_ring.jpg',
        descripcion_corta: 'Álzate, Sinluz, y déjate guiar por la gracia para esgrimir el poder del Círculo de Elden y convertirte en el Señor del Círculo en las Tierras Intermedias.',
        video: 'src/videos/elden.webm',
        galeria: [
            'src/img/elden_ring.jpg',
            'https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=800&fit=crop'
        ],
        acerca_de: `
            <p>EL NUEVO JUEGO DE ROL Y ACCIÓN DE FANTASÍA. Álzate, Sinluz, y déjate guiar por la gracia para esgrimir el poder del Círculo de Elden y convertirte en el Señor del Círculo en las Tierras Intermedias.</p>
            <p>Un vasto mundo lleno de emoción: Un vasto mundo donde los campos abiertos con una variedad de situaciones y enormes mazmorras con diseños complejos y tridimensionales se conectan fluidamente. A medida que exploras, te espera la alegría de descubrir amenazas desconocidas y abrumadoras.</p>
            <ul>
                <li><strong>Crea tu propio personaje:</strong> Además de personalizar la apariencia de tu personaje, puedes combinar libremente las armas, armaduras y magias que equipas.</li>
                <li><strong>Un drama épico nacido de un mito:</strong> Una historia de múltiples capas contada en fragmentos. Un drama épico en el que los diversos pensamientos de los personajes se cruzan en las Tierras Intermedias.</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'FromSoftware Inc.',
            editor: 'Bandai Namco Entertainment',
            fecha: '25 Feb 2022',
            modos: 'Un jugador, Cooperativo en línea, JcJ en línea',
            idiomas: 'Español (interfaz y subtítulos), Inglés (voces)'
        },
        requisitos: {
            minimos: {
                so: 'Windows 10 (64-bit)',
                procesador: 'Intel Core i5-8400 o AMD Ryzen 3 3300X',
                memoria: '12 GB de RAM',
                graficos: 'NVIDIA GeForce GTX 1060 (3 GB) o AMD Radeon RX 580 (4 GB)',
                directx: 'Versión 12',
                almacenamiento: '60 GB de espacio disponible'
            },
            recomendados: {
                so: 'Windows 10 / 11 (64-bit)',
                procesador: 'Intel Core i7-8700K o AMD Ryzen 5 3600X',
                memoria: '16 GB de RAM',
                graficos: 'NVIDIA GeForce GTX 1070 (8 GB) o AMD Radeon RX Vega 56 (8 GB)',
                directx: 'Versión 12',
                almacenamiento: '60 GB de espacio disponible (SSD recomendado)'
            }
        }
    },
    {
        id: 2,
        titulo: 'Cyberpunk 2077',
        precio: 49990,
        categoria: 'RPG / Mundo Abierto Futurista',
        imagen: 'src/img/cyberpunk2077.jpeg',
        descripcion_corta: 'Cyberpunk 2077 es un RPG de acción y aventura de mundo abierto ambientado en la megalópolis de Night City, donde te pones en la piel de un mercenario cibernético.',
        video: 'https://cdn.akamai.steamstatic.com/steam/apps/256805177/movie480_vp9.webm',
        galeria: [
            'src/img/cyberpunk2077.jpeg',
            'https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1600080972464-8e5f35f63d08?q=80&w=800&fit=crop'
        ],
        acerca_de: `
            <p>Conviértete en V, un mercenario al margen de la ley que persigue un implante único que permite alcanzar la inmortalidad. Personaliza las mejoras cibernéticas, las habilidades y el estilo de juego de tu personaje mientras exploras una inmensa ciudad donde tus decisiones dan forma a la historia.</p>
            <ul>
                <li><strong>Crea tu propio ciberdelincuente:</strong> Elige entre docenas de habilidades y armas mejorables según tu estilo de combate (sigilo, hackeo o fuerza bruta).</li>
                <li><strong>Explora la ciudad del futuro:</strong> Night City está repleta de cosas que hacer, lugares que ver y personas que conocer. Y tú decides a dónde ir, cuándo ir y cómo llegar.</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'CD PROJEKT RED',
            editor: 'CD PROJEKT RED',
            fecha: '10 Dic 2020',
            modos: 'Un jugador',
            idiomas: 'Español (voces y textos), Inglés, Francés, Alemán'
        },
        requisitos: {
            minimos: {
                so: 'Windows 10 (64-bit)',
                procesador: 'Intel Core i7-6700 o AMD Ryzen 5 1600',
                memoria: '12 GB de RAM',
                graficos: 'NVIDIA GeForce GTX 1060 (6 GB) o AMD Radeon RX 580',
                directx: 'Versión 12',
                almacenamiento: '70 GB de espacio disponible (SSD recomendado)'
            },
            recomendados: {
                so: 'Windows 10 / 11 (64-bit)',
                procesador: 'Intel Core i7-12700 o AMD Ryzen 7 7800X3D',
                memoria: '16 GB de RAM',
                graficos: 'NVIDIA GeForce RTX 2060 SUPER o AMD Radeon RX 5700 XT',
                directx: 'Versión 12',
                almacenamiento: '70 GB en SSD'
            }
        }
    },
    {
        id: 3,
        titulo: 'The Legend of Zelda: Tears of the Kingdom',
        precio: 69990,
        categoria: 'Aventura / Acción / Mundo Abierto',
        imagen: 'src/img/zelda.jpg',
        descripcion_corta: 'Una aventura épica a través de la tierra y los cielos de Hyrule te espera en la secuela directa de The Legend of Zelda: Breath of the Wild.',
        galeria: [
            'src/img/zelda.jpg',
            'https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=800&fit=crop'
        ],
        acerca_de: `
            <p>En esta secuela de The Legend of Zelda: Breath of the Wild, decidirás tu propio camino a través de los extensos paisajes de Hyrule y las misteriosas islas flotantes en los cielos infinitos.</p>
            <ul>
                <li><strong>Crea y experimenta:</strong> Aprovecha el poder de las nuevas habilidades de Link como Ultramano y Combinación para fabricar armas improvisadas y vehículos ingeniosos.</li>
                <li><strong>Explora las profundidades:</strong> Desciende a un mundo subterráneo lleno de peligros oscuros y recompensas legendarias.</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'Nintendo EPD',
            editor: 'Nintendo',
            fecha: '12 May 2023',
            modos: 'Un jugador',
            idiomas: 'Español de América, Español de España, Inglés, Japonés'
        },
        requisitos: {
            minimos: {
                so: 'Consola Nintendo Switch / OLED / Lite',
                procesador: 'NVIDIA Tegra Custom',
                memoria: '4 GB LPDDR4',
                graficos: '720p Modo Portátil / 1080p Modo TV',
                directx: 'N/A (Nintendo OS)',
                almacenamiento: '18.2 GB de espacio en memoria'
            },
            recomendados: {
                so: 'Consola Nintendo Switch (Modo TV con Switch Pro Controller)',
                procesador: 'NVIDIA Tegra Custom',
                memoria: '4 GB LPDDR4',
                graficos: '1080p 60Hz compatible',
                directx: 'N/A (Nintendo OS)',
                almacenamiento: 'Tarjeta MicroSD Clase 10 recomendada'
            }
        }
    },
    {
        id: 4,
        titulo: 'Starfield',
        precio: 69990,
        categoria: 'Sci-Fi RPG / Exploración Espacial',
        imagen: 'src/img/starfield.jpeg',
        descripcion_corta: 'Starfield es el primer universo nuevo en más de 25 años de Bethesda Game Studios. Crea el personaje que quieras y explora con una libertad inigualable.',
        video: 'https://cdn.akamai.steamstatic.com/steam/apps/256950290/movie480_vp9.webm',
        galeria: [
            'src/img/starfield.jpeg',
            'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=800&fit=crop'
        ],
        acerca_de: `
            <p>En el año 2330, la humanidad se ha aventurado más allá de nuestro sistema solar para asentarse en nuevos planetas. Te unirás a Constelación, el último grupo de exploradores espaciales en busca de artefactos raros por toda la galaxia.</p>
            <ul>
                <li><strong>Navega y pilota tu nave:</strong> Pilota y personaliza el aspecto de tu nave espacial, modifica sistemas clave como armamento y escudos, y recluta a miembros de la tripulación.</li>
                <li><strong>Más de mil planetas:</strong> Explora mundos alienígenas, extrae recursos para fabricar objetos y construye bases avanzadas.</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'Bethesda Game Studios',
            editor: 'Bethesda Softworks',
            fecha: '06 Sep 2023',
            modos: 'Un jugador',
            idiomas: 'Español (interfaz y subtítulos), Inglés (voces completas)'
        },
        requisitos: {
            minimos: {
                so: 'Windows 10 versión 21H1 (64-bit)',
                procesador: 'AMD Ryzen 5 2600X o Intel Core i7-6800K',
                memoria: '16 GB de RAM',
                graficos: 'AMD Radeon RX 5700 o NVIDIA GeForce GTX 1070 Ti',
                directx: 'Versión 12',
                almacenamiento: '125 GB de espacio disponible (SSD Obligatorio)'
            },
            recomendados: {
                so: 'Windows 10 / 11 actualizado',
                procesador: 'AMD Ryzen 5 3600X o Intel i5-10600K',
                memoria: '16 GB de RAM',
                graficos: 'AMD Radeon RX 6800 XT o NVIDIA GeForce RTX 2080',
                directx: 'Versión 12',
                almacenamiento: '125 GB en SSD NVMe'
            }
        }
    },
    {
        id: 5,
        titulo: 'Final Fantasy XVI',
        precio: 64990,
        categoria: 'Action RPG / Fantasía Oscura',
        imagen: 'src/img/final_fantasy_xvi.jpeg',
        descripcion_corta: 'Un trágico destino amenaza al reino de Valisthea. Sigue a Clive Rosfield en una historia de venganza y gigantescas batallas de Eikon.',
        galeria: [
            'src/img/final_fantasy_xvi.jpeg',
            'https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1600080972464-8e5f35f63d08?q=80&w=800&fit=crop'
        ],
        acerca_de: `
            <p>Final Fantasy XVI transporta a los jugadores al oscuro mundo de Valisthea, una tierra bendecida por la luz de los Cristales Madre, donde la paz se ve amenazada por la propagación de las tierras marchitas y el choque mortal de los Eikons.</p>
            <ul>
                <li><strong>Combates colosales:</strong> Cuando los Dominantes desatan el poder de sus Eikons, se libran batallas a escala titánica con fluidez cinematográfica.</li>
                <li><strong>Acción visceral:</strong> Diseñado por el director de combate de Devil May Cry V para ofrecer una experiencia rápida, dinámica y técnica.</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'Square Enix Creative Business Unit III',
            editor: 'Square Enix',
            fecha: '22 Jun 2023',
            modos: 'Un jugador',
            idiomas: 'Español latinoamericano (voces y textos), Inglés, Japonés'
        },
        requisitos: {
            minimos: {
                so: 'PlayStation 5 / Windows 10 (64-bit)',
                procesador: 'AMD Ryzen 5 1600 o Intel Core i5-8400',
                memoria: '16 GB de RAM',
                graficos: 'AMD Radeon RX 5700 o NVIDIA GeForce GTX 1070',
                directx: 'Versión 12',
                almacenamiento: '170 GB de espacio disponible (SSD)'
            },
            recomendados: {
                so: 'Windows 11 (64-bit)',
                procesador: 'AMD Ryzen 7 5700X o Intel Core i7-10700',
                memoria: '16 GB de RAM',
                graficos: 'AMD Radeon RX 6700 XT o NVIDIA GeForce RTX 2080',
                directx: 'Versión 12',
                almacenamiento: '170 GB en SSD ultra rápido'
            }
        }
    },
    {
        id: 6,
        titulo: 'Baldur\'s Gate 3',
        precio: 59990,
        categoria: 'CRPG / Rol Táctico D&D',
        imagen: 'src/img/baldurs_gate.jpg',
        descripcion_corta: 'Reúne a tu grupo y regresa a los Reinos Olvidados en una historia de compañerismo, traición, sacrificio y la atracción de un poder absoluto.',
        video: 'src/videos/baldurs.webm',
        galeria: [
            'src/img/baldurs_gate.jpg',
            'src/img/juegos/baldurs_gate/captura1.jpg',
            'src/img/juegos/baldurs_gate/captura2.png',
            'src/img/juegos/baldurs_gate/captura3.jpg',
            'src/img/juegos/baldurs_gate/captura4.jpg'
        ],
        acerca_de: `
            <p>Reúne a tu grupo y regresa a los Reinos Olvidados en una historia de compañerismo, traición, sacrificio, supervivencia y la atracción de un poder absoluto.</p>
            <p>Unas misteriosas aptitudes empiezan a surgir en tu interior por obra de un parásito de los azotamentes que te han implantado en el cerebro. Resístete y vuelve la oscuridad contra sí misma o abraza la corrupción y conviértete en el mal supremo.</p>
            <ul>
                <li><strong>Libertad absoluta de D&D:</strong> Elige entre 12 clases y 11 razas del Manual del jugador de D&D o crea tu propio héroe original.</li>
                <li><strong>Multijugador cooperativo de hasta 4 jugadores:</strong> Combina tus fuerzas en el combate por turnos y divide a tu grupo para cumplir misiones simultáneas.</li>
                <li><strong>Reconocimiento mundial:</strong> Ganador indiscutible del premio Juego del Año (GOTY 2023).</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'Larian Studios',
            editor: 'Larian Studios',
            fecha: '03 Ago 2023',
            modos: 'Un jugador, Cooperativo en línea, Multijugador multiplataforma',
            idiomas: 'Español de España (interfaz y subtítulos), Inglés (voces completas)'
        },
        requisitos: {
            minimos: {
                so: 'Windows 10 de 64 bits',
                procesador: 'Intel Core i5-4690 o AMD FX 8350',
                memoria: '8 GB de RAM',
                graficos: 'NVIDIA GeForce GTX 970 o AMD Radeon RX 480 (4 GB VRAM)',
                directx: 'Versión 11',
                almacenamiento: '150 GB de espacio disponible (SSD requerido)'
            },
            recomendados: {
                so: 'Windows 10 / 11 de 64 bits',
                procesador: 'Intel Core i7-8700K o AMD Ryzen 5 3600',
                memoria: '16 GB de RAM',
                graficos: 'NVIDIA GeForce RTX 2060 SUPER o AMD Radeon RX 5700 XT (8 GB VRAM)',
                directx: 'Versión 11',
                almacenamiento: '150 GB de espacio en SSD'
            }
        }
    }
];

// ===================================================
// GESTIÓN DEL CARRITO DE COMPRAS (LOCALSTORAGE)
// ===================================================

function obtenerCarrito() {
    return JSON.parse(localStorage.getItem('gz_carrito')) || [];
}

function guardarCarrito(carrito) {
    localStorage.setItem('gz_carrito', JSON.stringify(carrito));
    actualizarContadorCarrito();
}

function actualizarContadorCarrito() {
    const contador = document.getElementById('contador-carrito');
    if (contador) {
        const carrito = obtenerCarrito();
        const totalItems = carrito.reduce((acc, item) => acc + (item.cantidad || 1), 0);
        contador.textContent = totalItems;
    }
}

function agregarAlCarrito(idJuego, cantidad = 1) {
    const juegos = obtenerJuegos();
    const juego = juegos.find(j => j.id === idJuego);
    if (!juego) return;

    let carrito = obtenerCarrito();
    const itemExistente = carrito.find(item => item.id === idJuego);

    if (itemExistente) {
        itemExistente.cantidad = (itemExistente.cantidad || 1) + parseInt(cantidad);
    } else {
        carrito.push({
            id: juego.id,
            titulo: juego.titulo,
            precio: juego.precio,
            imagen: juego.imagen,
            categoria: juego.categoria,
            cantidad: parseInt(cantidad)
        });
    }

    guardarCarrito(carrito);
    alert(`¡"${juego.titulo}" se agregó al carrito! (${cantidad} unidad(es))`);
}

function eliminarDelCarrito(index) {
    let carrito = obtenerCarrito();
    carrito.splice(index, 1);
    guardarCarrito(carrito);
    renderizarCarrito();
}

function cambiarCantidadCarrito(index, cambio) {
    let carrito = obtenerCarrito();
    if (carrito[index]) {
        carrito[index].cantidad = (carrito[index].cantidad || 1) + cambio;
        if (carrito[index].cantidad <= 0) {
            carrito.splice(index, 1);
        }
        guardarCarrito(carrito);
        renderizarCarrito();
    }
}

function vaciarCarrito() {
    if (confirm('¿Estás seguro de que deseas vaciar tu carrito de compras?')) {
        localStorage.removeItem('gz_carrito');
        actualizarContadorCarrito();
        renderizarCarrito();
    }
}

// Gestión de Cupones de Descuento
function obtenerCuponActivo() {
    return sessionStorage.getItem('gz_cupon') || '';
}

function guardarCuponActivo(codigo) {
    if (codigo) sessionStorage.setItem('gz_cupon', codigo.toUpperCase());
    else sessionStorage.removeItem('gz_cupon');
}
window.obtenerCuponActivo = obtenerCuponActivo;
window.guardarCuponActivo = guardarCuponActivo;
window.quitarCupon = function() {
    guardarCuponActivo('');
    renderizarCarrito();
};

// Renderizado dinámico de la página carrito.html
function renderizarCarrito() {
    const contenedor = document.getElementById('lista-carrito');
    const subtotalEl = document.getElementById('carrito-subtotal');
    const totalEl = document.getElementById('carrito-total');
    const btnPagar = document.getElementById('btn-pagar-ahora');
    const filaDescuento = document.getElementById('fila-descuento');
    const descuentoEl = document.getElementById('carrito-descuento');
    const inputCupon = document.getElementById('input-cupon');
    const mensajeCupon = document.getElementById('mensaje-cupon');

    if (!contenedor) return;

    const carrito = obtenerCarrito();
    contenedor.innerHTML = '';

    if (carrito.length === 0) {
        contenedor.innerHTML = `
            <div class="text-center py-5 bg-white rounded-4 border p-4 shadow-sm">
                <i class="bi bi-cart-x text-purple display-1 mb-3"></i>
                <h3 class="fw-bold text-dark">Tu carrito está vacío</h3>
                <p class="text-secondary mb-4">Aún no has agregado juegos a tu pedido. ¡Revisa nuestro catálogo!</p>
                <a href="productos.html" class="btn btn-purple px-4 py-2 fw-bold">
                    <i class="bi bi-grid me-2"></i> Explorar Juegos
                </a>
            </div>
        `;
        if (subtotalEl) subtotalEl.textContent = '$0';
        if (totalEl) totalEl.textContent = '$0';
        if (filaDescuento) filaDescuento.classList.add('d-none');
        if (mensajeCupon) mensajeCupon.innerHTML = '';
        if (btnPagar) btnPagar.disabled = true;
        return;
    }

    if (btnPagar) btnPagar.disabled = false;
    let totalGeneral = 0;

    carrito.forEach((item, index) => {
        const itemCantidad = item.cantidad || 1;
        const subtotalItem = item.precio * itemCantidad;
        totalGeneral += subtotalItem;

        const card = document.createElement('div');
        card.className = 'card border-0 shadow-sm p-3 mb-3';
        card.innerHTML = `
            <div class="row align-items-center g-3">
                <div class="col-3 col-sm-2">
                    <img src="${item.imagen}" class="img-fluid rounded" alt="${item.titulo}" style="max-height: 80px; object-fit: cover;">
                </div>
                <div class="col-5 col-sm-5">
                    <h6 class="fw-bold text-dark mb-1">${item.titulo}</h6>
                    <small class="text-muted">${item.categoria || 'Videojuego'}</small>
                </div>
                <div class="col-4 col-sm-5 text-end d-flex align-items-center justify-content-end gap-3 flex-wrap">
                    <div class="btn-group btn-group-sm" role="group">
                        <button type="button" class="btn btn-outline-purple" onclick="cambiarCantidadCarrito(${index}, -1)">-</button>
                        <button type="button" class="btn btn-outline-purple fw-bold" disabled>${itemCantidad}</button>
                        <button type="button" class="btn btn-outline-purple" onclick="cambiarCantidadCarrito(${index}, 1)">+</button>
                    </div>
                    <span class="fw-bold text-purple fs-6">$${subtotalItem.toLocaleString('es-CL')}</span>
                    <button class="btn btn-link text-danger p-0 ms-2" onclick="eliminarDelCarrito(${index})" title="Eliminar producto">
                        <i class="bi bi-trash3 fs-5"></i>
                    </button>
                </div>
            </div>
        `;
        contenedor.appendChild(card);
    });

    if (subtotalEl) subtotalEl.textContent = `$${totalGeneral.toLocaleString('es-CL')}`;

    // Calcular descuento si hay cupón activo
    const cupon = obtenerCuponActivo();
    let montoDescuento = 0;
    if (cupon === 'GAMER10' || cupon === 'DUOC2026') {
        montoDescuento = Math.round(totalGeneral * 0.10);
        if (filaDescuento) {
            filaDescuento.classList.remove('d-none');
            const textoCupon = document.getElementById('texto-cupon');
            if (textoCupon) textoCupon.textContent = `${cupon} (-10%)`;
        }
        if (descuentoEl) descuentoEl.textContent = `-$${montoDescuento.toLocaleString('es-CL')}`;
        if (mensajeCupon) {
            mensajeCupon.innerHTML = `
                <div class="alert alert-success py-1 px-2 small mt-2 d-flex justify-content-between align-items-center mb-0">
                    <span><i class="bi bi-tag-fill me-1"></i> Cupón <strong>${cupon}</strong> aplicado (-10% OFF)</span>
                    <button type="button" class="btn-close btn-sm" onclick="quitarCupon()" title="Quitar cupón"></button>
                </div>
            `;
        }
        if (inputCupon) inputCupon.value = cupon;
    } else {
        if (filaDescuento) filaDescuento.classList.add('d-none');
        if (mensajeCupon) mensajeCupon.innerHTML = '';
    }

    const totalFinal = Math.max(0, totalGeneral - montoDescuento);
    if (totalEl) totalEl.textContent = `$${totalFinal.toLocaleString('es-CL')}`;
}

// Algoritmo Oficial de Validación de RUT Chileno (Módulo 11)
function validarRutChileno(rutCompleto) {
    if (!rutCompleto) return false;
    const rutLimpio = rutCompleto.replace(/[^0-9kK]/g, '').toUpperCase();
    if (rutLimpio.length < 8 || rutLimpio.length > 9) return false;

    const cuerpo = rutLimpio.slice(0, -1);
    const dv = rutLimpio.slice(-1);

    if (!/^\d+$/.test(cuerpo)) return false;

    let suma = 0;
    let multiplo = 2;
    for (let i = cuerpo.length - 1; i >= 0; i--) {
        suma += multiplo * parseInt(cuerpo.charAt(i), 10);
        multiplo = (multiplo < 7) ? multiplo + 1 : 2;
    }

    const dvEsperadoCalculado = 11 - (suma % 11);
    let dvEsperado = '';
    if (dvEsperadoCalculado === 11) dvEsperado = '0';
    else if (dvEsperadoCalculado === 10) dvEsperado = 'K';
    else dvEsperado = dvEsperadoCalculado.toString();

    return dv === dvEsperado;
}

// Configuración de autocompletado y sugerencias dinámicas de correo
function configurarAutocompletadoCorreo(inputId, datalistId) {
    const input = document.getElementById(inputId);
    const datalist = document.getElementById(datalistId);
    if (!input || !datalist) return;

    const dominios = ['duoc.cl', 'profesor.duoc.cl', 'gmail.com'];
    const actualizarDatalist = () => {
        const val = input.value.trim();
        datalist.innerHTML = '';
        if (!val) return;

        const partes = val.split('@');
        const usuario = partes[0];
        const dominioEscrito = partes.length > 1 ? partes[1].toLowerCase() : '';

        if (usuario) {
            dominios.forEach(dom => {
                if (!dominioEscrito || dom.startsWith(dominioEscrito)) {
                    const opt = document.createElement('option');
                    opt.value = `${usuario}@${dom}`;
                    datalist.appendChild(opt);
                }
            });
        }
    };

    input.addEventListener('input', actualizarDatalist);
    input.addEventListener('focus', actualizarDatalist);
}

// ===================================================
// SISTEMA DE USUARIOS Y SESIÓN (LOCALSTORAGE)
// ===================================================
function obtenerUsuarios() {
    let usuarios = JSON.parse(localStorage.getItem('gz_usuarios'));
    if (!usuarios || !Array.isArray(usuarios) || usuarios.length === 0) {
        usuarios = [
            {
                rut: '19011022-2',
                nombre: 'Estudiante',
                apellido: 'Duoc',
                correo: 'estudiante@duoc.cl',
                pass: '1234',
                region: 'metropolitana',
                comuna: 'Santiago Centro',
                direccion: 'Av. España 8'
            }
        ];
        localStorage.setItem('gz_usuarios', JSON.stringify(usuarios));
    }
    return usuarios;
}

function guardarUsuarios(usuarios) {
    localStorage.setItem('gz_usuarios', JSON.stringify(usuarios));
}

function obtenerUsuarioActivo() {
    return JSON.parse(localStorage.getItem('gz_usuario_activo'));
}

function guardarUsuarioActivo(usuario) {
    localStorage.setItem('gz_usuario_activo', JSON.stringify(usuario));
}

function cerrarSesion() {
    localStorage.removeItem('gz_usuario_activo');
    alert('Has cerrado sesión correctamente.');
    window.location.reload();
}
window.cerrarSesion = cerrarSesion;

function actualizarEstadoSesionNavbar() {
    const usuario = obtenerUsuarioActivo();
    const navCollapse = document.querySelector('.navbar-collapse');
    if (!navCollapse) return;

    const linksLogin = navCollapse.querySelectorAll('a[href="login.html"]');
    const linksRegistro = navCollapse.querySelectorAll('a[href="registro.html"]');

    if (usuario) {
        linksLogin.forEach(link => link.classList.add('d-none'));
        linksRegistro.forEach(link => link.classList.add('d-none'));

        // Evitar duplicar el botón de usuario
        if (!document.getElementById('nav-user-dropdown')) {
            const btnCarrito = navCollapse.querySelector('#btn-carrito');
            const contUser = document.createElement('div');
            contUser.id = 'nav-user-dropdown';
            contUser.className = 'dropdown d-inline-block';
            contUser.innerHTML = `
                <button class="btn btn-outline-light btn-sm dropdown-toggle fw-bold d-flex align-items-center" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="bi bi-person-circle text-warning me-1 fs-6"></i> Hola, ${usuario.nombre}
                </button>
                <ul class="dropdown-menu dropdown-menu-end shadow">
                    <li><h6 class="dropdown-header text-purple"><i class="bi bi-person-check me-1"></i> Sesión Activa</h6></li>
                    <li><span class="dropdown-item-text small text-muted"><i class="bi bi-envelope me-1"></i> ${usuario.correo}</span></li>
                    ${usuario.rut ? `<li><span class="dropdown-item-text small text-muted"><i class="bi bi-card-text me-1"></i> RUT: ${usuario.rut}</span></li>` : ''}
                    <li><hr class="dropdown-divider"></li>
                    <li><button class="dropdown-item text-danger fw-semibold" onclick="cerrarSesion()"><i class="bi bi-box-arrow-right me-1"></i> Cerrar Sesión</button></li>
                </ul>
            `;
            if (btnCarrito && btnCarrito.parentElement) {
                btnCarrito.parentElement.insertBefore(contUser, btnCarrito);
            }
        }
    }
}

// ===================================================
// INICIALIZACIÓN GLOBAL EN DOMCONTENTLOADED
// ===================================================
document.addEventListener('DOMContentLoaded', () => {

    actualizarContadorCarrito();
    renderizarCarrito();
    actualizarEstadoSesionNavbar();

    // Configurar autocompletado y sugerencias dinámicas de correo
    configurarAutocompletadoCorreo('correoReg', 'sugerencias-correo-reg');
    configurarAutocompletadoCorreo('correoLogin', 'sugerencias-correo-login');
    configurarAutocompletadoCorreo('correo', 'sugerencias-correo-contacto');

    // Evento Vaciar Carrito
    const btnVaciar = document.getElementById('btn-vaciar-carrito');
    if (btnVaciar) {
        btnVaciar.addEventListener('click', (e) => {
            e.preventDefault();
            vaciarCarrito();
        });
    }

    // Delegación de eventos para botones "Agregar al Carrito" (.btn-agregar)
    document.addEventListener('click', (e) => {
        const btn = e.target.closest('.btn-agregar');
        if (btn && !btn.hasAttribute('onclick')) {
            const card = btn.closest('[data-id]');
            if (card) {
                const id = parseInt(card.getAttribute('data-id'), 10);
                if (id) {
                    agregarAlCarrito(id);
                }
            }
        }
    });

    // Región y Comuna dependientes (Registro)
    const selectRegion = document.getElementById('selectRegion');
    const selectComuna = document.getElementById('selectComuna');
    const comunasPorRegion = {
        metropolitana: ["Santiago Centro", "Providencia", "Las Condes", "Maipú", "Puente Alto", "Ñuñoa"],
        valparaiso: ["Viña del Mar", "Valparaíso", "Quilpué", "Villa Alemana", "Concón"],
        biobio: ["Concepción", "Talcahuano", "San Pedro de la Paz", "Chiguayante", "Los Ángeles"]
    };

    if (selectRegion && selectComuna) {
        selectRegion.addEventListener('change', () => {
            const region = selectRegion.value;
            selectComuna.innerHTML = '<option value="">Selecciona una comuna</option>';
            if (region && comunasPorRegion[region]) {
                selectComuna.disabled = false;
                comunasPorRegion[region].forEach(comuna => {
                    const opt = document.createElement('option');
                    opt.value = comuna.toLowerCase().replace(/ /g, '-');
                    opt.textContent = comuna;
                    selectComuna.appendChild(opt);
                });
            } else {
                selectComuna.disabled = true;
            }
        });
    }

    // Validación Login con LocalStorage
    const formLogin = document.getElementById('formLogin');
    if (formLogin) {
        formLogin.addEventListener('submit', (e) => {
            e.preventDefault();
            let valido = true;
            const correoInput = document.getElementById('correoLogin');
            const passInput = document.getElementById('passLogin');
            const errCorreo = document.getElementById('errorCorreoL');
            const errPass = document.getElementById('errorPassL');

            const correo = correoInput ? correoInput.value.trim() : '';
            const pass = passInput ? passInput.value : '';
            const regex = /^[a-zA-Z0-9._%+-]+@(duoc\.cl|profesor\.duoc\.cl|gmail\.com)$/i;

            if (!correo) {
                if (errCorreo) errCorreo.textContent = "El correo es obligatorio.";
                valido = false;
            } else if (!regex.test(correo)) {
                if (errCorreo) errCorreo.textContent = "Solo dominios permitidos: @duoc.cl, @profesor.duoc.cl o @gmail.com.";
                valido = false;
            } else {
                if (errCorreo) errCorreo.textContent = "";
            }

            if (!pass) {
                if (errPass) errPass.textContent = "La contraseña es obligatoria.";
                valido = false;
            } else if (pass.length < 4 || pass.length > 10) {
                if (errPass) errPass.textContent = "Debe tener entre 4 y 10 caracteres.";
                valido = false;
            } else {
                if (errPass) errPass.textContent = "";
            }

            if (!valido) return;

            const usuarios = obtenerUsuarios();
            const usuarioEncontrado = usuarios.find(u => 
                u.correo.toLowerCase() === correo.toLowerCase() && u.pass === pass
            );

            if (usuarioEncontrado) {
                guardarUsuarioActivo(usuarioEncontrado);
                alert(`¡Bienvenido(a) a GamerZone, ${usuarioEncontrado.nombre}!`);
                window.location.href = "index.html";
            } else {
                if (errCorreo) errCorreo.textContent = "Correo o contraseña no coinciden con ningún usuario registrado.";
                if (errPass) errPass.textContent = "Verifica tu contraseña o crea una cuenta.";
            }
        });
    }

    // Validación Registro con LocalStorage
    const formRegistro = document.getElementById('formRegistro');
    if (formRegistro) {
        formRegistro.addEventListener('submit', (e) => {
            e.preventDefault();
            let valido = true;

            const rutInput = document.getElementById('rutReg');
            const errRut = document.getElementById('errorRut');
            const rut = rutInput ? rutInput.value.trim() : '';

            if (!rut) {
                if (errRut) errRut.textContent = "El RUT es obligatorio.";
                valido = false;
            } else if (!validarRutChileno(rut)) {
                if (errRut) errRut.textContent = "RUT inválido. Verifica el dígito verificador (Ej: 19011022-2 o 11111111-1).";
                valido = false;
            } else {
                if (errRut) errRut.textContent = "";
            }

            const nombreInput = document.getElementById('nombreReg');
            const errNombre = document.getElementById('errorNombreR');
            const nombre = nombreInput ? nombreInput.value.trim() : '';
            if (!nombre) {
                if (errNombre) errNombre.textContent = "El nombre es obligatorio.";
                valido = false;
            } else if (nombre.length > 50) {
                if (errNombre) errNombre.textContent = "Máximo 50 caracteres.";
                valido = false;
            } else {
                if (errNombre) errNombre.textContent = "";
            }

            const apellidoInput = document.getElementById('apellidoReg');
            const errApellido = document.getElementById('errorApellidoR');
            const apellido = apellidoInput ? apellidoInput.value.trim() : '';
            if (!apellido) {
                if (errApellido) errApellido.textContent = "Los apellidos son obligatorios.";
                valido = false;
            } else if (apellido.length > 100) {
                if (errApellido) errApellido.textContent = "Máximo 100 caracteres.";
                valido = false;
            } else {
                if (errApellido) errApellido.textContent = "";
            }

            const correoInput = document.getElementById('correoReg');
            const errCorreo = document.getElementById('errorCorreoR');
            const correo = correoInput ? correoInput.value.trim() : '';
            const regexCorreo = /^[a-zA-Z0-9._%+-]+@(duoc\.cl|profesor\.duoc\.cl|gmail\.com)$/i;

            if (!correo) {
                if (errCorreo) errCorreo.textContent = "El correo es obligatorio.";
                valido = false;
            } else if (correo.length > 100) {
                if (errCorreo) errCorreo.textContent = "Máximo 100 caracteres.";
                valido = false;
            } else if (!regexCorreo.test(correo)) {
                if (errCorreo) errCorreo.textContent = "Solo dominios permitidos: @duoc.cl, @profesor.duoc.cl o @gmail.com.";
                valido = false;
            } else {
                // Verificar si ya existe en LocalStorage
                const usuarios = obtenerUsuarios();
                if (usuarios.some(u => u.correo.toLowerCase() === correo.toLowerCase())) {
                    if (errCorreo) errCorreo.textContent = "Este correo ya está registrado. Inicia sesión o usa otro.";
                    valido = false;
                } else {
                    if (errCorreo) errCorreo.textContent = "";
                }
            }

            const passInput = document.getElementById('passReg');
            const errPass = document.getElementById('errorPassR');
            const pass = passInput ? passInput.value : '';
            if (!pass) {
                if (errPass) errPass.textContent = "La contraseña es obligatoria.";
                valido = false;
            } else if (pass.length < 4 || pass.length > 10) {
                if (errPass) errPass.textContent = "Debe tener entre 4 y 10 caracteres.";
                valido = false;
            } else {
                if (errPass) errPass.textContent = "";
            }

            const direccionInput = document.getElementById('direccionReg');
            const errDireccion = document.getElementById('errorDireccionR');
            const direccion = direccionInput ? direccionInput.value.trim() : '';
            if (!direccion) {
                if (errDireccion) errDireccion.textContent = "La dirección es obligatoria.";
                valido = false;
            } else if (direccion.length > 300) {
                if (errDireccion) errDireccion.textContent = "Máximo 300 caracteres.";
                valido = false;
            } else {
                if (errDireccion) errDireccion.textContent = "";
            }

            if (!valido) return;

            // Guardar usuario en LocalStorage
            const usuarios = obtenerUsuarios();
            const nuevoUsuario = {
                rut,
                nombre,
                apellido,
                correo,
                pass,
                region: selectRegion ? selectRegion.value : '',
                comuna: selectComuna ? selectComuna.value : '',
                direccion
            };

            usuarios.push(nuevoUsuario);
            guardarUsuarios(usuarios);
            guardarUsuarioActivo(nuevoUsuario);

            alert(`¡Cuenta registrada con éxito en GamerZone!\nBienvenido(a), ${nombre}. Has iniciado sesión automáticamente.`);
            window.location.href = "index.html";
        });
    }

});

