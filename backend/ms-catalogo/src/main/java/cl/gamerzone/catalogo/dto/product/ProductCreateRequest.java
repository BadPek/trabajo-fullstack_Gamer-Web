
package cl.gamerzone.catalogo.dto.product;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(

        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 150, message = "El título no puede superar los 150 caracteres.")
        String titulo,

        @NotNull(message = "El precio es obligatorio.")
        @Positive(message = "El precio debe ser mayor que cero.")
        Integer precio,

        @NotBlank(message = "La categoría es obligatoria.")
        @Size(max = 100, message = "La categoría no puede superar los 100 caracteres.")
        String categoria,

        @NotBlank(message = "La imagen de portada es obligatoria.")
        @MediaPath(message = "La imagen debe ser una ruta multimedia válida.")
        @Size(max = 2048, message = "La imagen no puede superar los 2048 caracteres.")
        String imagen,

        @NotBlank(message = "La descripción corta es obligatoria.")
        @Size(max = 500, message = "La descripción corta no puede superar los 500 caracteres.")
        String descripcionCorta,

        @NotBlank(message = "La descripción es obligatoria.")
        @Size(max = 10000, message = "La descripción no puede superar los 10000 caracteres.")
        String descripcion,

        @MediaPath(message = "El video debe ser una ruta multimedia válida.")
        @Size(max = 2048, message = "El video no puede superar los 2048 caracteres.")
        String video,

        @NotEmpty(message = "Debe incluir al menos una imagen en la galería.")
        @Size(max = 20, message = "La galería no puede superar las 20 imágenes.")
        List<@NotBlank(message = "Cada imagen de la galería es obligatoria.")
                @MediaPath(message = "Cada elemento de la galería debe ser una ruta multimedia válida.")
                @Size(max = 2048, message = "Cada ruta de la galería no puede superar los 2048 caracteres.")
                String> galeria,

        @NotNull(message = "Los detalles son obligatorios.")
        @Valid
        ProductDetails detalles,

        @NotNull(message = "Los requisitos son obligatorios.")
        @Valid
        ProductRequirements requisitos) {
}
