package cl.gamerzone.catalogo.dto.product;

public record ProductResponse(
        Long id,
        String titulo,
        Integer precio,
        String categoria,
        String imagen,
        String descripcionCorta) {
}
