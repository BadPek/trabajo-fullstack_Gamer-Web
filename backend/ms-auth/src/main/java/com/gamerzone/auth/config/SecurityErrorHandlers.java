package com.gamerzone.auth.config;

import java.io.IOException;
import java.util.List;

import com.gamerzone.auth.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class SecurityErrorHandlers {

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, exception) -> writeError(objectMapper, request, response,
                HttpStatus.UNAUTHORIZED, "Autenticación requerida", "UNAUTHORIZED",
                "Se requiere un JWT válido para acceder a este recurso.");
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, exception) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"insufficient_scope\"");
            writeError(objectMapper, request, response, HttpStatus.FORBIDDEN,
                    "Acceso denegado", "FORBIDDEN", "No tienes permisos para realizar esta operación.");
        };
    }

    private static void writeError(
            ObjectMapper mapper,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String title,
            String code,
            String detail) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(),
                new ApiError("about:blank", title, status.value(), detail, request.getRequestURI(), code, List.of()));
    }
}
