package com.gamerzone.contacto.service;

import com.gamerzone.contacto.dto.request.CrearContactoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContactoService {

    public void create(CrearContactoRequest request) {
        throw persistenceNotConfigured();
    }

    public void findAll(int page, int size) {
        throw persistenceNotConfigured();
    }

    public void findById(Long id) {
        throw persistenceNotConfigured();
    }

    private ResponseStatusException persistenceNotConfigured() {
        return new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "La persistencia de mensajes depende de la aprobación del modelo relacional.");
    }
}
