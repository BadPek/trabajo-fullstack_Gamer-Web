package com.gamerzone.pedido.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PedidoFingerprint {

    private PedidoFingerprint() {
    }

    /** SHA-256 en hexadecimal (64 caracteres), usado como huella de la cotización del carrito. */
    public static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible en esta JVM.", exception);
        }
    }
}
