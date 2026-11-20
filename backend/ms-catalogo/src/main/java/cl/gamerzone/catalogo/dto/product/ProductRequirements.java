
package cl.gamerzone.catalogo.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ProductRequirements(

        @NotNull(message = "Los requisitos mínimos son obligatorios.")
        @Valid
        SystemRequirements minimos,

        @NotNull(message = "Los requisitos recomendados son obligatorios.")
        @Valid
        SystemRequirements recomendados

) {
}
