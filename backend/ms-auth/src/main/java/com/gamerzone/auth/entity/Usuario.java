package com.gamerzone.auth.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.gamerzone.auth.dto.response.UsuarioResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Cuenta de usuario (tabla {@code users}, migración V1 de ms-auth).
 *
 * <p>Los datos personales solo quedan nulos en una cuenta anonimizada; la base exige el perfil
 * completo en cualquier otra. El RUT se guarda normalizado (sin puntos ni guion, K mayúscula) y el
 * correo en minúsculas. {@code created_at} y {@code updated_at} los asigna la base.</p>
 */
@Entity
@Table(name = "users")
public class Usuario {

    public static final String ROL_CLIENTE = "CLIENTE";
    public static final String ESTADO_ACTIVO = "ACTIVE";

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "rut_normalized", length = 9)
    private String rut;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "apellidos", length = 100)
    private String apellido;

    @Column(name = "email_normalized", nullable = false, length = 100)
    private String correo;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "commune_id")
    private Comuna comuna;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @Column(name = "role_code", nullable = false, length = 20)
    private String rol;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "anonymized_at")
    private LocalDateTime anonymizedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected Usuario() {
    }

    public Usuario(String rut, String nombre, String apellido, String correo, String passwordHash,
            Comuna comuna, String direccion) {
        this.id = UUID.randomUUID().toString();
        this.rut = rut;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.comuna = comuna;
        this.direccion = direccion;
        this.rol = ROL_CLIENTE;
        this.status = ESTADO_ACTIVO;
    }

    @PrePersist
    void assignId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    /** Solo una cuenta activa y no anonimizada puede iniciar sesión. */
    public boolean isActive() {
        return ESTADO_ACTIVO.equals(status) && anonymizedAt == null;
    }

    public String getId() { return id; }
    public String getRut() { return rut; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public Comuna getComuna() { return comuna; }
    public String getDireccion() { return direccion; }
    public String getRol() { return rol; }
    public String getStatus() { return status; }
    public LocalDateTime getAnonymizedAt() { return anonymizedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public UsuarioResponse toResponse() {
        String region = comuna == null ? null : comuna.getRegion().getCode();
        String nombreComuna = comuna == null ? null : comuna.getName();
        return new UsuarioResponse(id, rut, nombre, apellido, correo, region, nombreComuna, direccion);
    }
}
