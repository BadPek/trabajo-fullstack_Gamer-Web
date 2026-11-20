package cl.gamerzone.catalogo.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class JwtAuthenticationConverterTests {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void mapsTheConfiguredAuthorityClaimAndPrefix() {
        JwtAuthenticationConverter converter =
                securityConfig.jwtAuthenticationConverter("customAuthorities", "ROLE_");
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("customAuthorities", List.of("ADMINISTRADOR"))
                .build();

        assertTrue(converter.convert(jwt).getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMINISTRADOR")));
    }

    @Test
    void doesNotAssumeARolesClaimWhenNoAuthorityClaimIsConfigured() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter("", "ROLE_");
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", List.of("ADMINISTRADOR"))
                .build();

        assertFalse(converter.convert(jwt).getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMINISTRADOR")));
    }
}
