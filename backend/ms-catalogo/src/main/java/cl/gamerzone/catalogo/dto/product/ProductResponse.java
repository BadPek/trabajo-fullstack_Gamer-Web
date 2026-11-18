package cl.gamerzone.catalogo.dto.product;

import java.util.UUID;

public record ProductResponse(
        UUID id,
        String titulo,
        Integer precio,
        String categoria,
        String imagen,
        String descripcionCorta) {
}
