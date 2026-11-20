package cl.gamerzone.catalogo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GamerZone ms-catalogo API",
                version = "v1",
                description = "Contrato del catálogo de productos. Las operaciones CRUD requieren la persistencia "
                        + "del modelo relacional aprobado."))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT validado por ms-catalogo como OAuth2 Resource Server.")
public class OpenApiConfig {
}
