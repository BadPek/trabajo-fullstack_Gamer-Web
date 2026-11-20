package com.gamerzone.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RutValidatorTest {

    private final RutValidator validator = new RutValidator();

    @Test
    void acceptsValidRutWithFormattingAndNormalizesIt() {
        assertTrue(validator.isValid("19.011.022-2"));
        assertEquals("190110222", validator.normalize("19.011.022-2"));
        assertEquals("12345678K", validator.normalize(" 12.345.678-k "));
    }

    @Test
    void rejectsIncorrectCheckDigitAndMalformedInput() {
        assertFalse(validator.isValid("19011022-3"));
        assertFalse(validator.isValid("not-a-rut"));
    }
}
