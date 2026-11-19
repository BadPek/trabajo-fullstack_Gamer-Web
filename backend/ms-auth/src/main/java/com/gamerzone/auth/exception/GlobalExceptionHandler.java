package com.gamerzone.auth.exception;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ApiFieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiFieldError(error.getField(),
                        error.getCode() == null ? "INVALID_VALUE" : error.getCode(),
                        error.getDefaultMessage() == null ? "El valor no es válido." : error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "La solicitud contiene campos inválidos.",
                request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception,
            HttpServletRequest request) {
        List<ApiFieldError> errors = exception.getConstraintViolations().stream()
                .map(error -> new ApiFieldError(error.getPropertyPath().toString(), "INVALID_VALUE", error.getMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "La solicitud contiene campos inválidos.",
                request, errors);
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handleApiException(ApiException exception, HttpServletRequest request) {
        return response(exception.status(), exception.code(), exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadableBody(HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
                "El cuerpo de la solicitud no es válido.", request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> handleMethodNotAllowed(HttpServletRequest request) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "El método HTTP no está permitido para este recurso.", request, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> handleUnsupportedMediaType(HttpServletRequest request) {
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "El tipo de contenido enviado no está permitido.", request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleDuplicate(DataIntegrityViolationException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "ACCOUNT_CONFLICT",
                "El correo o el RUT ya están registrados.", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Error procesando {}", request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrió un error interno. Inténtalo más tarde.", request, List.of());
    }

    private static ResponseEntity<ApiError> response(HttpStatus status, String code, String detail,
            HttpServletRequest request, List<ApiFieldError> errors) {
        String title = status == HttpStatus.UNAUTHORIZED ? "Autenticación requerida"
                : status == HttpStatus.CONFLICT ? "Conflicto"
                : status == HttpStatus.BAD_REQUEST ? "Solicitud incorrecta" : "Error del servidor";
        ApiError body = new ApiError("about:blank", title, status.value(), detail, request.getRequestURI(), code, errors);
        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .body(body);
    }
}
