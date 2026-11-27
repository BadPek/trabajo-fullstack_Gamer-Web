package com.gamerzone.auth.controller;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;
import java.util.List;

import com.gamerzone.auth.config.JwtKeyProvider;
import com.gamerzone.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DiscoveryController {

    private final JwtKeyProvider keys;
    private final String issuer;

    public DiscoveryController(
            JwtKeyProvider keys,
            @Value("${auth.jwt.issuer}") String issuer) {
        this.keys = keys;
        this.issuer = issuer;
    }

    @GetMapping("/.well-known/openid-configuration")
    public Map<String, Object> openIdConfiguration() {
        return Map.of(
                "issuer", issuer,
                "jwks_uri", issuer + "/.well-known/jwks.json",
                "token_endpoint", issuer + "/api/v1/auth/login",
                "response_types_supported", new String[]{"token"},
                "subject_types_supported", new String[]{"public"},
                "id_token_signing_alg_values_supported", new String[]{"RS256"});
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        List<Map<String, String>> publicJwks = keys.verificationKeys().stream()
                .map(key -> Map.of(
                        "kty", "RSA",
                        "use", "sig",
                        "alg", JwtService.ALGORITHM,
                        "kid", key.keyId(),
                        "n", base64Url(key.publicKey().getModulus()),
                        "e", base64Url(key.publicKey().getPublicExponent())))
                .toList();
        return Map.of("keys", publicJwks);
    }

    private static String base64Url(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            bytes = java.util.Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
