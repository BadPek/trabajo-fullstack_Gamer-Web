package com.gamerzone.auth.exception;

public record ApiFieldError(String field, String code, String message) {
}
