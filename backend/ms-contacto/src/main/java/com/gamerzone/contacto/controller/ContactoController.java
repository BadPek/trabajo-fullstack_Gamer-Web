package com.gamerzone.contacto.controller;

import java.net.URI;
import java.time.OffsetDateTime;

import com.gamerzone.contacto.dto.request.ActualizarEstadoRequest;
import com.gamerzone.contacto.dto.request.CrearContactoRequest;
import com.gamerzone.contacto.dto.response.ContactoResponse;
import com.gamerzone.contacto.dto.response.PageResponse;
import com.gamerzone.contacto.exception.ApiError;
import com.gamerzone.contacto.service.ContactoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Contacto", description = "Recepción y consulta administrativa de mensajes.")
public class ContactoController {

    private final ContactoService contactoService;

    public ContactoController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }

    @PostMapping("/contact-messages")
    @Operation(
            summary = "Registrar un mensaje de contacto",
            description = "Endpoint público; si hay JWT válido asocia el mensaje a la cuenta actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje registrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ContactoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ContactoResponse> create(
            @Valid @RequestBody CrearContactoRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        ContactoResponse response = contactoService.create(request, jwt == null ? null : jwt.getSubject());
        return ResponseEntity.created(URI.create("/api/v1/contact-messages/" + response.id()))
                .body(response);
    }

    @GetMapping("/admin/contact-messages")
    @Operation(
            summary = "Consultar mensajes recibidos",
            description = "Requiere ADMINISTRADOR.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación o filtros inválidos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR.")
    })
    public PageResponse<ContactoResponse> findAll(
            @Parameter(description = "Índice de página desde cero.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Cantidad de mensajes por página (máximo 100).", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Filtrar por estado del mensaje.")
            @RequestParam(required = false) @Pattern(regexp = "RECIBIDO|LEIDO|CERRADO") String status,
            @Parameter(description = "Fecha mínima ISO 8601 con zona horaria.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @Parameter(description = "Fecha máxima ISO 8601 con zona horaria.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        validateDateRange(from, to);
        return contactoService.findAll(page, size, status, from, to);
    }

    @PatchMapping("/admin/contact-messages/{id}/status")
    @Operation(
            summary = "Actualizar el estado de un mensaje",
            description = "Requiere ADMINISTRADOR.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ContactoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estado o identificador inválido."),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR."),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado.")
    })
    public ContactoResponse updateStatus(
            @Parameter(description = "Identificador UUID del mensaje.",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable
            @Pattern(regexp = "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
            String id,
            @Valid @RequestBody ActualizarEstadoRequest request) {
        return contactoService.updateStatus(id, request);
    }

    @DeleteMapping("/admin/contact-messages/{id}")
    @Operation(
            summary = "Eliminar un mensaje de contacto",
            description = "Requiere ADMINISTRADOR.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mensaje eliminado."),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR."),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado.")
    })
    public ResponseEntity<Void> delete(
            @PathVariable
            @Pattern(regexp = "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
            String id) {
        contactoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static void validateDateRange(OffsetDateTime from, OffsetDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' debe ser anterior o igual a 'to'.");
        }
    }
}
