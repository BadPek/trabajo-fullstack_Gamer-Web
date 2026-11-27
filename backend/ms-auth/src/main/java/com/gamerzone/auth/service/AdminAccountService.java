package com.gamerzone.auth.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.gamerzone.auth.dto.request.ActualizarUsuarioRequest;
import com.gamerzone.auth.dto.response.AdminUsuarioResponse;
import com.gamerzone.auth.dto.response.PageResponse;
import com.gamerzone.auth.entity.Comuna;
import com.gamerzone.auth.entity.Usuario;
import com.gamerzone.auth.exception.ApiException;
import com.gamerzone.auth.repository.ComunaRepository;
import com.gamerzone.auth.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminAccountService.class);

    private final UsuarioRepository usuarioRepository;
    private final ComunaRepository comunaRepository;
    private final PasswordEncoder passwordEncoder;
    private final RutValidator rutValidator;
    private final RefreshTokenService refreshTokenService;
    private final ContactMessageCleanupClient contactMessageCleanupClient;
    private final Clock clock;

    public AdminAccountService(
            UsuarioRepository usuarioRepository,
            ComunaRepository comunaRepository,
            PasswordEncoder passwordEncoder,
            RutValidator rutValidator,
            RefreshTokenService refreshTokenService,
            ContactMessageCleanupClient contactMessageCleanupClient,
            Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.comunaRepository = comunaRepository;
        this.passwordEncoder = passwordEncoder;
        this.rutValidator = rutValidator;
        this.refreshTokenService = refreshTokenService;
        this.contactMessageCleanupClient = contactMessageCleanupClient;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUsuarioResponse> findAll(
            String actorId, int page, int size, String role, String status) {
        requireAdministrator(actorId);
        Specification<Usuario> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (role != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("rol"), role));
        }
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }
        Page<Usuario> users = usuarioRepository.findAll(specification, PageRequest.of(
                page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"))));
        return PageResponse.from(users.map(AdminAccountService::toResponse));
    }

    @Transactional(readOnly = true)
    public AdminUsuarioResponse findById(String actorId, String userId) {
        requireAdministrator(actorId);
        return toResponse(findUser(userId));
    }

    @Transactional
    public AdminUsuarioResponse updateProfile(
            String actorId, String userId, ActualizarUsuarioRequest request) {
        requireAdministrator(actorId);
        lockActiveAdministrators();
        requireAdministrator(actorId);
        Usuario user = findUser(userId);
        if (user.getAnonymizedAt() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "ACCOUNT_ANONYMIZED",
                    "Una cuenta anonimizada no puede modificarse.");
        }

        String rut = valueOr(request.rut(), user.getRut());
        if (rut == null || rut.isBlank()) {
            throw incompleteProfile();
        }
        rut = rutValidator.normalize(rut);
        if (!rutValidator.isValid(rut)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RUT", "El RUT no es válido.");
        }
        if (!rut.equals(user.getRut()) && usuarioRepository.existsByRutAndIdNot(rut, userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "RUT_ALREADY_REGISTERED", "El RUT ya está registrado.");
        }

        String name = requiredText(valueOr(request.nombre(), user.getNombre()));
        String surname = requiredText(valueOr(request.apellidos(), user.getApellido()));
        String emailInput = valueOr(request.correo(), user.getCorreo());
        String email = normalizeEmail(emailInput);
        if (!email.equals(user.getCorreo())
                && usuarioRepository.findByCorreoIgnoreCase(email).filter(other -> !other.getId().equals(userId))
                        .isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED",
                    "El correo ya está registrado.");
        }
        String region = valueOr(request.region(),
                user.getComuna() == null ? null : user.getComuna().getRegion().getCode());
        String communeName = valueOr(request.comuna(),
                user.getComuna() == null ? null : user.getComuna().getName());
        if (region == null || region.isBlank() || communeName == null || communeName.isBlank()) {
            throw incompleteProfile();
        }
        Comuna commune = comunaRepository.findByRegion_CodeAndNameIgnoreCase(
                        region.trim().toLowerCase(Locale.ROOT), communeName.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_COMMUNE",
                        "La comuna no corresponde a la región indicada."));
        String address = requiredText(valueOr(request.direccion(), user.getDireccion()));

        user.updateProfile(rut, name, surname, email, commune, address);
        return toResponse(usuarioRepository.saveAndFlush(user));
    }

    @Transactional
    public AdminUsuarioResponse changeRole(String actorId, String userId, String role) {
        requireAdministrator(actorId);
        List<Usuario> activeAdmins = lockActiveAdministrators();
        requireAdministrator(actorId);
        Usuario user = findUser(userId);
        if (user.getAnonymizedAt() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "ACCOUNT_ANONYMIZED",
                    "Una cuenta anonimizada no puede modificarse.");
        }
        if (Usuario.ROL_CLIENTE.equals(role) && !Usuario.ROL_CLIENTE.equals(user.getRol())) {
            protectLastAdministrator(user, activeAdmins);
            if (!user.hasCompleteProfile()) {
                throw incompleteProfile();
            }
        }
        if (!role.equals(user.getRol())) {
            user.changeRole(role);
            refreshTokenService.revokeAllForUser(userId);
            LOGGER.info("AUTH_ADMIN_ROLE_CHANGED actor={} target={} role={}", actorId, userId, role);
        }
        return toResponse(usuarioRepository.saveAndFlush(user));
    }

    @Transactional
    public AdminUsuarioResponse changeStatus(String actorId, String userId, String status) {
        requireAdministrator(actorId);
        List<Usuario> activeAdmins = lockActiveAdministrators();
        requireAdministrator(actorId);
        Usuario user = findUser(userId);
        if (user.getAnonymizedAt() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "ACCOUNT_ANONYMIZED",
                    "Una cuenta anonimizada no puede modificarse.");
        }
        if (Usuario.ESTADO_ACTIVO.equals(status) && !user.hasCompleteProfile()
                && !Usuario.ROL_ADMINISTRADOR.equals(user.getRol())) {
            throw incompleteProfile();
        }
        if (Usuario.ESTADO_INACTIVO.equals(status)
                && Usuario.ROL_ADMINISTRADOR.equals(user.getRol())
                && Usuario.ESTADO_ACTIVO.equals(user.getStatus())) {
            protectLastAdministrator(user, activeAdmins);
        }
        if (!status.equals(user.getStatus())) {
            user.changeStatus(status);
            LOGGER.info("AUTH_ADMIN_STATUS_CHANGED actor={} target={} status={}", actorId, userId, status);
        }
        if (Usuario.ESTADO_INACTIVO.equals(status)) {
            refreshTokenService.revokeAllForUser(userId);
        }
        return toResponse(usuarioRepository.saveAndFlush(user));
    }

    @Transactional
    public void delete(String actorId, String userId, String accessToken) {
        requireAdministrator(actorId);
        List<Usuario> activeAdmins = lockActiveAdministrators();
        requireAdministrator(actorId);
        Usuario user = findUser(userId);
        if (user.getAnonymizedAt() != null) {
            return;
        }
        if (Usuario.ROL_ADMINISTRADOR.equals(user.getRol())
                && Usuario.ESTADO_ACTIVO.equals(user.getStatus())) {
            protectLastAdministrator(user, activeAdmins);
        }
        contactMessageCleanupClient.deleteForUser(userId, accessToken);
        refreshTokenService.revokeAllForUser(userId);
        user.anonymize(LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        usuarioRepository.saveAndFlush(user);
        LOGGER.info("AUTH_ADMIN_ACCOUNT_ANONYMIZED actor={} target={}", actorId, userId);
    }

    @Transactional
    public Usuario createBootstrapAdministrator(String email, String rawPassword) {
        if (usuarioRepository.count() != 0) {
            throw new IllegalStateException("El bootstrap de administrador solo se permite con la base vacía.");
        }
        String normalizedEmail = normalizeEmail(email);
        validatePassword(rawPassword);
        Usuario administrator = Usuario.bootstrapAdministrator(
                normalizedEmail, passwordEncoder.encode(rawPassword));
        Usuario saved = usuarioRepository.saveAndFlush(administrator);
        LOGGER.info("AUTH_ADMIN_BOOTSTRAPPED account={}", saved.getId());
        return saved;
    }

    @Transactional
    public Usuario recoverAdministrator(String email, String rawPassword) {
        List<Usuario> activeAdmins = lockActiveAdministrators();
        if (!activeAdmins.isEmpty()) {
            throw new IllegalStateException("La recuperación CLI solo se permite cuando no hay administradores activos.");
        }
        String normalizedEmail = normalizeEmail(email);
        validatePassword(rawPassword);
        Optional<Usuario> existing = usuarioRepository.findByCorreoIgnoreCase(normalizedEmail);
        Usuario administrator;
        if (existing.isPresent()) {
            administrator = existing.get();
            if (!administrator.isActive() || administrator.getAnonymizedAt() != null) {
                throw new IllegalStateException("No se puede recuperar una cuenta inactiva o anonimizada.");
            }
            administrator.changeRole(Usuario.ROL_ADMINISTRADOR);
            administrator.changePasswordHash(passwordEncoder.encode(rawPassword));
            refreshTokenService.revokeAllForUser(administrator.getId());
        } else {
            administrator = Usuario.bootstrapAdministrator(
                    normalizedEmail, passwordEncoder.encode(rawPassword));
        }
        Usuario saved = usuarioRepository.saveAndFlush(administrator);
        LOGGER.warn("AUTH_ADMIN_RECOVERY_COMPLETED account={} channel=cli", saved.getId());
        return saved;
    }

    private void requireAdministrator(String actorId) {
        usuarioRepository.findById(actorId)
                .filter(Usuario::isActive)
                .filter(user -> Usuario.ROL_ADMINISTRADOR.equals(user.getRol()))
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                        "Se requiere una cuenta ADMINISTRADOR activa."));
    }

    private List<Usuario> lockActiveAdministrators() {
        return usuarioRepository.findActiveAdministratorsForUpdate(
                Usuario.ROL_ADMINISTRADOR, Usuario.ESTADO_ACTIVO);
    }

    private static void protectLastAdministrator(Usuario target, List<Usuario> activeAdmins) {
        if (Usuario.ROL_ADMINISTRADOR.equals(target.getRol())
                && Usuario.ESTADO_ACTIVO.equals(target.getStatus())
                && activeAdmins.size() <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, "LAST_ADMIN_PROTECTED",
                    "No se puede desactivar, eliminar o degradar al último administrador activo.");
        }
    }

    private Usuario findUser(String userId) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        "No se encontró la cuenta solicitada."));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EMAIL", "El correo es obligatorio.");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 100 || !normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EMAIL", "El correo no es válido.");
        }
        if (!normalized.endsWith("@duoc.cl") && !normalized.endsWith("@duocuc.cl")
                && !normalized.endsWith("@profesor.duoc.cl") && !normalized.endsWith("@gmail.com")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_DOMAIN",
                    "Solo se permiten correos @duoc.cl, @duocuc.cl, @profesor.duoc.cl o @gmail.com.");
        }
        return normalized;
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 12 || password.length() > 128) {
            throw new IllegalArgumentException("La contraseña bootstrap debe tener entre 12 y 128 caracteres.");
        }
    }

    private static String requiredText(String value) {
        if (value == null || value.isBlank()) {
            throw incompleteProfile();
        }
        return value.trim();
    }

    private static String valueOr(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private static ApiException incompleteProfile() {
        return new ApiException(HttpStatus.BAD_REQUEST, "PROFILE_REQUIRED",
                "La cuenta requiere un perfil completo para esta operación.");
    }

    private static AdminUsuarioResponse toResponse(Usuario user) {
        Comuna comuna = user.getComuna();
        return new AdminUsuarioResponse(
                user.getId(),
                user.getRut(),
                user.getNombre(),
                user.getApellido(),
                user.getCorreo(),
                comuna == null ? null : comuna.getRegion().getCode(),
                comuna == null ? null : comuna.getName(),
                user.getDireccion(),
                user.getRol(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getAnonymizedAt());
    }
}
