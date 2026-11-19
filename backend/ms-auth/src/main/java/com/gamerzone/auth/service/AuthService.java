package com.gamerzone.auth.service;

import java.util.Locale;

import com.gamerzone.auth.dto.request.LoginRequest;
import com.gamerzone.auth.dto.request.RegistroRequest;
import com.gamerzone.auth.dto.response.AuthResponse;
import com.gamerzone.auth.dto.response.UsuarioResponse;
import com.gamerzone.auth.entity.Usuario;
import com.gamerzone.auth.exception.ApiException;
import com.gamerzone.auth.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RutValidator rutValidator;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            RutValidator rutValidator, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.rutValidator = rutValidator;
        this.jwtService = jwtService;
    }

    @Transactional
    public UsuarioResponse register(RegistroRequest request) {
        String rut = rutValidator.normalize(request.rut());
        String correo = request.correo().trim().toLowerCase(Locale.ROOT);
        if (!rutValidator.isValid(rut)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RUT", "El RUT no es válido.");
        }
        if (!allowedEmail(correo)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_DOMAIN",
                    "Solo se permiten correos @duoc.cl, @profesor.duoc.cl o @gmail.com.");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED",
                    "El correo ya está registrado.");
        }
        if (usuarioRepository.existsByRut(rut)) {
            throw new ApiException(HttpStatus.CONFLICT, "RUT_ALREADY_REGISTERED", "El RUT ya está registrado.");
        }
        Usuario usuario = new Usuario(rut, request.nombre().trim(), request.apellido().trim(), correo,
                passwordEncoder.encode(request.pass()), request.region().trim(), request.comuna().trim(),
                request.direccion().trim());
        return usuarioRepository.save(usuario).toResponse();
    }

    @Transactional(readOnly = true)
    public AuthResponse authenticate(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(request.correo().trim())
                .filter(account -> passwordEncoder.matches(request.pass(), account.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "Correo o contraseña incorrectos."));
        return new AuthResponse("Bearer", jwtService.issue(usuario),
                jwtService.expiresInSeconds(), usuario.toResponse());
    }

    private static boolean allowedEmail(String email) {
        return email.endsWith("@duoc.cl") || email.endsWith("@profesor.duoc.cl") || email.endsWith("@gmail.com");
    }
}
