package com.gamerzone.carrito.integration;

public record CatalogoProductResponse(
        String id,
        String titulo,
        Integer precio,
        String imagen) {

    public CatalogoProductResponse(String id, String titulo, Integer precio) {
        this(id, titulo, precio, null);
    }
}
