package cl.gamerzone.catalogo.dto.product;

import java.util.UUID;

public record AdminProductResponse(
        UUID id,
        ProductStatus status,
        String titulo,
        Integer precio,
        String categoria,
        String imagen,
        String descripcionCorta) {
}
