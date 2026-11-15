package cl.gamerzone.catalogo.dto.product;

import java.util.List;

public record ProductDetailResponse(
        Long id,
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
