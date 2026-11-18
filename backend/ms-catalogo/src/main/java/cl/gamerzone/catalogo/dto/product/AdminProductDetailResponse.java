package cl.gamerzone.catalogo.dto.product;

import java.util.List;
import java.util.UUID;

public record AdminProductDetailResponse(
        UUID id,
        ProductStatus status,
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
