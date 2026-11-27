package com.gamerzone.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.gamerzone.auth.dto.request.LoginRequest;
import com.gamerzone.auth.dto.request.RegistroRequest;
import com.gamerzone.auth.entity.Comuna;
import com.gamerzone.auth.entity.Region;
import com.gamerzone.auth.entity.Usuario;
import com.gamerzone.auth.exception.ApiException;
import com.gamerzone.auth.repository.ComunaRepository;
import com.gamerzone.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Comuna SANTIAGO_CENTRO = new Comuna(101,
            new Region(1, "metropolitana", "Región Metropolitana de Santiago"), "Santiago Centro");

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ComunaRepository comunaRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private Argon2PasswordEncoder passwordEncoder;
    private Clock clock;
    private AuthService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2);
        clock = Clock.fixed(Instant.parse("2026-11-26T12:00:00Z"), ZoneOffset.UTC);
        service = new AuthService(usuarioRepository, comunaRepository, passwordEncoder, new RutValidator(),
                jwtService, refreshTokenService, clock);
    }

    @Test
    void registrationNormalizesIdentityResolvesCommuneAndPersistsOnlyPasswordHash() {
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@gmail.com")).thenReturn(false);
        when(usuarioRepository.existsByRut("190110222")).thenReturn(false);
        when(comunaRepository.findByRegion_CodeAndNameIgnoreCase("metropolitana", "Santiago Centro"))
                .thenReturn(Optional.of(SANTIAGO_CENTRO));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenService.createSession(any())).thenReturn(
                new RefreshTokenGrant("opaque-refresh-token", clock.instant().plusSeconds(604800)));
        when(jwtService.issue(any())).thenReturn("signed-token");

        var result = service.register(new RegistroRequest("19.011.022-2", " Ana ", " Gamer ",
                "ANA@gmail.com", "clave segura de prueba", " Metropolitana ", " Santiago Centro ", "Calle 1"));

        ArgumentCaptor<Usuario> saved = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(saved.capture());
        Usuario usuario = saved.getValue();
        assertEquals("190110222", usuario.getRut());
        assertEquals("ana@gmail.com", usuario.getCorreo());
        assertEquals("Ana", usuario.getNombre());
        assertSame(SANTIAGO_CENTRO, usuario.getComuna());
        assertEquals("CLIENTE", usuario.getRol());
        assertEquals("ACTIVE", usuario.getStatus());
        assertTrue(usuario.getId().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        assertNotEquals("clave segura de prueba", usuario.getPasswordHash());
        assertTrue(passwordEncoder.matches("clave segura de prueba", usuario.getPasswordHash()));
        assertEquals("ana@gmail.com", result.response().user().correo());
        assertEquals("CLIENTE", result.response().user().rol());
        assertEquals("opaque-refresh-token", result.refreshToken());
    }

    @Test
    void duplicateEmailIsRejectedWithoutSavingAnAccount() {
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@gmail.com")).thenReturn(true);

        ApiException error = assertThrows(ApiException.class, () -> service.register(new RegistroRequest(
                "19.011.022-2", "Ana", "Gamer", "ana@gmail.com", "clave segura de prueba",
                "metropolitana", "Santiago Centro", "Calle 1")));

        assertEquals("EMAIL_ALREADY_REGISTERED", error.code());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void communeOutsideTheRegionIsRejectedWithoutSavingAnAccount() {
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@gmail.com")).thenReturn(false);
        when(usuarioRepository.existsByRut("190110222")).thenReturn(false);
        when(comunaRepository.findByRegion_CodeAndNameIgnoreCase("valparaiso", "Santiago Centro"))
                .thenReturn(Optional.empty());

        ApiException error = assertThrows(ApiException.class, () -> service.register(new RegistroRequest(
                "19.011.022-2", "Ana", "Gamer", "ana@gmail.com", "clave segura de prueba",
                "valparaiso", "Santiago Centro", "Calle 1")));

        assertEquals("INVALID_COMMUNE", error.code());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void loginUsesCaseInsensitiveEmailAndReturnsSignedToken() {
        Usuario account = new Usuario("190110222", "Ana", "Gamer", "ana@gmail.com",
                passwordEncoder.encode("clave segura de prueba"), SANTIAGO_CENTRO, "Calle 1");
        when(usuarioRepository.findByCorreoIgnoreCase("ANA@gmail.com")).thenReturn(Optional.of(account));
        when(jwtService.issue(account)).thenReturn("signed-token");
        when(refreshTokenService.createSession(account.getId())).thenReturn(
                new RefreshTokenGrant("opaque-refresh-token", clock.instant().plusSeconds(604800)));

        var response = service.authenticate(new LoginRequest("ANA@gmail.com", "clave segura de prueba"));

        assertEquals("signed-token", response.response().accessToken());
        assertEquals(account.getId(), response.response().user().id());
        assertEquals("CLIENTE", response.response().user().rol());
        assertEquals("opaque-refresh-token", response.refreshToken());
    }

    @Test
    void inactiveAccountCannotLogIn() {
        Usuario account = new Usuario("190110222", "Ana", "Gamer", "ana@gmail.com",
                passwordEncoder.encode("clave segura de prueba"), SANTIAGO_CENTRO, "Calle 1");
        ReflectionTestUtils.setField(account, "status", "INACTIVE");
        when(usuarioRepository.findByCorreoIgnoreCase("ana@gmail.com")).thenReturn(Optional.of(account));

        ApiException error = assertThrows(ApiException.class,
                () -> service.authenticate(new LoginRequest("ana@gmail.com", "clave segura de prueba")));

        assertEquals("INVALID_CREDENTIALS", error.code());
        verify(jwtService, never()).issue(any(Usuario.class));
    }
}
