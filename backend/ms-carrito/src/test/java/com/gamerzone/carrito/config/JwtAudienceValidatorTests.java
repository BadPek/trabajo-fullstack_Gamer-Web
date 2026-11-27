package com.gamerzone.carrito.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAudienceValidatorTests {

    @Test
    void acceptsMultiAudienceTokensOnlyWhenThisServiceIsIncluded() {
        Jwt accepted = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("aud", List.of("ms-carrito", "ms-pedido"))
                .build();
        Jwt rejected = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("aud", List.of("ms-pedido"))
                .build();

        assertFalse(SecurityConfig.audienceValidator("ms-carrito").validate(accepted).hasErrors());
        assertTrue(SecurityConfig.audienceValidator("ms-carrito").validate(rejected).hasErrors());
    }
}
