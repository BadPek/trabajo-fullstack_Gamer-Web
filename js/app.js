// Lógica básica con JavaScript (Vanilla) para explicar en la entrega

// Esperar a que todo el HTML se cargue antes de ejecutar el código
document.addEventListener('DOMContentLoaded', () => {
    
    // 1. Obtener todas las referencias a los botones "Agregar al Carrito"
    const botonesAgregar = document.querySelectorAll('.btn-agregar');
    const contadorCarrito = document.getElementById('contador-carrito');
    
    // Variable para llevar la cuenta
    let totalItems = 0;

    // 2. Por cada botón, asignarle un evento de "click"
    botonesAgregar.forEach(boton => {
        boton.addEventListener('click', (event) => {
            
            // Subir el contador
            totalItems++;
            
            // Actualizar el numerito en el HTML (Navbar)
            contadorCarrito.textContent = totalItems;

            // Animación sencilla del botón al hacer click
            const btnOriginalText = event.target.textContent;
            event.target.textContent = "¡Agregado!";
            event.target.classList.replace('btn-warning', 'btn-success');

            // Volver el botón a la normalidad después de 1 segundo
            setTimeout(() => {
                event.target.textContent = btnOriginalText;
                event.target.classList.replace('btn-success', 'btn-warning');
            }, 1000);

        });
    });

});
