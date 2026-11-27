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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ContactoServiceTest {

    private static final String MESSAGE_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String USER_ID = "11111111-1111-4111-8111-111111111111";

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
                "  Mensaje de prueba  "), null);

        assertEquals("Sara Morales", response.nombre());
        assertEquals("sara@gmail.com", response.correo());
        assertEquals("Consulta", response.asunto());
        assertEquals("Mensaje de prueba", response.mensaje());
        assertEquals("RECIBIDO", response.estado());
        verify(repository).saveAndFlush(any(ContactoMensaje.class));
    }

    @Test
    void authenticatedContactMessageIsAssociatedWithNormalizedUserId() {
        when(repository.saveAndFlush(any(ContactoMensaje.class)))
                .thenAnswer(invocation -> {
                    ContactoMensaje message = invocation.getArgument(0);
                    ReflectionTestUtils.setField(message, "id", MESSAGE_ID);
                    return message;
                });

        var response = service.create(new CrearContactoRequest(
                "Sara Morales", "sara@gmail.com", "Consulta", "Mensaje"), USER_ID.toUpperCase());

        assertEquals(USER_ID, response.userId());
    }

    @Test
    void listMessagesReturnsPageOfResponses() {
        ContactoMensaje message = new ContactoMensaje(
                "Sara Morales",
                "sara@gmail.com",
                "Consulta",
                "Mensaje");
        ReflectionTestUtils.setField(message, "id", MESSAGE_ID);
        when(repository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(message)));

        var page = service.findAll(0, 20, null, null, null);

        assertEquals(1, page.content().size());
        assertEquals(MESSAGE_ID, page.content().get(0).id());
    }

    @Test
    void deleteMessageReturnsNotFoundWhenItDoesNotExist() {
        when(repository.findById(MESSAGE_ID)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.delete(MESSAGE_ID));

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void deleteMessageRemovesItFromRepository() {
        ContactoMensaje message = new ContactoMensaje(
                "Sara Morales",
                "sara@gmail.com",
                "Consulta",
                "Mensaje");
        ReflectionTestUtils.setField(message, "id", MESSAGE_ID);
        when(repository.findById(MESSAGE_ID)).thenReturn(Optional.of(message));

        service.delete(MESSAGE_ID);

        verify(repository).delete(message);
        verify(repository).flush();
    }

    @Test
    void deletesAllContactMessagesForAnonymizedAccount() {
        service.deleteForUser(USER_ID.toUpperCase());

        verify(repository).deleteByUserId(USER_ID);
    }
}
