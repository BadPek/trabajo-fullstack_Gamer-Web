package com.gamerzone.contacto.controller;

import com.gamerzone.contacto.dto.request.CrearContactoRequest;
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
import jakarta.validation.constraints.Positive;
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
            description = "Endpoint público. La solicitud se valida; el almacenamiento está pendiente "
                    + "de la aprobación del modelo relacional.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Solicitud inválida.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "501", description = "Persistencia pendiente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public void create(@Valid @RequestBody CrearContactoRequest request) {
        contactoService.create(request);
    }

    @GetMapping
    @Operation(
            summary = "Consultar mensajes recibidos",
            description = "Requiere ADMINISTRADOR. La consulta depende de la aprobación del modelo relacional.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR."),
            @ApiResponse(responseCode = "501", description = "Persistencia pendiente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public void findAll(
            @Parameter(description = "Índice de página desde cero.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Cantidad de mensajes por página (máximo 100).", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        contactoService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar un mensaje",
            description = "Requiere ADMINISTRADOR. La consulta depende de la aprobación del modelo relacional.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "El identificador no es válido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR."),
            @ApiResponse(responseCode = "501", description = "Persistencia pendiente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public void findById(
            @Parameter(description = "Identificador del mensaje.", example = "1")
            @PathVariable @Positive Long id) {
        contactoService.findById(id);
    }
}
