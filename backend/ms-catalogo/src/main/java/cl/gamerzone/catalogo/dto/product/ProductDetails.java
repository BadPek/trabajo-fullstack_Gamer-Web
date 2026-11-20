package cl.gamerzone.catalogo.dto.product;

import java.time.LocalDate;
import java.util.List;

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

        @NotNull(message = "Los modos de juego son obligatorios.")
        @Size(min = 1, max = 20, message = "Debe incluir entre 1 y 20 modos de juego.")
        @UniqueCatalogValues(message = "Los modos de juego no pueden repetirse.")
        List<
                @NotBlank(message = "Cada modo de juego es obligatorio.")
                @Size(max = 100, message = "Cada modo de juego no puede superar los 100 caracteres.")
                String> modos,

        @NotNull(message = "Los idiomas son obligatorios.")
        @Size(min = 1, max = 20, message = "Debe incluir entre 1 y 20 idiomas.")
        @UniqueCatalogValues(message = "Los idiomas no pueden repetirse.")
        List<
                @NotBlank(message = "Cada idioma es obligatorio.")
                @Size(max = 100, message = "Cada idioma no puede superar los 100 caracteres.")
                String> idiomas) {
}
