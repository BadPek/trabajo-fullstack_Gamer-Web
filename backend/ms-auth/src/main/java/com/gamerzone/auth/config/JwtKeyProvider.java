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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtKeyProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtKeyProvider(
            @Value("${auth.jwt.private-key}") String privateKeyPem,
            @Value("${auth.jwt.public-key}") String publicKeyPem) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey parsedPrivateKey = factory.generatePrivate(
                    new PKCS8EncodedKeySpec(decodePem(privateKeyPem, "PRIVATE KEY")));
            PublicKey parsedPublicKey = factory.generatePublic(
                    new X509EncodedKeySpec(decodePem(publicKeyPem, "PUBLIC KEY")));
            if (!(parsedPrivateKey instanceof RSAPrivateKey rsaPrivateKey)
                    || !(parsedPublicKey instanceof RSAPublicKey rsaPublicKey)
                    || !rsaPrivateKey.getModulus().equals(rsaPublicKey.getModulus())
                    || !matches(rsaPrivateKey, rsaPublicKey)) {
                throw new IllegalArgumentException("La clave pública debe corresponder a la clave privada RSA.");
            }
            this.privateKey = rsaPrivateKey;
            this.publicKey = rsaPublicKey;
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("No se pudieron cargar las claves RSA configuradas para JWT.", exception);
        }
    }

    public RSAPrivateKey privateKey() {
        return privateKey;
    }

    public RSAPublicKey publicKey() {
        return publicKey;
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
