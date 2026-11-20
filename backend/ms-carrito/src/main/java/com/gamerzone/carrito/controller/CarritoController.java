package com.gamerzone.carrito.controller;

import com.gamerzone.carrito.dto.request.AgregarProductoRequest;
import com.gamerzone.carrito.dto.request.ActualizarCantidadRequest;
import com.gamerzone.carrito.dto.response.CarritoResponse;
import com.gamerzone.carrito.dto.response.ItemCarritoResponse;
import com.gamerzone.carrito.exception.ApiError;
import com.gamerzone.carrito.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/carrito")
@Tag(name = "Carrito", description = "Contratos provisionales para administrar el carrito del cliente.")
@SecurityRequirement(name = "bearerAuth")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    @Operation(summary = "Consultar el carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Contrato previsto; disponible al integrar el modelo relacional.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarritoResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
            @ApiResponse(responseCode = "501", description = "Operación pendiente del modelo relacional.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public CarritoResponse findCart() {
        return carritoService.findCart();
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar un producto al carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Contrato previsto; disponible al integrar el modelo relacional.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemCarritoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
            @ApiResponse(responseCode = "501", description = "Operación pendiente del modelo relacional.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ItemCarritoResponse> addItem(@Valid @RequestBody AgregarProductoRequest request) {
        ItemCarritoResponse item = carritoService.addItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Modificar la cantidad de un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Contrato previsto; disponible al integrar el modelo relacional.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemCarritoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Identificador o solicitud inválidos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
            @ApiResponse(responseCode = "501", description = "Operación pendiente del modelo relacional.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ItemCarritoResponse updateItem(
            @Parameter(description = "Identificador opaco del elemento, según el contrato provisional.")
            @PathVariable @NotBlank String id,
            @Valid @RequestBody ActualizarCantidadRequest request) {
        return carritoService.updateItem(id, request);
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Eliminar un producto del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Contrato previsto; disponible al integrar el modelo relacional."),
            @ApiResponse(responseCode = "400", description = "Identificador inválido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
            @ApiResponse(responseCode = "501", description = "Operación pendiente del modelo relacional.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> removeItem(
            @Parameter(description = "Identificador opaco del elemento, según el contrato provisional.")
            @PathVariable @NotBlank String id) {
        carritoService.removeItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Vaciar el carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Contrato previsto; disponible al integrar el modelo relacional."),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
            @ApiResponse(responseCode = "501", description = "Operación pendiente del modelo relacional.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> clearCart() {
        carritoService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
