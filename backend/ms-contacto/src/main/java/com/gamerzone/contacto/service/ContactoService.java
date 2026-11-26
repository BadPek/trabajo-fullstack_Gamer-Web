package com.gamerzone.contacto.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

import com.gamerzone.contacto.dto.request.ActualizarEstadoRequest;
import com.gamerzone.contacto.dto.request.CrearContactoRequest;
import com.gamerzone.contacto.dto.response.ContactoResponse;
import com.gamerzone.contacto.dto.response.PageResponse;
import com.gamerzone.contacto.entity.ContactoMensaje;
import com.gamerzone.contacto.repository.ContactoMensajeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContactoService {

    private final ContactoMensajeRepository repository;

    public ContactoService(ContactoMensajeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ContactoResponse create(CrearContactoRequest request, String userId) {
        ContactoMensaje mensaje = new ContactoMensaje(
                request.nombre().trim(),
                request.correo().trim().toLowerCase(Locale.ROOT),
                request.asunto().trim(),
                request.mensaje().trim());
        if (userId != null) {
            mensaje.setUserId(normalizeUuid(userId));
        }
        ContactoMensaje saved = repository.saveAndFlush(mensaje);
        return ContactoResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ContactoResponse> findAll(
            int page, int size, String status, OffsetDateTime from, OffsetDateTime to) {
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("creadoEn"),
                        Sort.Order.asc("id")));
        Specification<ContactoMensaje> specification = (root, query, criteriaBuilder) ->
                criteriaBuilder.conjunction();
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("estado"), status));
        }
        if (from != null) {
            var fromUtc = from.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.<LocalDateTime>get("creadoEn"), fromUtc));
        }
        if (to != null) {
            var toUtc = to.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.<LocalDateTime>get("creadoEn"), toUtc));
        }
        return PageResponse.from(repository.findAll(specification, pageable)
                .map(ContactoResponse::fromEntity));
    }

    @Transactional
    public ContactoResponse updateStatus(String id, ActualizarEstadoRequest request) {
        ContactoMensaje mensaje = repository.findById(normalizeUuid(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el mensaje de contacto."));
        mensaje.setEstado(request.estado());
        return ContactoResponse.fromEntity(repository.saveAndFlush(mensaje));
    }

    @Transactional
    public void delete(String id) {
        ContactoMensaje mensaje = repository.findById(normalizeUuid(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el mensaje de contacto."));
        repository.delete(mensaje);
        repository.flush();
    }

    private static String normalizeUuid(String value) {
        if (value == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El identificador del mensaje es obligatorio.");
        }
        try {
            return UUID.fromString(value.trim()).toString().toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El identificador del mensaje debe tener formato UUID.",
                    exception);
        }
    }
}
