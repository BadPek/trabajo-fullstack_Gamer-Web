package com.gamerzone.pedido.dto.integration;

/** Datos del producto que entrega ms-catalogo; {@code id} es un UUID en texto. */
public record ProductoResponse(String id, String titulo, Integer precio) {
}
