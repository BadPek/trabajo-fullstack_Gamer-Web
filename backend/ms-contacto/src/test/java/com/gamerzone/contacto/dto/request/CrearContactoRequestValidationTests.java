package com.gamerzone.contacto.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CrearContactoRequestValidationTests {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void acceptsSubjectAtTheMaximumLength() {
        var request = new CrearContactoRequest("Nombre", "persona@gmail.com", "a".repeat(120), "Mensaje");

        assertTrue(validator.validate(request).stream()
                .noneMatch(error -> error.getPropertyPath().toString().equals("asunto")));
    }

    @Test
    void rejectsSubjectLongerThan120Characters() {
        var request = new CrearContactoRequest("Nombre", "persona@gmail.com", "a".repeat(121), "Mensaje");

        var error = validator.validate(request).stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("asunto"))
                .findFirst()
                .orElseThrow();

        assertEquals("El asunto no puede superar los 120 caracteres.", error.getMessage());
    }
}
