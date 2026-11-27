package com.gamerzone.auth.service;

import com.gamerzone.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AdminAccountService adminAccountService;
    private final UsuarioRepository userRepository;
    private final Environment environment;
    private final String bootstrapEmail;
    private final String bootstrapPassword;
    private final String recoveryEmail;
    private final String recoveryPassword;

    public AdminBootstrapRunner(
            AdminAccountService adminAccountService,
            UsuarioRepository userRepository,
            Environment environment,
            @Value("${auth.bootstrap.admin.email:}") String bootstrapEmail,
            @Value("${auth.bootstrap.admin.password:}") String bootstrapPassword,
            @Value("${auth.admin-recovery.email:}") String recoveryEmail,
            @Value("${auth.admin-recovery.password:}") String recoveryPassword) {
        this.adminAccountService = adminAccountService;
        this.userRepository = userRepository;
        this.environment = environment;
        this.bootstrapEmail = bootstrapEmail;
        this.bootstrapPassword = bootstrapPassword;
        this.recoveryEmail = recoveryEmail;
        this.recoveryPassword = recoveryPassword;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (arguments.containsOption("auth.admin-recovery")) {
            runRecovery();
            return;
        }
        if (userRepository.count() > 0) {
            return;
        }
        if (!StringUtils.hasText(bootstrapEmail) || !StringUtils.hasText(bootstrapPassword)) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_EMAIL y BOOTSTRAP_ADMIN_PASSWORD son obligatorios con la base sin usuarios.");
        }
        adminAccountService.createBootstrapAdministrator(bootstrapEmail, bootstrapPassword);
    }

    private void runRecovery() {
        if (!"none".equalsIgnoreCase(environment.getProperty("spring.main.web-application-type"))) {
            throw new IllegalStateException(
                    "La recuperación administrativa solo se ejecuta en modo CLI sin servidor web.");
        }
        if (!StringUtils.hasText(recoveryEmail) || !StringUtils.hasText(recoveryPassword)) {
            throw new IllegalStateException(
                    "AUTH_ADMIN_RECOVERY_EMAIL y AUTH_ADMIN_RECOVERY_PASSWORD son obligatorios para recuperar.");
        }
        adminAccountService.recoverAdministrator(recoveryEmail, recoveryPassword);
    }
}
