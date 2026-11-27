package com.gamerzone.auth.config;

import java.security.KeyFactory;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtKeyProvider {

    private final String activeKeyId;
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final List<VerificationKey> verificationKeys;
    private final Map<String, RSAPublicKey> publicKeysById;

    public JwtKeyProvider(
            @Value("${auth.jwt.key-id}") String activeKeyId,
            @Value("${auth.jwt.private-key}") String privateKeyPem,
            @Value("${auth.jwt.public-key}") String publicKeyPem,
            @Value("${auth.jwt.previous-public-keys-json:[]}") String previousPublicKeysJson,
            ObjectMapper objectMapper) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey parsedPrivateKey = factory.generatePrivate(
                    new PKCS8EncodedKeySpec(decodePem(privateKeyPem, "PRIVATE KEY")));
            PublicKey parsedPublicKey = factory.generatePublic(
                    new X509EncodedKeySpec(decodePem(publicKeyPem, "PUBLIC KEY")));
            if (!(parsedPrivateKey instanceof RSAPrivateKey rsaPrivateKey)
                    || !(parsedPublicKey instanceof RSAPublicKey rsaPublicKey)
                    || !rsaPrivateKey.getModulus().equals(rsaPublicKey.getModulus())
                    || rsaPublicKey.getModulus().bitLength() < 2048
                    || activeKeyId == null || activeKeyId.isBlank() || activeKeyId.length() > 128
                    || !matches(rsaPrivateKey, rsaPublicKey)) {
                throw new IllegalArgumentException("La clave pública debe corresponder a la clave privada RSA.");
            }
            Map<String, RSAPublicKey> keysById = new HashMap<>();
            List<VerificationKey> configuredKeys = new ArrayList<>();
            keysById.put(activeKeyId, rsaPublicKey);
            configuredKeys.add(new VerificationKey(activeKeyId, rsaPublicKey));
            PreviousPublicKey[] previousKeys = objectMapper.readValue(
                    previousPublicKeysJson, PreviousPublicKey[].class);
            for (PreviousPublicKey previous : previousKeys) {
                if (previous.kid() == null || previous.kid().isBlank() || previous.kid().length() > 128
                        || keysById.containsKey(previous.kid())) {
                    throw new IllegalArgumentException("Los key IDs JWT deben ser válidos y únicos.");
                }
                PublicKey parsedPrevious = factory.generatePublic(
                        new X509EncodedKeySpec(decodePem(previous.pem(), "PUBLIC KEY")));
                if (!(parsedPrevious instanceof RSAPublicKey previousRsa)
                        || previousRsa.getModulus().bitLength() < 2048) {
                    throw new IllegalArgumentException("Las claves públicas JWT deben ser RSA de al menos 2048 bits.");
                }
                keysById.put(previous.kid(), previousRsa);
                configuredKeys.add(new VerificationKey(previous.kid(), previousRsa));
            }
            this.activeKeyId = activeKeyId;
            this.privateKey = rsaPrivateKey;
            this.publicKey = rsaPublicKey;
            this.verificationKeys = List.copyOf(configuredKeys);
            this.publicKeysById = Map.copyOf(keysById);
        } catch (GeneralSecurityException | IllegalArgumentException | JacksonException exception) {
            throw new IllegalStateException("No se pudieron cargar las claves RSA configuradas para JWT.", exception);
        }
    }

    public String activeKeyId() {
        return activeKeyId;
    }

    public RSAPrivateKey privateKey() {
        return privateKey;
    }

    public RSAPublicKey publicKey() {
        return publicKey;
    }

    public List<VerificationKey> verificationKeys() {
        return verificationKeys;
    }

    public RSAPublicKey publicKey(String keyId) {
        return publicKeysById.get(keyId);
    }

    public record VerificationKey(String keyId, RSAPublicKey publicKey) {
    }

    private record PreviousPublicKey(String kid, String pem) {
    }

    private static byte[] decodePem(String pem, String label) {
        String normalized = pem.replace("\\n", "\n").trim()
                .replace("-----BEGIN " + label + "-----", "")
                .replace("-----END " + label + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    private static boolean matches(RSAPrivateKey privateKey, RSAPublicKey publicKey)
            throws GeneralSecurityException {
        byte[] challenge = "GamerZone JWT key validation".getBytes(StandardCharsets.US_ASCII);
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(challenge);
        byte[] signature = signer.sign();
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(challenge);
        return verifier.verify(signature);
    }
}
