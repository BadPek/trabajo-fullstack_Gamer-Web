package com.gamerzone.contacto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.gamerzone.contacto.dto.request.CrearContactoRequest;
import com.gamerzone.contacto.entity.ContactoMensaje;
import com.gamerzone.contacto.repository.ContactoMensajeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ContactoServiceTest {

    private static final String MESSAGE_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private ContactoMensajeRepository repository;

    private ContactoService service;

    @BeforeEach
    void setUp() {
        service = new ContactoService(repository);
    }

    @Test
    void registerNormalizesEmailAndAssignsReceivedStatus() {
        when(repository.saveAndFlush(any(ContactoMensaje.class)))
                .thenAnswer(invocation -> {
                    ContactoMensaje message = invocation.getArgument(0);
                    ReflectionTestUtils.setField(message, "id", MESSAGE_ID);
                    return message;
                });

        var response = service.create(new CrearContactoRequest(
                "  Sara Morales  ",
                "  SARA@GMAIL.COM  ",
                "  Consulta  ",
                "  Mensaje de prueba  "));

        assertEquals("Sara Morales", response.nombre());
        assertEquals("sara@gmail.com", response.correo());
        assertEquals("Consulta", response.asunto());
        assertEquals("Mensaje de prueba", response.mensaje());
        assertEquals("RECIBIDO", response.estado());
        verify(repository).saveAndFlush(any(ContactoMensaje.class));
    }

    @Test
    void listMessagesReturnsPageOfResponses() {
        ContactoMensaje message = new ContactoMensaje(
                "Sara Morales",
                "sara@gmail.com",
                "Consulta",
                "Mensaje");
        ReflectionTestUtils.setField(message, "id", MESSAGE_ID);
        when(repository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(message)));

        var page = service.findAll(0, 20);

        assertEquals(1, page.getContent().size());
        assertEquals(MESSAGE_ID, page.getContent().get(0).id());
    }

    @Test
    void findMessageReturnsNotFoundWhenItDoesNotExist() {
        when(repository.findById(MESSAGE_ID)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.findById(MESSAGE_ID));

        assertEquals(404, error.getStatusCode().value());
    }
}
