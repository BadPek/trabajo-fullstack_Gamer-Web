package com.gamerzone.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.gamerzone.auth.config.JwtKeyProvider;
import com.gamerzone.auth.entity.Usuario;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public static final String ALGORITHM = "RS256";

    private final JwtKeyProvider keys;
    private final ObjectMapper objectMapper;
    private final String issuer;
    private final long expiresInSeconds;
    private final List<String> audiences;

    public JwtService(
            JwtKeyProvider keys,
            ObjectMapper objectMapper,
            @Value("${auth.jwt.issuer}") String issuer,
            @Value("${auth.jwt.expiration-seconds:600}") long expiresInSeconds,
            @Value("${auth.jwt.audiences:ms-catalogo,ms-carrito,ms-contacto,ms-pedido}") String audiences) {
        if (expiresInSeconds < 60) {
            throw new IllegalArgumentException("La vigencia JWT debe ser de al menos 60 segundos.");
        }
        this.keys = keys;
        this.objectMapper = objectMapper;
        this.issuer = issuer;
        this.expiresInSeconds = expiresInSeconds;
        this.audiences = parseAudiences(audiences);
    }

    public String issue(Usuario usuario) {
        Instant issuedAt = Instant.now();
        Map<String, Object> header = Map.of("alg", ALGORITHM, "typ", "JWT", "kid", keys.activeKeyId());
        Map<String, Object> claims = Map.of(
                "iss", issuer,
                "sub", usuario.getId(),
                "email", usuario.getCorreo(),
                "aud", audiences,
                "iat", issuedAt.getEpochSecond(),
                "exp", issuedAt.plusSeconds(expiresInSeconds).getEpochSecond(),
                "jti", UUID.randomUUID().toString(),
                "roles", List.of(usuario.getRol()));
        try {
            String signingInput = encode(header) + "." + encode(claims);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(keys.privateKey());
            signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            return signingInput + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
        } catch (GeneralSecurityException | JacksonException exception) {
            throw new IllegalStateException("No fue posible emitir el token JWT.", exception);
        }
    }

    public long expiresInSeconds() {
        return expiresInSeconds;
    }

    private String encode(Object value) throws JacksonException {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private static List<String> parseAudiences(String audienceList) {
        if (audienceList == null || audienceList.isBlank()) {
            throw new IllegalArgumentException("Se debe configurar al menos una audiencia JWT.");
        }
        List<String> parsed = Arrays.stream(audienceList.split(",", -1))
                .map(String::trim)
                .toList();
        if (parsed.stream().anyMatch(String::isBlank) || parsed.stream().distinct().count() != parsed.size()) {
            throw new IllegalArgumentException("Las audiencias JWT deben ser únicas y no estar vacías.");
        }
        return parsed;
    }
}
