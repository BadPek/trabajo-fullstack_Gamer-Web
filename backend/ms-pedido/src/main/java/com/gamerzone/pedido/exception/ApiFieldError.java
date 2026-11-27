package com.gamerzone.pedido.exception;

public record ApiFieldError(String field, String code, String message) {
}
