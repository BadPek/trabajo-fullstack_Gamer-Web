package com.gamerzone.auth.config;

import java.time.Clock;
import java.util.List;
import java.security.SecureRandom;
import java.text.ParseException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            AuthenticationEntryPoint entryPoint,
            AccessDeniedHandler deniedHandler,
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login",
                                "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/health", "/actuator/health",
                                "/api/v1/auth/csrf",
                                "/.well-known/openid-configuration", "/.well-known/jwks.json",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        .requestMatchers("/api/v1/admin/users", "/api/v1/admin/users/**")
                        .hasRole("ADMINISTRADOR")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder(
            @Value("${auth.password.argon2.salt-length:16}") int saltLength,
            @Value("${auth.password.argon2.hash-length:32}") int hashLength,
            @Value("${auth.password.argon2.parallelism:1}") int parallelism,
            @Value("${auth.password.argon2.memory-kib:19456}") int memoryKiB,
            @Value("${auth.password.argon2.iterations:2}") int iterations) {
        if (saltLength < 16 || hashLength < 32 || parallelism < 1 || memoryKiB < 19456 || iterations < 2) {
            throw new IllegalArgumentException("La configuración Argon2id no puede ser inferior a los mínimos aprobados.");
        }
        return new org.springframework.security.crypto.argon2.Argon2PasswordEncoder(
                saltLength, hashLength, parallelism, memoryKiB, iterations);
    }

    @Bean
    Clock utcClock() {
        return Clock.systemUTC();
    }

    @Bean
    SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @Bean
    JwtDecoder jwtDecoder(JwtKeyProvider keys, @Value("${auth.jwt.issuer}") String issuer) {
        var decoders = keys.verificationKeys().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                JwtKeyProvider.VerificationKey::keyId,
                key -> {
                    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(key.publicKey())
                            .signatureAlgorithm(SignatureAlgorithm.RS256)
                            .build();
                    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
                    return decoder;
                }));
        return token -> {
            String keyId;
            try {
                keyId = SignedJWT.parse(token).getHeader().getKeyID();
            } catch (ParseException exception) {
                throw new JwtException("El header JWT no es válido.", exception);
            }
            JwtDecoder decoder = decoders.get(keyId);
            if (decoder == null) {
                throw new JwtException("El JWT fue firmado con un key ID no publicado.");
            }
            return decoder.decode(token);
        };
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${auth.frontend-origin:http://localhost:5173}") String frontendOrigin) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", frontendOrigin.trim()).stream()
                .distinct()
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-CSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/auth/**", configuration);
        source.registerCorsConfiguration("/api/v1/admin/users", configuration);
        source.registerCorsConfiguration("/api/v1/admin/users/**", configuration);
        return source;
    }
}
