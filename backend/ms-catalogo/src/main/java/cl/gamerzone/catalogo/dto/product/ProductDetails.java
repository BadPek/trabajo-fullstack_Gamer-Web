package cl.gamerzone.catalogo.dto.product;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductDetails(
        @NotBlank(message = "El desarrollador es obligatorio.")
        @Size(max = 150, message = "El desarrollador no puede superar los 150 caracteres.")
        String desarrollador,

        @NotBlank(message = "El editor es obligatorio.")
        @Size(max = 150, message = "El editor no puede superar los 150 caracteres.")
        String editor,

        @NotNull(message = "La fecha de lanzamiento es obligatoria.")
        LocalDate fechaLanzamiento,

        @NotBlank(message = "Los modos de juego son obligatorios.")
        @Size(max = 500, message = "Los modos de juego no pueden superar los 500 caracteres.")
        String modos,

        @NotBlank(message = "Los idiomas son obligatorios.")
        @Size(max = 500, message = "Los idiomas no pueden superar los 500 caracteres.")
        String idiomas) {
}
