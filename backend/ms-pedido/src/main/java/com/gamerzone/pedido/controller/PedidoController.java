package com.gamerzone.pedido.controller;

import java.net.URI;
import java.time.OffsetDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.gamerzone.pedido.dto.request.CrearPedidoRequest;
import com.gamerzone.pedido.dto.response.PageResponse;
import com.gamerzone.pedido.dto.response.PedidoResponse;
import com.gamerzone.pedido.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Pedidos", description = "Checkout del carrito con snapshots de producto.")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/orders")
    @Operation(summary = "Crear un pedido idempotente desde el carrito actual")
    public ResponseEntity<PedidoResponse> create(
            @Parameter(description = "UUID v4 estable para reintentar la misma solicitud.", required = true)
            @RequestHeader("Idempotency-Key") @NotBlank
            @Pattern(regexp = "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
                    message = "Idempotency-Key debe ser un UUID v4.")
            String idempotencyKey,
            @Valid @RequestBody CrearPedidoRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        PedidoResponse result = pedidoService.create(jwt.getSubject(), jwt.getTokenValue(), idempotencyKey, request);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + result.id())).body(result);
    }

    @GetMapping("/orders/me")
    @Operation(summary = "Listar los pedidos del cliente autenticado", description = "Requiere CLIENTE.")
    public PageResponse<PedidoResponse> findMine(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Índice de página desde cero.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página entre 1 y 100.", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return pedidoService.findMine(jwt.getSubject(), page, size);
    }

    @GetMapping("/admin/orders")
    @Operation(summary = "Listar pedidos para administración", description = "Requiere ADMINISTRADOR.")
    public PageResponse<PedidoResponse> findAll(
            @Parameter(description = "Índice de página desde cero.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página entre 1 y 100.", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Filtrar por estado del pedido.")
            @RequestParam(required = false)
            @Pattern(regexp = "REGISTRADO_SIN_PAGO") String status,
            @Parameter(description = "Fecha mínima ISO 8601 con zona horaria.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @Parameter(description = "Fecha máxima ISO 8601 con zona horaria.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        validateDateRange(from, to);
        return pedidoService.findAll(page, size, status, from, to);
    }

    @GetMapping("/admin/orders/{id}")
    @Operation(summary = "Consultar un pedido con sus snapshots", description = "Requiere ADMINISTRADOR.")
    public PedidoResponse findById(@PathVariable String id) {
        return pedidoService.findById(id);
    }

    private static void validateDateRange(OffsetDateTime from, OffsetDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' debe ser anterior o igual a 'to'.");
        }
    }
}
