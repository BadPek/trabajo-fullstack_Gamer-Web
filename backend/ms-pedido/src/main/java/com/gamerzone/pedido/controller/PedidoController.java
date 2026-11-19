package com.gamerzone.pedido.controller;

import java.net.URI;
import java.util.List;

import com.gamerzone.pedido.dto.request.CrearPedidoRequest;
import com.gamerzone.pedido.dto.response.PedidoResponse;
import com.gamerzone.pedido.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Checkout del carrito con snapshots de producto.")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @Operation(summary = "Crear un pedido idempotente desde el carrito actual")
    public ResponseEntity<PedidoResponse> create(
            @Parameter(description = "Clave estable para reintentar la misma solicitud.", required = true)
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 128) String idempotencyKey,
            @Valid @RequestBody CrearPedidoRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        PedidoResponse result = pedidoService.create(jwt.getSubject(), jwt.getTokenValue(), idempotencyKey, request);
        return ResponseEntity.created(URI.create("/api/v1/pedidos/" + result.id())).body(result);
    }

    @GetMapping
    @Operation(summary = "Listar los pedidos del cliente autenticado")
    public List<PedidoResponse> findMine(@AuthenticationPrincipal Jwt jwt) {
        return pedidoService.findMine(jwt.getSubject());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un pedido propio")
    public PedidoResponse findMineById(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return pedidoService.findMineById(id, jwt.getSubject());
    }
}
