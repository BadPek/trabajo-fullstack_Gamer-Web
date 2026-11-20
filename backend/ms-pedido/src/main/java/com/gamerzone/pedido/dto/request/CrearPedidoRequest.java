package com.gamerzone.pedido.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Versión del carrito y cotización firmada que entrega GET /cart (BACKEND_CONTRACT.md). "
        + "No incluye precios ni totales: el servidor los obtiene del catálogo.")
public record CrearPedidoRequest(
        @Schema(description = "Versión del carrito que el cliente revisó.", example = "7")
        @NotNull @PositiveOrZero Long cartVersion,
        @Schema(description = "Token de cotización firmado por ms-carrito.")
        @NotBlank @Size(max = 4096) String priceQuoteToken) {
}
