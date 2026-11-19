package com.gamerzone.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.gamerzone.auth.dto.request.LoginRequest;
import com.gamerzone.auth.dto.request.RegistroRequest;
import com.gamerzone.auth.entity.Usuario;
import com.gamerzone.auth.exception.ApiException;
import com.gamerzone.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private JwtService jwtService;

    private BCryptPasswordEncoder passwordEncoder;
    private AuthService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new AuthService(usuarioRepository, passwordEncoder, new RutValidator(), jwtService);
    }

    @Test
    void registrationNormalizesIdentityAndPersistsOnlyBcryptPassword() {
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@gmail.com")).thenReturn(false);
        when(usuarioRepository.existsByRut("19011022-2")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.register(new RegistroRequest("19.011.022-2", " Ana ", " Gamer ",
                "ANA@gmail.com", "clave", "metropolitana", "Santiago", "Calle 1"));

        ArgumentCaptor<Usuario> saved = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(saved.capture());
        assertEquals("19011022-2", saved.getValue().getRut());
        assertEquals("ana@gmail.com", saved.getValue().getCorreo());
        assertNotEquals("clave", saved.getValue().getPasswordHash());
        assertTrue(passwordEncoder.matches("clave", saved.getValue().getPasswordHash()));
        assertEquals("ana@gmail.com", response.correo());
    }

    @Test
    void duplicateEmailIsRejectedWithoutSavingAnAccount() {
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@gmail.com")).thenReturn(true);

        ApiException error = assertThrows(ApiException.class, () -> service.register(new RegistroRequest(
                "19.011.022-2", "Ana", "Gamer", "ana@gmail.com", "clave",
                "metropolitana", "Santiago", "Calle 1")));

        assertEquals("EMAIL_ALREADY_REGISTERED", error.code());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void loginUsesCaseInsensitiveEmailAndReturnsSignedToken() {
        Usuario account = new Usuario("19011022-2", "Ana", "Gamer", "ana@gmail.com",
                passwordEncoder.encode("clave"), "metropolitana", "Santiago", "Calle 1");
        when(usuarioRepository.findByCorreoIgnoreCase("ANA@gmail.com")).thenReturn(Optional.of(account));
        when(jwtService.issue(account)).thenReturn("signed-token");

        var response = service.authenticate(new LoginRequest("ANA@gmail.com", "clave"));

        assertEquals("signed-token", response.accessToken());
        assertEquals(account.getId(), response.usuario().id());
    }
}
