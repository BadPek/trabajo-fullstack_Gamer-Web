package com.gamerzone.pedido.exception;

public class ExternalDependencyException extends RuntimeException {

    public ExternalDependencyException(String message) {
        super(message);
    }

    public ExternalDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
