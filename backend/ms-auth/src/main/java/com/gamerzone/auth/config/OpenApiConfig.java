package com.gamerzone.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI authOpenApi() {
        return new OpenAPI()
                .info(new Info().title("GamerZone Auth API")
                        .version("v1")
                        .description("Registro, inicio de sesión y claves públicas JWT."))
                .components(new Components());
    }
}
