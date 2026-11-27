package com.gamerzone.auth.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamerzone.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    @Mock
    private AdminAccountService adminAccountService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private Environment environment;

    private AdminBootstrapRunner runner;

    @BeforeEach
    void setUp() {
        runner = new AdminBootstrapRunner(adminAccountService, usuarioRepository, environment,
                "admin@gmail.com", "bootstrap password", "recovery@gmail.com", "recovery password");
    }

    @Test
    void bootstrapsOnlyWhenTheDatabaseHasNoUsers() throws Exception {
        when(usuarioRepository.count()).thenReturn(0L);

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(adminAccountService).createBootstrapAdministrator("admin@gmail.com", "bootstrap password");
    }

    @Test
    void doesNotOverwriteAnExistingAdministrator() throws Exception {
        when(usuarioRepository.count()).thenReturn(1L);

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(adminAccountService, never()).createBootstrapAdministrator(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void recoveryRequiresNonWebCliMode() {
        when(environment.getProperty("spring.main.web-application-type")).thenReturn("servlet");

        assertThrows(IllegalStateException.class, () -> runner.run(
                new DefaultApplicationArguments(new String[]{"--auth.admin-recovery=true"})));

        verify(adminAccountService, never()).recoverAdministrator(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void runsRecoveryOnlyInNonWebCliMode() throws Exception {
        when(environment.getProperty("spring.main.web-application-type")).thenReturn("none");

        runner.run(new DefaultApplicationArguments(new String[]{"--auth.admin-recovery=true"}));

        verify(adminAccountService).recoverAdministrator("recovery@gmail.com", "recovery password");
    }
}
