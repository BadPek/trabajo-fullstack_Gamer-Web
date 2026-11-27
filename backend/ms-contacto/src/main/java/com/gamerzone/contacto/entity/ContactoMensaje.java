package com.gamerzone.contacto.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "contact_messages")
public class ContactoMensaje {

    public static final String ESTADO_RECIBIDO = "RECIBIDO";

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "message_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", length = 36, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "sender_name_snapshot", nullable = false, length = 100)
    private String nombre;

    @Column(name = "sender_email_snapshot", nullable = false, length = 100)
    private String correo;

    @Column(name = "subject", nullable = false, length = 120)
    private String asunto;

    @Column(name = "message_text", nullable = false, length = 500)
    private String mensaje;

    @Column(name = "status", nullable = false, length = 10)
    private String estado;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime actualizadoEn;

    protected ContactoMensaje() {
    }

    public ContactoMensaje(String nombre, String correo, String asunto, String mensaje) {
        this.nombre = nombre;
        this.correo = correo;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.estado = ESTADO_RECIBIDO;
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        this.creadoEn = now;
        this.actualizadoEn = now;
    }

    @PrePersist
    void initialize() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (estado == null) {
            estado = ESTADO_RECIBIDO;
        }
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now(ZoneOffset.UTC);
        }
        if (actualizadoEn == null) {
            actualizadoEn = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    public void setEstado(String estado) {
        this.estado = estado;
        this.actualizadoEn = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getAsunto() { return asunto; }
    public String getMensaje() { return mensaje; }
    public String getEstado() { return estado; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
}
