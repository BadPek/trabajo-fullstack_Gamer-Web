// JS VANILLA

const obtenerJuegos = () => [
    {
        id: 1,
        titulo: 'Elden Ring',
        precio: 59990,
        descripcion: 'Un épico juego de rol de acción en un mundo abierto creado por FromSoftware.',
        imagen: 'src/img/elden_ring.jpg',
        especificaciones: [
            'Plataforma: PS5, Xbox Series X/S, PC',
            'Género: Action RPG',
            'Desarrollador: FromSoftware',
            'Modo: Un jugador y multijugador'
        ]
    },
    {
        id: 2,
        titulo: 'Cyberpunk 2077',
        precio: 49990,
        descripcion: 'Vive una aventura futurista en Night City con gráficos impresionantes y libertad total.',
        imagen: 'src/img/cyberpunk2077.jpeg',
        especificaciones: [
            'Plataforma: PS5, Xbox Series X/S, PC',
            'Género: RPG / Acción',
            'Desarrollador: CD Projekt Red',
            'Modo: Un jugador'
        ]
    },
    {
        id: 3,
        titulo: 'The Legend of Zelda: Tears of the Kingdom',
        precio: 69990,
        descripcion: 'La continuación épica del viaje de Link con nuevas mecánicas y un mundo expansivo.',
        imagen: 'src/img/zelda.jpg',
        especificaciones: [
            'Plataforma: Nintendo Switch',
            'Género: Aventura / Acción',
            'Desarrollador: Nintendo',
            'Modo: Un jugador'
        ]
    },
    {
        id: 4,
        titulo: 'Starfield',
        precio: 69990,
        descripcion: 'Explora el espacio infinito con libertad total en este ambicioso juego espacial.',
        imagen: 'src/img/starfield.jpeg',
        especificaciones: [
            'Plataforma: Xbox Series X/S, PC',
            'Género: RPG / Acción',
            'Desarrollador: Bethesda',
            'Modo: Un jugador'
        ]
    },
    {
        id: 5,
        titulo: 'Final Fantasy XVI',
        precio: 64990,
        descripcion: 'La serie de fantasía final llega con un capítulo épico lleno de magia y combates.',
        imagen: 'src/img/final_fantasy_xvi.jpeg',
        especificaciones: [
            'Plataforma: PlayStation 5',
            'Género: RPG / Fantasía',
            'Desarrollador: Square Enix',
            'Modo: Un jugador'
        ]
    },
    {
        id: 6,
        titulo: 'Baldur\'s Gate 3',
        precio: 59990,
        descripcion: 'Una aventura de rol épica basada en D&D con decisiones que cambian el destino.',
        imagen: 'src/img/baldurs_gate.jpg',
        especificaciones: [
            'Plataforma: PS5, Xbox Series X/S, PC',
            'Género: RPG / Aventura',
            'Desarrollador: Larian Studios',
            'Modo: Un jugador y multijugador'
        ]
    }
];

document.addEventListener('DOMContentLoaded', () => {
    
    // -------------------------------------------------------------
    // LÓGICA DEL CARRITO (Botones de las Cards)
    // -------------------------------------------------------------
    const botonesAgregar = document.querySelectorAll('.btn-agregar');
    const contadorCarrito = document.getElementById('contador-carrito');
    
    let totalItems = 0;

    // Función genérica para manejar la animación del botón
    const animarBoton = (boton) => {
        totalItems++;
        contadorCarrito.textContent = totalItems;
        
        const btnOriginalText = boton.innerHTML;
        boton.innerHTML = "<i class='bi bi-check-circle'></i> ¡Agregado!";
        boton.classList.replace('btn-warning', 'btn-success');

        setTimeout(() => {
            boton.innerHTML = btnOriginalText;
            boton.classList.replace('btn-success', 'btn-warning');
        }, 1500);
    };

    botonesAgregar.forEach(boton => {
        boton.addEventListener('click', (event) => {
            animarBoton(event.target);
        });
    });


    // -------------------------------------------------------------
    // LÓGICA DEL MODAL (Detalle del Producto)
    // -------------------------------------------------------------
    const botonesDetalle = document.querySelectorAll('.btn-detalle');
    
    // Referencias a los elementos dentro de la ventana flotante (Modal)
    const modalImg = document.getElementById('modalImg');
    const modalTitulo = document.getElementById('modalTitulo');
    const modalDesc = document.getElementById('modalDesc');
    const modalPrecio = document.getElementById('modalPrecio');
    const btnAgregarModal = document.getElementById('btn-agregar-modal');

    // Cuando se hace clic en "Ver Detalle" en alguna tarjeta
    botonesDetalle.forEach(boton => {
        boton.addEventListener('click', (event) => {
            // Obtener la "tarjeta" que contiene toda la info del producto
            const tarjeta = event.target.closest('.card-gamer');
            
            // Extraer la información desde el HTML de la tarjeta
            const imagenSrc = tarjeta.querySelector('.card-img-top').src;
            const titulo = tarjeta.querySelector('.card-title').textContent;
            const descripcion = tarjeta.querySelector('.card-text').textContent;
            const precio = tarjeta.querySelector('h4').textContent;

            // Inyectar esa información dentro del Modal
            modalImg.src = imagenSrc;
            modalTitulo.textContent = titulo;
            // Expandimos un poco la descripción para que el modal no se vea vacío
            modalDesc.textContent = descripcion + " Aprovecha esta increíble oportunidad para añadir este artículo a tu colección. Cuenta con garantía y envío seguro.";
            modalPrecio.textContent = precio;
        });
    });

    // Agregar al carrito desde dentro del Modal
    btnAgregarModal.addEventListener('click', (event) => {
        animarBoton(event.currentTarget);
    });

});
