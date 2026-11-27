package com.gamerzone.auth.controller;

import java.time.Duration;

import com.gamerzone.auth.dto.response.CsrfResponse;
import com.gamerzone.auth.dto.request.LoginRequest;
import com.gamerzone.auth.dto.request.RegistroRequest;
import com.gamerzone.auth.dto.response.AuthResponse;
import com.gamerzone.auth.dto.response.UsuarioResponse;
import com.gamerzone.auth.exception.ApiError;
import com.gamerzone.auth.service.AuthSessionResult;
import com.gamerzone.auth.service.AuthService;
import com.gamerzone.auth.service.CsrfGrant;
import com.gamerzone.auth.service.CsrfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, sesiones y perfil autenticado.")
public class AuthController {

    private final AuthService authService;
    private final CsrfService csrfService;
    private final boolean secureCookies;

    public AuthController(
            AuthService authService,
            CsrfService csrfService,
            @Value("${auth.cookie.secure:false}") boolean secureCookies) {
        this.authService = authService;
        this.csrfService = csrfService;
        this.secureCookies = secureCookies;
    }

    @GetMapping("/csrf")
    @Operation(summary = "Obtener un token CSRF")
    @ApiResponse(responseCode = "200", description = "Token CSRF y contexto HttpOnly emitidos.")
    public ResponseEntity<CsrfResponse> csrf(
            @CookieValue(name = CsrfService.CONTEXT_COOKIE, required = false) String contextCookie) {
        CsrfGrant grant = csrfService.issue(contextCookie);
        ResponseCookie cookie = cookie(CsrfService.CONTEXT_COOKIE, grant.contextCookie(),
                "/api/v1", grant.contextMaxAgeSeconds());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new CsrfResponse(grant.token(), grant.tokenExpiresAt()));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar una cuenta de cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada y sesión iniciada.",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud o token CSRF inválidos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Origen o token CSRF no permitido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "El correo o RUT ya están registrados.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegistroRequest request,
            @CookieValue(name = CsrfService.CONTEXT_COOKIE, required = false) String contextCookie,
            @RequestHeader(name = CsrfService.TOKEN_HEADER, required = false) String csrfToken,
            @RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin) {
        csrfService.requireValid(contextCookie, csrfToken, origin);
        AuthSessionResult result = authService.register(request);
        csrfService.rotate(contextCookie);
        return withRefreshCookie(HttpStatus.CREATED, result);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y emitir un JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access JWT emitido y refresh cookie establecida.",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud o token CSRF inválidos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Origen o token CSRF no permitido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            @CookieValue(name = CsrfService.CONTEXT_COOKIE, required = false) String contextCookie,
            @RequestHeader(name = CsrfService.TOKEN_HEADER, required = false) String csrfToken,
            @RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin) {
        csrfService.requireValid(contextCookie, csrfToken, origin);
        AuthSessionResult result = authService.authenticate(request);
        csrfService.rotate(contextCookie);
        return withRefreshCookie(HttpStatus.OK, result);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotar la sesión y emitir un nuevo JWT de acceso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refresh rotado y access JWT emitido.",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh inválido, expirado o reutilizado.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Origen o token CSRF no permitido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = CsrfService.CONTEXT_COOKIE, required = false) String contextCookie,
            @CookieValue(name = "gz_refresh", required = false) String refreshToken,
            @RequestHeader(name = CsrfService.TOKEN_HEADER, required = false) String csrfToken,
            @RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin) {
        csrfService.requireValid(contextCookie, csrfToken, origin);
        AuthSessionResult result = authService.refresh(refreshToken);
        csrfService.rotate(contextCookie);
        return withRefreshCookie(HttpStatus.OK, result);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión y revocar la familia de refresh")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sesión revocada y cookies eliminadas."),
            @ApiResponse(responseCode = "403", description = "Origen o token CSRF no permitido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> logout(
            @CookieValue(name = CsrfService.CONTEXT_COOKIE, required = false) String contextCookie,
            @CookieValue(name = "gz_refresh", required = false) String refreshToken,
            @RequestHeader(name = CsrfService.TOKEN_HEADER, required = false) String csrfToken,
            @RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin) {
        csrfService.requireValid(contextCookie, csrfToken, origin);
        authService.logout(refreshToken);
        csrfService.invalidate(contextCookie);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("gz_refresh", "", "/api/v1/auth", 0).toString())
                .header(HttpHeaders.SET_COOKIE, cookie(CsrfService.CONTEXT_COOKIE, "", "/api/v1", 0).toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar el perfil de la sesión autenticada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil mínimo autenticado.",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente, inválido o cuenta inactiva.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.findCurrentUser(jwt.getSubject());
    }

    private ResponseEntity<AuthResponse> withRefreshCookie(HttpStatus status, AuthSessionResult result) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie(
                        "gz_refresh", result.refreshToken(), "/api/v1/auth", result.refreshMaxAgeSeconds()).toString())
                .body(result.response());
    }

    private ResponseCookie cookie(String name, String value, String path, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Lax")
                .path(path)
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }
}
