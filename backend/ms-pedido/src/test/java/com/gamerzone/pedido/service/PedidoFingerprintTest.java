package com.gamerzone.pedido.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PedidoFingerprintTest {

    @Test
    void fingerprintIsDeterministicLowercaseHexAndChangesWithTheQuote() {
        String first = PedidoFingerprint.sha256("cotizacion-firmada-1");

        assertEquals(first, PedidoFingerprint.sha256("cotizacion-firmada-1"));
        assertNotEquals(first, PedidoFingerprint.sha256("cotizacion-firmada-2"));
        assertEquals(64, first.length());
        assertTrue(first.matches("[0-9a-f]{64}"));
    }
}
