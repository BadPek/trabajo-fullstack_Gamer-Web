package com.gamerzone.contacto.service;

import com.gamerzone.contacto.dto.request.CrearContactoRequest;
import com.gamerzone.contacto.dto.request.ActualizarEstadoRequest;
import com.gamerzone.contacto.dto.response.ContactoResponse;
import com.gamerzone.contacto.entity.ContactoMensaje;
import com.gamerzone.contacto.repository.ContactoMensajeRepository;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public ContactoResponse create(CrearContactoRequest request) {
        ContactoMensaje mensaje = new ContactoMensaje(
                request.nombre().trim(),
                request.correo().trim().toLowerCase(Locale.ROOT),
                request.asunto().trim(),
                request.mensaje().trim());
        ContactoMensaje saved = repository.saveAndFlush(mensaje);
        return ContactoResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<ContactoResponse> findAll(int page, int size) {
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("creadoEn"),
                        Sort.Order.asc("id")));
        return repository.findAll(pageable).map(ContactoResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ContactoResponse findById(String id) {
        return repository.findById(normalizeUuid(id))
                .map(ContactoResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el mensaje de contacto."));
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
