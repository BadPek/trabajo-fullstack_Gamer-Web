package cl.gamerzone.catalogo.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.http.HttpServletRequest;

import cl.gamerzone.catalogo.dto.product.ProductCreateRequest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void validationErrorsUseStableFieldCodesAndProblemMediaType() throws Exception {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "request");
        bindingResult.rejectValue("titulo", "NotBlank", "El título es obligatorio.");
        bindingResult.rejectValue("categoria", "Size", "La categoría es demasiado larga.");
        bindingResult.rejectValue("precio", "Positive", "El precio debe ser positivo.");
        bindingResult.rejectValue("imagen", "MediaPath", "La ruta no es válida.");
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                methodParameter(), bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(exception, request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertEquals("REQUIRED_FIELD", response.getBody().fieldErrors().get(0).code());
        assertEquals("INVALID_LENGTH", response.getBody().fieldErrors().get(1).code());
        assertEquals("INVALID_VALUE", response.getBody().fieldErrors().get(2).code());
        assertEquals("INVALID_FORMAT", response.getBody().fieldErrors().get(3).code());
    }

    @Test
    void commonHttpErrorsHaveExpectedCodesAndEmptyFieldErrors() {
        assertProblem(handler.handleUnreadableBody(request()), HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY");
        assertProblem(handler.handleNoResourceFound(request()), HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
        assertProblem(handler.handleMethodNotAllowed(request()), HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED");
        assertProblem(handler.handleUnsupportedMediaType(request()), HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED_MEDIA_TYPE");
        assertProblem(handler.handleUnexpected(new Exception("internal details"), request()),
                HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }

    @Test
    void responseStatusWithNonEnumCodeDoesNotRequireHttpStatusValueOf() {
        ResponseEntity<ApiError> response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatusCode.valueOf(599), "private detail"),
                request());

        assertEquals(599, response.getStatusCode().value());
        assertEquals("SERVER_ERROR", response.getBody().code());
        assertEquals("Ocurrió un error interno. Inténtalo más tarde.", response.getBody().detail());
    }

    private void assertProblem(
            ResponseEntity<ApiError> response,
            HttpStatus expectedStatus,
            String expectedCode) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
        assertEquals(expectedCode, response.getBody().code());
        assertEquals(0, response.getBody().fieldErrors().size());
    }

    private HttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/products");
        return request;
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        return new MethodParameter(
                GlobalExceptionHandlerTests.class.getDeclaredMethod(
                        "sampleRequest", ProductCreateRequest.class),
                0);
    }

    @SuppressWarnings("unused")
    private void sampleRequest(ProductCreateRequest request) {
    }
}
