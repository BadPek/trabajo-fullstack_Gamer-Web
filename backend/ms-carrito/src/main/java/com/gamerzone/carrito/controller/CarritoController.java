package com.gamerzone.carrito.controller;

import com.gamerzone.carrito.dto.request.AgregarProductoRequest;
import com.gamerzone.carrito.dto.request.ActualizarCantidadRequest;
import com.gamerzone.carrito.dto.request.AplicarCuponRequest;
import com.gamerzone.carrito.dto.request.CrearReservaRequest;
import com.gamerzone.carrito.dto.request.ValidateQuoteRequest;
import com.gamerzone.carrito.dto.response.CarritoResponse;
import com.gamerzone.carrito.dto.response.CartReservationResponse;
import com.gamerzone.carrito.dto.response.ValidatedQuoteResponse;
import com.gamerzone.carrito.dto.response.CouponCalculationResponse;
import com.gamerzone.carrito.dto.response.CartQuoteResponse;
import com.gamerzone.carrito.dto.response.ItemCarritoResponse;
import com.gamerzone.carrito.exception.ApiError;
import com.gamerzone.carrito.service.CarritoService;
import com.gamerzone.carrito.service.CartQuoteService;
import com.gamerzone.carrito.service.CartReservationService;
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
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    private final CartQuoteService cartQuoteService;
    private final CartReservationService cartReservationService;

    public CarritoController(
            CarritoService carritoService,
            CartQuoteService cartQuoteService,
            CartReservationService cartReservationService) {
        this.carritoService = carritoService;
        this.cartQuoteService = cartQuoteService;
        this.cartReservationService = cartReservationService;
    }

    @GetMapping
    @Operation(summary = "Consultar el carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Contenido del carrito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarritoResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
    })
    public CarritoResponse findCart(@AuthenticationPrincipal Jwt jwt) {
        return carritoService.findCart(jwt.getSubject());
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar un producto al carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Producto agregado o actualizado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemCarritoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
    })
    public ResponseEntity<ItemCarritoResponse> addItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AgregarProductoRequest request) {
        ItemCarritoResponse item = carritoService.addItem(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Modificar la cantidad de un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Cantidad modificada.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ItemCarritoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Identificador o solicitud inválidos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
    })
    public ItemCarritoResponse updateItem(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Identificador opaco del elemento, según el contrato provisional.")
            @PathVariable @NotBlank
            @Pattern(regexp = "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
            String id,
            @Valid @RequestBody ActualizarCantidadRequest request) {
        return carritoService.updateItem(jwt.getSubject(), id, request);
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Eliminar un producto del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Producto eliminado."),
            @ApiResponse(responseCode = "400", description = "Identificador inválido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
    })
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Identificador opaco del elemento, según el contrato provisional.")
            @PathVariable @NotBlank
            @Pattern(regexp = "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
            String id) {
        carritoService.removeItem(jwt.getSubject(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Vaciar el carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Carrito vaciado."),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
    })
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Jwt jwt) {
        carritoService.clearCart(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/coupon")
    @Operation(summary = "Aplicar un cupón al carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Cupón aplicado y cálculo realizado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CouponCalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Código de cupón inválido."),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
            @ApiResponse(responseCode = "404", description = "Cupón inexistente o inactivo."),
            @ApiResponse(responseCode = "502", description = "Catálogo no disponible.")
    })
    public CouponCalculationResponse applyCoupon(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AplicarCuponRequest request) {
        return carritoService.applyCoupon(jwt.getSubject(), request);
    }

    @DeleteMapping("/coupon")
    @Operation(summary = "Quitar el cupón del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cupón quitado."),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE.")
    })
    public ResponseEntity<Void> removeCoupon(@AuthenticationPrincipal Jwt jwt) {
        carritoService.removeCoupon(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/quotes")
    @Operation(summary = "Crear una cotización firmada del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Cotización creada con una vigencia de 10 minutos.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartQuoteResponse.class))),
            @ApiResponse(responseCode = "400", description = "El carrito está vacío."),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
            @ApiResponse(responseCode = "404", description = "No existe un carrito activo."),
            @ApiResponse(responseCode = "502", description = "Catálogo no disponible.")
    })
    public CartQuoteResponse createQuote(@AuthenticationPrincipal Jwt jwt) {
        return cartQuoteService.create(jwt.getSubject());
    }

    @PostMapping("/quotes/validate")
    @Operation(summary = "Validar internamente una cotización")
    public ValidatedQuoteResponse validateQuote(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ValidateQuoteRequest request) {
        return cartQuoteService.validate(jwt.getSubject(), request);
    }

    @PostMapping("/quotes/consume")
    @Operation(summary = "Consumir una cotización después de guardar el pedido")
    public ResponseEntity<Void> consumeQuote(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ValidateQuoteRequest request) {
        cartQuoteService.consume(jwt.getSubject(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reservations")
    @Operation(summary = "Crear una reserva temporal del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Reserva creada por cinco minutos.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida."),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere el rol CLIENTE."),
            @ApiResponse(responseCode = "404", description = "No existe un carrito activo."),
            @ApiResponse(responseCode = "409", description = "Versión incorrecta o reserva activa.")
    })
    public ResponseEntity<CartReservationResponse> createReservation(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CrearReservaRequest request) {
        CartReservationResponse response = cartReservationService.create(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/reservations/{id}/complete")
    @Operation(summary = "Confirmar una reserva activa")
    public CartReservationResponse completeReservation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @NotBlank String id) {
        return cartReservationService.complete(jwt.getSubject(), id);
    }

    @DeleteMapping("/reservations/{id}")
    @Operation(summary = "Liberar una reserva activa")
    public ResponseEntity<Void> releaseReservation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @NotBlank String id) {
        cartReservationService.release(jwt.getSubject(), id);
        return ResponseEntity.noContent().build();
    }
}
