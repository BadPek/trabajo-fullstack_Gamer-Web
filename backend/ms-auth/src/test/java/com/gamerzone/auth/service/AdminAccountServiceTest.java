package com.gamerzone.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.gamerzone.auth.entity.Comuna;
import com.gamerzone.auth.entity.Region;
import com.gamerzone.auth.entity.Usuario;
import com.gamerzone.auth.exception.ApiException;
import com.gamerzone.auth.repository.ComunaRepository;
import com.gamerzone.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminAccountServiceTest {

    private static final Comuna SANTIAGO_CENTRO = new Comuna(101,
            new Region(1, "metropolitana", "Región Metropolitana de Santiago"), "Santiago Centro");

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ComunaRepository comunaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private ContactMessageCleanupClient contactMessageCleanupClient;

    private AdminAccountService service;

    @BeforeEach
    void setUp() {
        service = new AdminAccountService(usuarioRepository, comunaRepository, passwordEncoder, new RutValidator(),
                refreshTokenService, contactMessageCleanupClient,
                Clock.fixed(Instant.parse("2026-11-26T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void bootstrapCreatesAnIncompleteAdministratorOnlyOnAnEmptyDatabase() {
        when(usuarioRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("strong bootstrap password")).thenReturn("argon2-hash");
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario administrator = service.createBootstrapAdministrator(
                "  Admin@GMAIL.com ", "strong bootstrap password");

        assertEquals("admin@gmail.com", administrator.getCorreo());
        assertEquals("ADMINISTRADOR", administrator.getRol());
        assertEquals("argon2-hash", administrator.getPasswordHash());
        assertNull(administrator.getRut());
        assertNull(administrator.getComuna());
    }

    @Test
    void bootstrapRefusesToCreateAnAdministratorAfterInitialization() {
        when(usuarioRepository.count()).thenReturn(1L);

        assertThrows(IllegalStateException.class,
                () -> service.createBootstrapAdministrator("admin@gmail.com", "strong bootstrap password"));

        verify(usuarioRepository, never()).saveAndFlush(any(Usuario.class));
    }

    @Test
    void protectsTheLastActiveAdministratorFromDeactivation() {
        Usuario administrator = Usuario.bootstrapAdministrator("admin@gmail.com", "hash");
        when(usuarioRepository.findById(administrator.getId())).thenReturn(Optional.of(administrator));
        when(usuarioRepository.findActiveAdministratorsForUpdate("ADMINISTRADOR", "ACTIVE"))
                .thenReturn(List.of(administrator));

        ApiException error = assertThrows(ApiException.class,
                () -> service.changeStatus(administrator.getId(), administrator.getId(), "INACTIVE"));

        assertEquals("LAST_ADMIN_PROTECTED", error.code());
        verify(refreshTokenService, never()).revokeAllForUser(administrator.getId());
    }

    @Test
    void deactivatingAClientRevokesItsRefreshFamilies() {
        Usuario administrator = Usuario.bootstrapAdministrator("admin@gmail.com", "hash");
        Usuario client = new Usuario("190110222", "Ana", "Gamer", "ana@gmail.com", "hash",
                SANTIAGO_CENTRO, "Calle 1");
        when(usuarioRepository.findById(administrator.getId())).thenReturn(Optional.of(administrator));
        when(usuarioRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(usuarioRepository.findActiveAdministratorsForUpdate("ADMINISTRADOR", "ACTIVE"))
                .thenReturn(List.of(administrator));
        when(usuarioRepository.saveAndFlush(client)).thenReturn(client);

        var response = service.changeStatus(administrator.getId(), client.getId(), "INACTIVE");

        assertEquals("INACTIVE", response.status());
        verify(refreshTokenService).revokeAllForUser(client.getId());
    }

    @Test
    void recoveryCreatesANewBootstrapAdministratorOnlyWhenNoAdminIsActive() {
        when(usuarioRepository.findActiveAdministratorsForUpdate("ADMINISTRADOR", "ACTIVE"))
                .thenReturn(List.of());
        when(usuarioRepository.findByCorreoIgnoreCase("recovered@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("new recovery password")).thenReturn("new-argon2-hash");
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario administrator = service.recoverAdministrator("Recovered@gmail.com", "new recovery password");

        assertEquals("recovered@gmail.com", administrator.getCorreo());
        assertEquals("ADMINISTRADOR", administrator.getRol());
        assertEquals("new-argon2-hash", administrator.getPasswordHash());
        assertNull(administrator.getRut());
    }

    @Test
    void recoveryPromotesAnExistingActiveAccountAndRevokesItsSessions() {
        Usuario client = new Usuario("190110222", "Ana", "Gamer", "ana@gmail.com", "old-hash",
                SANTIAGO_CENTRO, "Calle 1");
        when(usuarioRepository.findActiveAdministratorsForUpdate("ADMINISTRADOR", "ACTIVE"))
                .thenReturn(List.of());
        when(usuarioRepository.findByCorreoIgnoreCase("ana@gmail.com")).thenReturn(Optional.of(client));
        when(passwordEncoder.encode("new recovery password")).thenReturn("new-argon2-hash");
        when(usuarioRepository.saveAndFlush(client)).thenReturn(client);

        Usuario administrator = service.recoverAdministrator("ana@gmail.com", "new recovery password");

        assertEquals("ADMINISTRADOR", administrator.getRol());
        assertEquals("new-argon2-hash", administrator.getPasswordHash());
        verify(refreshTokenService).revokeAllForUser(client.getId());
    }
}
