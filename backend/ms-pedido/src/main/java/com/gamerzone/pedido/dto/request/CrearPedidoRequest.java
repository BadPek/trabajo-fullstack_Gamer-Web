package com.gamerzone.pedido.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos de entrega que se congelan como snapshot en el pedido.")
public record CrearPedidoRequest(
        @NotBlank @Size(max = 300) String direccionDespacho) {
}
