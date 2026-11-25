package com.gamerzone.contacto.dto.response;

import java.time.Instant;
import java.time.ZoneOffset;

import com.gamerzone.contacto.entity.ContactoMensaje;

public record ContactoResponse(
        String id,
        String userId,
        String nombre,
        String correo,
        String asunto,
        String mensaje,
        String estado,
        Instant creadoEn,
        Instant actualizadoEn) {

    public static ContactoResponse fromEntity(ContactoMensaje entity) {
        return new ContactoResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getNombre(),
                entity.getCorreo(),
                entity.getAsunto(),
                entity.getMensaje(),
                entity.getEstado(),
                entity.getCreadoEn() == null ? null : entity.getCreadoEn().toInstant(ZoneOffset.UTC),
                entity.getActualizadoEn() == null ? null : entity.getActualizadoEn().toInstant(ZoneOffset.UTC));
    }
}
