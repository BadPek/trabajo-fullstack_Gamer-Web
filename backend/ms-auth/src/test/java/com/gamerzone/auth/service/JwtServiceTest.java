package com.gamerzone.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;

import com.gamerzone.auth.config.JwtKeyProvider;
import com.gamerzone.auth.entity.Comuna;
import com.gamerzone.auth.entity.Region;
import com.gamerzone.auth.entity.Usuario;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void emitsVerifiableRs256TokenWithIssuerSubjectAndRole() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        var objectMapper = JsonMapper.builder().build();
        JwtKeyProvider keys = new JwtKeyProvider(
                "key-1",
                pem("PRIVATE KEY", pair.getPrivate().getEncoded()),
                pem("PUBLIC KEY", pair.getPublic().getEncoded()), "[]", objectMapper);
        JwtService service = new JwtService(keys, objectMapper,
                "https://auth.example.test", 600,
                "ms-catalogo,ms-carrito,ms-contacto,ms-pedido");
        Usuario usuario = new Usuario("190110222", "Ana", "Gamer", "ana@gmail.com", "unused-hash",
                new Comuna(101, new Region(1, "metropolitana", "Región Metropolitana de Santiago"),
                        "Santiago Centro"),
                "Calle 1");

        String[] parts = service.issue(usuario).split("\\.");
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(pair.getPublic());
        verifier.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));

        assertEquals(3, parts.length);
        assertTrue(verifier.verify(Base64.getUrlDecoder().decode(parts[2])));
        String header = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String claims = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        assertTrue(header.contains("\"alg\":\"RS256\""));
        assertTrue(claims.contains("\"iss\":\"https://auth.example.test\""));
        assertTrue(claims.contains("\"sub\":\"" + usuario.getId() + "\""));
        assertTrue(claims.contains("\"aud\":[\"ms-catalogo\",\"ms-carrito\",\"ms-contacto\",\"ms-pedido\"]"));
        assertTrue(claims.contains("\"email\":\"ana@gmail.com\""));
        assertTrue(claims.contains("\"roles\":[\"CLIENTE\"]"));
        assertFalse(claims.contains("\"authorities\""));
        assertEquals(600, service.expiresInSeconds());
    }

    @Test
    void rejectsBlankOrDuplicateAudienceConfiguration() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        var objectMapper = JsonMapper.builder().build();
        JwtKeyProvider keys = new JwtKeyProvider(
                "key-1",
                pem("PRIVATE KEY", pair.getPrivate().getEncoded()),
                pem("PUBLIC KEY", pair.getPublic().getEncoded()), "[]", objectMapper);

        assertThrows(IllegalArgumentException.class, () -> new JwtService(
                keys, objectMapper, "https://auth.example.test", 600, " "));
        assertThrows(IllegalArgumentException.class, () -> new JwtService(
                keys, objectMapper, "https://auth.example.test", 600,
                "ms-carrito,ms-carrito"));
    }

    private static String pem(String label, byte[] bytes) {
        return "-----BEGIN " + label + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(bytes)
                + "\n-----END " + label + "-----";
    }
}
