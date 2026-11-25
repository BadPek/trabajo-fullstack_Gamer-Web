package com.gamerzone.contacto.controller;

import com.gamerzone.contacto.dto.request.CrearContactoRequest;
import com.gamerzone.contacto.dto.request.ActualizarEstadoRequest;
import com.gamerzone.contacto.dto.response.ContactoResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import java.net.URI;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/contactos")
@Tag(name = "Contacto", description = "Recepción y consulta administrativa de mensajes.")
public class ContactoController {

    private final ContactoService contactoService;

    public ContactoController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }

    @PostMapping
    @Operation(
            summary = "Registrar un mensaje de contacto",
            description = "Endpoint público.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje registrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ContactoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ContactoResponse> create(@Valid @RequestBody CrearContactoRequest request) {
        ContactoResponse response = contactoService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/contactos/" + response.id()))
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "Consultar mensajes recibidos",
            description = "Requiere ADMINISTRADOR.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR.")
    })
    public Page<ContactoResponse> findAll(
            @Parameter(description = "Índice de página desde cero.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Cantidad de mensajes por página (máximo 100).", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return contactoService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar un mensaje",
            description = "Requiere ADMINISTRADOR.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "El identificador no es válido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR."),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado.")
    })
    public ContactoResponse findById(
            @Parameter(description = "Identificador UUID del mensaje.",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable
            @Pattern(regexp = "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
            String id) {
        return contactoService.findById(id);
    }

    @PatchMapping("/{id}/estado")
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
}
