package cl.gamerzone.catalogo.exception;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import cl.gamerzone.catalogo.dto.product.MediaPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                        validationCode(error.getCode()),
                        error.getDefaultMessage() == null ? "El valor no es válido." : error.getDefaultMessage())));

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
                exception.getName(),
                "INVALID_VALUE",
                "El parámetro no tiene un formato válido.");
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "La solicitud contiene un parámetro inválido.", request, List.of(fieldError));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = new ArrayList<>();
        exception.getConstraintViolations().forEach(violation ->
                fieldErrors.add(new ApiFieldError(
                        violation.getPropertyPath().toString(),
                        validationCode(violation.getConstraintDescriptor().getAnnotation().annotationType()),
                        violation.getMessage())));

        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "La solicitud contiene campos inválidos.", request, fieldErrors);
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
        if (status.value() == HttpStatus.NOT_FOUND.value()) {
            return problem(status, "RESOURCE_NOT_FOUND",
                    "No se encontró el recurso solicitado.", request, List.of());
        }
        if (status.is5xxServerError()) {
            String code = status.value() == HttpStatus.NOT_IMPLEMENTED.value() ? "NOT_IMPLEMENTED"
                    : status.value() == HttpStatus.INTERNAL_SERVER_ERROR.value() ? "INTERNAL_ERROR" : "SERVER_ERROR";
            String detail = status.value() == HttpStatus.NOT_IMPLEMENTED.value()
                    ? "Esta funcionalidad aún no está disponible."
                    : "Ocurrió un error interno. Inténtalo más tarde.";
            return problem(status, code, detail, request, List.of());
        }
        return problem(status, "REQUEST_ERROR",
                "No fue posible procesar la solicitud.", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {
        logger.error("Unexpected error while processing request {}", request.getRequestURI(), exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrió un error interno. Inténtalo más tarde.", request, List.of());
    }

    private String validationCode(String constraintCode) {
        if (constraintCode == null) {
            return "INVALID_VALUE";
        }
        String code = constraintCode.contains(".")
                ? constraintCode.substring(0, constraintCode.indexOf('.'))
                : constraintCode;
        return switch (code) {
            case "NotBlank", "NotNull" -> "REQUIRED_FIELD";
            case "Size" -> "INVALID_LENGTH";
            case "Positive" -> "INVALID_VALUE";
            case "MediaPath" -> "INVALID_FORMAT";
            default -> "INVALID_VALUE";
        };
    }

    private String validationCode(Class<? extends Annotation> constraintType) {
        if (constraintType == NotBlank.class || constraintType == NotNull.class) {
            return "REQUIRED_FIELD";
        }
        if (constraintType == Size.class) {
            return "INVALID_LENGTH";
        }
        if (constraintType == Positive.class) {
            return "INVALID_VALUE";
        }
        if (constraintType == MediaPath.class) {
            return "INVALID_FORMAT";
        }
        return "INVALID_VALUE";
    }

    private ResponseEntity<ApiError> problem(
            HttpStatusCode status,
            String code,
            String detail,
            HttpServletRequest request,
            List<ApiFieldError> fieldErrors) {
        ApiError body = new ApiError(
                "about:blank",
                spanishTitle(status),
                status.value(),
                detail,
                request.getRequestURI(),
                code,
                fieldErrors);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, status);
    }

    private String spanishTitle(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            return statusCode.is4xxClientError() ? "Error en la solicitud" : "Error del servidor";
        }
        return switch (status) {
            case BAD_REQUEST -> "Solicitud incorrecta";
            case NOT_FOUND -> "Recurso no encontrado";
            case METHOD_NOT_ALLOWED -> "Método no permitido";
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo de contenido no soportado";
            case NOT_IMPLEMENTED -> "Funcionalidad no implementada";
            case INTERNAL_SERVER_ERROR -> "Error interno del servidor";
            default -> status.is4xxClientError() ? "Error en la solicitud" : "Error del servidor";
        };
    }
}
