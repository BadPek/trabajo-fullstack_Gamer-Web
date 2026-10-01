// VALIDACIÓN DE FORMULARIO DE CONTACTO (Sara + Rúbrica)
document.addEventListener('DOMContentLoaded', () => {

    const formulario = document.getElementById('formContacto');
    if (!formulario) return;

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

        // Limpiar errores previos
        errorNombre.textContent = "";
        errorCorreo.textContent = "";
        errorAsunto.textContent = "";
        errorMensaje.textContent = "";
        if (mensajeExito) mensajeExito.classList.add('d-none');

        let formularioValido = true;
        const regexDominios = /^[a-zA-Z0-9._%+-]+@(duoc\.cl|profesor\.duoc\.cl|gmail\.com)$/;

        // Validar nombre (Requerido, máx 100 según rúbrica)
        if (nombre === "") {
            errorNombre.textContent = "Debes ingresar tu nombre.";
            formularioValido = false;
        } else if (nombre.length > 100) {
            errorNombre.textContent = "El nombre no puede tener más de 100 caracteres.";
            formularioValido = false;
        }

        // Validar correo (Requerido, máx 100, dominios permitidos)
        if (correo === "") {
            errorCorreo.textContent = "Debes ingresar tu correo.";
            formularioValido = false;
        } else if (correo.length > 100) {
            errorCorreo.textContent = "El correo no puede superar los 100 caracteres.";
            formularioValido = false;
        } else if (!regexDominios.test(correo)) {
            errorCorreo.textContent = "Solo se permiten correos @duoc.cl, @profesor.duoc.cl o @gmail.com.";
            formularioValido = false;
        }

        // Validar asunto
        if (asunto === "") {
            errorAsunto.textContent = "Debes ingresar un asunto.";
            formularioValido = false;
        }

        // Validar mensaje (Requerido, máx 500 según rúbrica)
        if (mensaje === "") {
            errorMensaje.textContent = "Debes escribir un mensaje.";
            formularioValido = false;
        } else if (mensaje.length > 500) {
            errorMensaje.textContent = "El mensaje no puede superar los 500 caracteres.";
            formularioValido = false;
        }

        // Si es válido
        if (formularioValido) {
            if (mensajeExito) mensajeExito.classList.remove('d-none');
            alert("¡Mensaje de contacto enviado con éxito!");
            formulario.reset();
        }
    });

});