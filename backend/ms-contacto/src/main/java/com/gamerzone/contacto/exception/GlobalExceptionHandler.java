package com.gamerzone.contacto.exception;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.add(new ApiFieldError(
                        error.getField(),
                        error.getCode() == null ? "INVALID_VALUE" : error.getCode(),
                        error.getDefaultMessage() == null ? "El valor no es válido." : error.getDefaultMessage())));
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "La solicitud contiene campos inválidos.", request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = new ArrayList<>();
        exception.getConstraintViolations().forEach(violation ->
                fieldErrors.add(new ApiFieldError(
                        violation.getPropertyPath().toString(),
                        violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                        violation.getMessage())));
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "La solicitud contiene campos inválidos.", request, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
                "El cuerpo de la solicitud no es válido.", request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleInvalidParameter(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        ApiFieldError fieldError = new ApiFieldError(
                exception.getName(), "INVALID_VALUE", "El parámetro no tiene un formato válido.");
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "La solicitud contiene un parámetro inválido.", request, List.of(fieldError));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                "No se encontró el recurso solicitado.", request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(HttpServletRequest request) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "El método HTTP no está permitido para este recurso.", request, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleUnsupportedMediaType(HttpServletRequest request) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "El tipo de contenido enviado no está permitido.", request, List.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request) {
        HttpStatusCode status = exception.getStatusCode();
        String code = status.value() == HttpStatus.NOT_IMPLEMENTED.value()
                ? "PERSISTENCE_NOT_CONFIGURED"
                : status.value() == HttpStatus.NOT_FOUND.value() ? "RESOURCE_NOT_FOUND" : "REQUEST_ERROR";
        String detail = status.value() == HttpStatus.NOT_IMPLEMENTED.value()
                ? "La operación depende de la aprobación del modelo relacional."
                : status.value() == HttpStatus.NOT_FOUND.value()
                        ? "No se encontró el recurso solicitado."
                        : "No fue posible procesar la solicitud.";
        return problem(status, code, detail, request, List.of());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handlePersistence(
            DataAccessException exception,
            HttpServletRequest request) {
        logger.error("Persistence error while processing request {}", request.getRequestURI(), exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "PERSISTENCE_ERROR",
                "Ocurrió un error interno. Inténtalo más tarde.", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {
        logger.error("Unexpected error while processing request {}", request.getRequestURI(), exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrió un error interno. Inténtalo más tarde.", request, List.of());
    }

    private ResponseEntity<ApiError> problem(
            HttpStatusCode status,
            String code,
            String detail,
            HttpServletRequest request,
            List<ApiFieldError> fieldErrors) {
        String title = status.value() == HttpStatus.NOT_IMPLEMENTED.value()
                ? "Funcionalidad no implementada"
                : status.value() == HttpStatus.BAD_REQUEST.value() ? "Solicitud incorrecta"
                : status.value() == HttpStatus.NOT_FOUND.value() ? "Recurso no encontrado"
                : status.value() == HttpStatus.UNAUTHORIZED.value() ? "Autenticación requerida"
                : status.value() == HttpStatus.FORBIDDEN.value() ? "Acceso denegado"
                : status.is4xxClientError() ? "Error en la solicitud" : "Error del servidor";
        ApiError body = new ApiError(
                "about:blank", title, status.value(), detail, request.getRequestURI(), code, fieldErrors);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, status);
    }
}
