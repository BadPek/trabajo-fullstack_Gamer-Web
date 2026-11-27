package com.gamerzone.auth.controller;

import com.gamerzone.auth.dto.request.ActualizarUsuarioRequest;
import com.gamerzone.auth.dto.request.CambiarEstadoUsuarioRequest;
import com.gamerzone.auth.dto.request.CambiarRolRequest;
import com.gamerzone.auth.dto.response.AdminUsuarioResponse;
import com.gamerzone.auth.dto.response.PageResponse;
import com.gamerzone.auth.exception.ApiError;
import com.gamerzone.auth.service.AdminAccountService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Administración de usuarios", description = "Gestión segura de cuentas.")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private static final String UUID_PATTERN =
            "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

    private final AdminAccountService adminAccountService;

    public AdminUserController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @GetMapping
    @Operation(summary = "Listar cuentas", description = "Ordena por creación descendente e ID ascendente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de perfiles sin credenciales."),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido."),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR.")
    })
    public PageResponse<AdminUsuarioResponse> findAll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Pattern(regexp = "CLIENTE|ADMINISTRADOR") String role,
            @RequestParam(required = false) @Pattern(regexp = "ACTIVE|INACTIVE") String status) {
        return adminAccountService.findAll(jwt.getSubject(), page, size, role, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil administrativo sin secretos.",
                    content = @Content(schema = @Schema(implementation = AdminUsuarioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada."),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR.")
    })
    public AdminUsuarioResponse findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Pattern(regexp = UUID_PATTERN) String id) {
        return adminAccountService.findById(jwt.getSubject(), id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Editar el perfil de una cuenta",
            description = "Los campos omitidos conservan su valor. Para completar el admin bootstrap, "
                    + "se deben enviar todos los campos de perfil.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado.",
                    content = @Content(schema = @Schema(implementation = AdminUsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Perfil o comuna inválidos."),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada."),
            @ApiResponse(responseCode = "409", description = "RUT o correo ya registrados.")
    })
    public AdminUsuarioResponse updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Pattern(regexp = UUID_PATTERN) String id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        return adminAccountService.updateProfile(jwt.getSubject(), id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activar o desactivar una cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado."),
            @ApiResponse(responseCode = "400", description = "Estado inválido."),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada."),
            @ApiResponse(responseCode = "409", description = "No se puede desactivar al último administrador.")
    })
    public AdminUsuarioResponse changeStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Pattern(regexp = UUID_PATTERN) String id,
            @Valid @RequestBody CambiarEstadoUsuarioRequest request) {
        return adminAccountService.changeStatus(jwt.getSubject(), id, request.status());
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Cambiar el rol de una cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado."),
            @ApiResponse(responseCode = "400", description = "Rol inválido o perfil incompleto."),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada."),
            @ApiResponse(responseCode = "409", description = "No se puede degradar al último administrador.")
    })
    public AdminUsuarioResponse changeRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Pattern(regexp = UUID_PATTERN) String id,
            @Valid @RequestBody CambiarRolRequest request) {
        return adminAccountService.changeRole(jwt.getSubject(), id, request.rol());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Anonimizar una cuenta y revocar sus sesiones")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta anonimizada."),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada."),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar al último administrador."),
            @ApiResponse(responseCode = "502", description = "No fue posible eliminar mensajes de contacto asociados.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Pattern(regexp = UUID_PATTERN) String id) {
        adminAccountService.delete(jwt.getSubject(), id, jwt.getTokenValue());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
