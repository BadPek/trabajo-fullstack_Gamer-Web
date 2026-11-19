package com.gamerzone.pedido.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class PedidoFingerprintTest {

    @Test
    void fingerprintIsDeterministicAndChangesWithDeliveryDetails() {
        String first = PedidoFingerprint.sha256("Av. España 8");
        assertEquals(first, PedidoFingerprint.sha256("Av. España 8"));
        assertNotEquals(first, PedidoFingerprint.sha256("Av. España 9"));
        assertEquals(64, first.length());
    }
}
