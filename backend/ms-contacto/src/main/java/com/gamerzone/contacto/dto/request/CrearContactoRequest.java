package com.gamerzone.contacto.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos entregados por una persona mediante el formulario de contacto.")
public record CrearContactoRequest(
        @Schema(description = "Nombre de quien envía el mensaje.", example = "Sara Morales")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
        String nombre,

        @Schema(description = "Correo de contacto.", example = "usuario@gmail.com")
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo debe tener un formato válido.")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
        String correo,

        @Schema(description = "Asunto del mensaje.", example = "Consulta sobre un pedido")
        @NotBlank(message = "El asunto es obligatorio.")
        String asunto,

        @Schema(description = "Contenido del mensaje.")
        @NotBlank(message = "El mensaje es obligatorio.")
        @Size(max = 500, message = "El mensaje no puede superar los 500 caracteres.")
        String mensaje) {
}
