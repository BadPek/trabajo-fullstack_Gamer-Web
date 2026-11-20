package com.gamerzone.auth.controller;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;

import com.gamerzone.auth.config.JwtKeyProvider;
import com.gamerzone.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DiscoveryController {

    private final JwtKeyProvider keys;
    private final String issuer;
    private final String keyId;

    public DiscoveryController(
            JwtKeyProvider keys,
            @Value("${auth.jwt.issuer}") String issuer,
            @Value("${auth.jwt.key-id}") String keyId) {
        this.keys = keys;
        this.issuer = issuer;
        this.keyId = keyId;
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
        RSAPublicKey publicKey = keys.publicKey();
        return Map.of("keys", new Object[]{Map.of(
                "kty", "RSA",
                "use", "sig",
                "alg", JwtService.ALGORITHM,
                "kid", keyId,
                "n", base64Url(publicKey.getModulus()),
                "e", base64Url(publicKey.getPublicExponent()))});
    }

    private static String base64Url(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            bytes = java.util.Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
