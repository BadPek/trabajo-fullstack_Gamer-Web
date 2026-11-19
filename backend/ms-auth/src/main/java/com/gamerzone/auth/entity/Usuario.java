package com.gamerzone.auth.entity;

import java.util.UUID;

import com.gamerzone.auth.dto.response.UsuarioResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "GZ_AUTH_USERS")
public class Usuario {

    @Id
    @Column(name = "ID", length = 36, nullable = false)
    private String id;

    @Column(name = "RUT", length = 12, nullable = false, unique = true)
    private String rut;

    @Column(name = "NOMBRE", length = 50, nullable = false)
    private String nombre;

    @Column(name = "APELLIDO", length = 100, nullable = false)
    private String apellido;

    @Column(name = "CORREO", length = 100, nullable = false, unique = true)
    private String correo;

    @Column(name = "PASSWORD_HASH", length = 100, nullable = false)
    private String passwordHash;

    @Column(name = "REGION", length = 50, nullable = false)
    private String region;

    @Column(name = "COMUNA", length = 100, nullable = false)
    private String comuna;

    @Column(name = "DIRECCION", length = 300, nullable = false)
    private String direccion;

    @Column(name = "ROL", length = 30, nullable = false)
    private String rol;

    protected Usuario() {
    }

    public Usuario(String rut, String nombre, String apellido, String correo, String passwordHash,
            String region, String comuna, String direccion) {
        this.id = UUID.randomUUID().toString();
        this.rut = rut;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.region = region;
        this.comuna = comuna;
        this.direccion = direccion;
        this.rol = "CLIENTE";
    }

    @PrePersist
    void assignId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public String getRut() { return rut; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public String getRegion() { return region; }
    public String getComuna() { return comuna; }
    public String getDireccion() { return direccion; }
    public String getRol() { return rol; }

    public UsuarioResponse toResponse() {
        return new UsuarioResponse(id, rut, nombre, apellido, correo, region, comuna, direccion);
    }
}
