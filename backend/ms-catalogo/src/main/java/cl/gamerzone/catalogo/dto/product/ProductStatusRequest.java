package cl.gamerzone.catalogo.dto.product;

import jakarta.validation.constraints.NotNull;

public record ProductStatusRequest(
        @NotNull(message = "El estado es obligatorio.")
        ProductStatus status) {
}
