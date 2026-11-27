package com.gamerzone.carrito.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Código del cupón porcentual que se aplicará al carrito.")
public record AplicarCuponRequest(
        @Schema(description = "Código normalizado del cupón.", example = "GAMER10")
        @NotBlank(message = "El código del cupón es obligatorio.")
        @Size(min = 3, max = 30, message = "El código debe tener entre 3 y 30 caracteres.")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "El código solo puede contener letras y números.")
        String code) {
}
