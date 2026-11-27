package cl.gamerzone.catalogo.config;

import java.io.IOException;
import java.util.List;

import cl.gamerzone.catalogo.exception.ApiError;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.StringUtils;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            AuthenticationEntryPoint apiAuthenticationEntryPoint,
            AccessDeniedHandler apiAccessDeniedHandler) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/admin/products/**")
                    .hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/products", "/api/v1/products/**", "/api/v1/health", "/api/v1/health/**",
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml")
                    .permitAll()
                .anyRequest().denyAll()
            )
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(apiAuthenticationEntryPoint)
                    .accessDeniedHandler(apiAccessDeniedHandler))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        http.oauth2ResourceServer(oauth2 -> oauth2
                .authenticationEntryPoint(apiAuthenticationEntryPoint)
                .accessDeniedHandler(apiAccessDeniedHandler)
                .jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:${JWT_JWKS_URI:${JWT_ISSUER_URI:http://localhost:8084}/.well-known/jwks.json}}") String jwkSetUri,
            @Value("${catalogo.security.jwt.audience:ms-catalogo}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                audienceValidator(audience)));
        return decoder;
    }

    static OAuth2TokenValidator<Jwt> audienceValidator(String audience) {
        if (!StringUtils.hasText(audience)) {
            throw new IllegalArgumentException("La audiencia JWT de ms-catalogo es obligatoria.");
        }
        return jwt -> jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "El token no está destinado a ms-catalogo.", null));
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            @Value("${catalogo.security.jwt.authorities-claim:roles}") String authoritiesClaim,
            @Value("${catalogo.security.jwt.authority-prefix:ROLE_}") String authorityPrefix) {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        if (StringUtils.hasText(authoritiesClaim)) {
            authoritiesConverter.setAuthoritiesClaimName(authoritiesClaim);
            authoritiesConverter.setAuthorityPrefix(authorityPrefix);
        }

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, exception) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            writeSecurityError(
                    objectMapper,
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Autenticación requerida",
                    "UNAUTHORIZED",
                    "Se requiere un JWT válido para acceder a este recurso.");
        };
    }

    @Bean
    public AccessDeniedHandler apiAccessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, exception) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"insufficient_scope\"");
            writeSecurityError(
                    objectMapper,
                    request,
                    response,
                    HttpStatus.FORBIDDEN,
                    "Acceso denegado",
                    "FORBIDDEN",
                    "No tienes permisos para realizar esta operación.");
        };
    }

    private static void writeSecurityError(
            ObjectMapper objectMapper,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String title,
            String code,
            String detail) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiError(
                "about:blank",
                title,
                status.value(),
                detail,
                request.getRequestURI(),
                code,
                List.of()));
    }
}
