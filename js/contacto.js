document.addEventListener('DOMContentLoaded', () => {

    const formulario = document.getElementById('formContacto');

    formulario.addEventListener('submit', (event) => {

        event.preventDefault();

        const nombre = document.getElementById('nombre').value.trim();
        const correo = document.getElementById('correo').value.trim();
        const asunto = document.getElementById('asunto').value.trim();
        const mensaje = document.getElementById('mensaje').value.trim();

        const errorNombre = document.getElementById('errorNombre');
        const errorCorreo = document.getElementById('errorCorreo');
        const errorAsunto = document.getElementById('errorAsunto');
        const errorMensaje = document.getElementById('errorMensaje');

        const mensajeExito = document.getElementById('mensajeExito');

        // Limpiar mensajes anteriores
        errorNombre.textContent = "";
        errorCorreo.textContent = "";
        errorAsunto.textContent = "";
        errorMensaje.textContent = "";

        mensajeExito.classList.add('d-none');

        let formularioValido = true;


        // Validar nombre
        if (nombre === "") {

            errorNombre.textContent = "Debes ingresar tu nombre.";
            formularioValido = false;

        }


        // Validar correo
        if (correo === "") {

            errorCorreo.textContent = "Debes ingresar tu correo.";
            formularioValido = false;

        } else if (!correo.includes("@") || !correo.includes(".")) {

            errorCorreo.textContent = "Ingresa un correo válido.";
            formularioValido = false;

        }


        // Validar asunto
        if (asunto === "") {

            errorAsunto.textContent = "Debes ingresar un asunto.";
            formularioValido = false;

        }


        // Validar mensaje
        if (mensaje === "") {

            errorMensaje.textContent = "Debes escribir un mensaje.";
            formularioValido = false;

        }


        // Si todo está correcto
        if (formularioValido) {

            mensajeExito.classList.remove('d-none');

            formulario.reset();

        }

    });

});