// Lógica básica con JavaScript (Vanilla) para explicar en la entrega

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
