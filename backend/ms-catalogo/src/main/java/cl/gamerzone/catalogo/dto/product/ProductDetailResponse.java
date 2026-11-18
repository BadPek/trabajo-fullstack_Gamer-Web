package cl.gamerzone.catalogo.dto.product;

import java.util.List;
import java.util.UUID;

public record ProductDetailResponse(
        UUID id,
        String titulo,
        Integer precio,
        String categoria,
        String imagen,
        String descripcionCorta,
        String descripcion,
        String video,
        List<String> galeria,
        ProductDetails detalles,
        ProductRequirements requisitos) {
}
