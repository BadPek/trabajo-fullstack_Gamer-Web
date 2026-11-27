package com.gamerzone.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class JwtKeyRotationTest {

    private static final String ISSUER = "https://auth.example.test";

    @Test
    void decoderAcceptsCurrentAndRetainedPublicKeysAndRejectsUnknownKeyIds() throws Exception {
        KeyPair current = rsaKeyPair();
        KeyPair previous = rsaKeyPair();
        ObjectMapper objectMapper = JsonMapper.builder().build();
        String oldKeysJson = objectMapper.writeValueAsString(List.of(Map.of(
                "kid", "old-key",
                "pem", pem("PUBLIC KEY", previous.getPublic().getEncoded()))));
        JwtKeyProvider keyProvider = new JwtKeyProvider(
                "current-key",
                pem("PRIVATE KEY", current.getPrivate().getEncoded()),
                pem("PUBLIC KEY", current.getPublic().getEncoded()),
                oldKeysJson,
                objectMapper);
        JwtDecoder decoder = new SecurityConfig().jwtDecoder(keyProvider, ISSUER);

        assertEquals("current-user", decoder.decode(token(current, "current-key")).getSubject());
        assertEquals("previous-user", decoder.decode(token(previous, "old-key")).getSubject());
        assertThrows(JwtException.class, () -> decoder.decode(token(previous, "retired-key")));
    }

    @Test
    void rejectsDuplicateRetainedKeyIds() throws Exception {
        KeyPair pair = rsaKeyPair();
        ObjectMapper objectMapper = JsonMapper.builder().build();
        String duplicateKeyId = objectMapper.writeValueAsString(List.of(Map.of(
                "kid", "current-key",
                "pem", pem("PUBLIC KEY", pair.getPublic().getEncoded()))));

        assertThrows(IllegalStateException.class, () -> new JwtKeyProvider(
                "current-key",
                pem("PRIVATE KEY", pair.getPrivate().getEncoded()),
                pem("PUBLIC KEY", pair.getPublic().getEncoded()),
                duplicateKeyId,
                objectMapper));
    }

    private static String token(KeyPair pair, String keyId) throws Exception {
        long now = Instant.now().getEpochSecond();
        String header = encode(Map.of("alg", "RS256", "typ", "JWT", "kid", keyId));
        String claims = encode(Map.of(
                "iss", ISSUER,
                "sub", keyId.equals("current-key") ? "current-user" : "previous-user",
                "iat", now,
                "exp", now + 600));
        String signingInput = header + "." + claims;
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(pair.getPrivate());
        signer.update(signingInput.getBytes(StandardCharsets.US_ASCII));
        return signingInput + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signer.sign());
    }

    private static String encode(Object value) throws Exception {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(JsonMapper.builder().build().writeValueAsBytes(value));
    }

    private static KeyPair rsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String pem(String label, byte[] bytes) {
        return "-----BEGIN " + label + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(bytes)
                + "\n-----END " + label + "-----";
    }
}
