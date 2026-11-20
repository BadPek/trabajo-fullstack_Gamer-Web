package com.gamerzone.carrito.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GamerZone - Microservicio Carrito",
                version = "v1",
                description = "Administra las operaciones del carrito de compras. Los contratos son provisionales; "
                        + "las operaciones dependen del modelo relacional oficial y actualmente responden HTTP 501."))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT emitido por ms-auth y validado por ms-carrito.")
public class OpenApiConfig {
}
