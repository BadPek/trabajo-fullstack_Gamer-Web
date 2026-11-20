package cl.gamerzone.catalogo.dto.product;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record SystemRequirements(
        @NotBlank(message = "El sistema operativo es obligatorio.")
        @Size(max = 300, message = "El sistema operativo no puede superar los 300 caracteres.")
        String so,

        @NotBlank(message = "El procesador es obligatorio.")
        @Size(max = 300, message = "El procesador no puede superar los 300 caracteres.")
        String procesador,

        @NotBlank(message = "La memoria es obligatoria.")
        @Size(max = 300, message = "La memoria no puede superar los 300 caracteres.")
        String memoria,

        @NotBlank(message = "Los gráficos son obligatorios.")
        @Size(max = 300, message = "Los gráficos no pueden superar los 300 caracteres.")
        String graficos,

        @NotBlank(message = "DirectX es obligatorio.")
        @Size(max = 300, message = "DirectX no puede superar los 300 caracteres.")
        String directx,

        @NotBlank(message = "El almacenamiento es obligatorio.")
        @Size(max = 300, message = "El almacenamiento no puede superar los 300 caracteres.")
        String almacenamiento) {
}
