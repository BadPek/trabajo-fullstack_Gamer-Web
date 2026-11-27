package com.gamerzone.pedido.integration;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import com.gamerzone.pedido.dto.integration.CarritoResponse;
import com.gamerzone.pedido.dto.integration.QuoteValidationResponse;
import com.gamerzone.pedido.dto.integration.ReservationResponse;
import com.gamerzone.pedido.exception.ApiException;
import com.gamerzone.pedido.exception.ExternalDependencyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class HttpCarritoClient implements CarritoClient {

    private final RestClient client;

    public HttpCarritoClient(RestClient.Builder builder,
            @Value("${pedido.integration.carrito-url}") String baseUrl,
            @Value("${pedido.integration.carrito-connect-timeout:3000}") long connectTimeout,
            @Value("${pedido.integration.carrito-read-timeout:5000}") long readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeout));
        this.client = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Override
    public CarritoResponse getCurrentCart(String bearerToken) {
        try {
            CarritoResponse cart = client.get()
                    .uri("/api/v1/cart")
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .retrieve()
                    .body(CarritoResponse.class);
            if (cart == null || cart.items() == null) {
                throw new ExternalDependencyException("ms-carrito respondió sin una lista de productos.");
            }
            return cart;
        } catch (RestClientException exception) {
            throw new ExternalDependencyException("No se pudo consultar el carrito en ms-carrito.", exception);
        }
    }

    @Override
    public QuoteValidationResponse validateQuote(
            String bearerToken,
            long cartVersion,
            String priceQuoteToken) {
        try {
            QuoteValidationResponse response = client.post()
                    .uri("/api/v1/cart/quotes/validate")
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .body(Map.of(
                            "priceQuoteToken", priceQuoteToken,
                            "cartVersion", cartVersion))
                    .retrieve()
                    .body(QuoteValidationResponse.class);
            if (response == null || response.items() == null || response.items().isEmpty()) {
                throw new ExternalDependencyException(
                        "ms-carrito respondió una cotización sin snapshots.");
            }
            return response;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "QUOTE_NOT_FOUND",
                    "La cotización no existe.");
        } catch (HttpClientErrorException.Gone exception) {
            throw new ApiException(HttpStatus.GONE, "QUOTE_EXPIRED",
                    "La cotización expiró.");
        } catch (HttpClientErrorException.Conflict exception) {
            throw new ApiException(HttpStatus.CONFLICT, "CART_VERSION_MISMATCH",
                    "La versión del carrito o el estado de la cotización no coincide.");
        } catch (HttpClientErrorException.UnprocessableEntity exception) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_QUOTE",
                    "La cotización no es válida.");
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden exception) {
            throw new ExternalDependencyException(
                    "ms-carrito rechazó la autenticación para validar la cotización.",
                    exception);
        } catch (RestClientException exception) {
            throw new ExternalDependencyException(
                    "No se pudo validar la cotización en ms-carrito.",
                    exception);
        }
    }

    @Override
    public void consumeQuote(String bearerToken, long cartVersion, String priceQuoteToken) {
        try {
            client.post()
                    .uri("/api/v1/cart/quotes/consume")
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .body(Map.of(
                            "priceQuoteToken", priceQuoteToken,
                            "cartVersion", cartVersion))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict exception) {
            throw new ApiException(HttpStatus.CONFLICT, "QUOTE_ALREADY_USED",
                    "La cotización ya fue utilizada.");
        } catch (HttpClientErrorException.Gone exception) {
            throw new ApiException(HttpStatus.GONE, "QUOTE_EXPIRED",
                    "La cotización expiró.");
        } catch (RestClientException exception) {
            throw new ExternalDependencyException(
                    "No se pudo consumir la cotización en ms-carrito.",
                    exception);
        }
    }

    @Override
    public ReservationResponse createReservation(
            String bearerToken,
            long cartVersion,
            String idempotencyKey) {
        try {
            ReservationResponse response = client.post()
                    .uri("/api/v1/cart/reservations")
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .body(Map.of(
                            "cartVersion", cartVersion,
                            "idempotencyKey", idempotencyKey))
                    .retrieve()
                    .body(ReservationResponse.class);
            if (response == null || response.reservationId() == null
                    || response.reservationId().isBlank()) {
                throw new ExternalDependencyException(
                        "ms-carrito no devolvió un identificador de reserva.");
            }
            return response;
        } catch (HttpClientErrorException.Conflict exception) {
            throw new ApiException(HttpStatus.CONFLICT, "RESERVATION_CONFLICT",
                    "El carrito tiene una reserva activa o la versión no coincide.");
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "CART_NOT_FOUND",
                    "No existe un carrito activo.");
        } catch (RestClientException exception) {
            throw new ExternalDependencyException(
                    "No se pudo crear la reserva en ms-carrito.",
                    exception);
        }
    }

    @Override
    public ReservationResponse completeReservation(String bearerToken, String reservationId) {
        try {
            ReservationResponse response = client.patch()
                    .uri("/api/v1/cart/reservations/{id}/complete", reservationId)
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .retrieve()
                    .body(ReservationResponse.class);
            if (response == null) {
                throw new ExternalDependencyException(
                        "ms-carrito no confirmó la reserva.");
            }
            return response;
        } catch (HttpClientErrorException.Gone exception) {
            throw new ApiException(HttpStatus.GONE, "RESERVATION_EXPIRED",
                    "La reserva expiró.");
        } catch (HttpClientErrorException.Conflict exception) {
            throw new ApiException(HttpStatus.CONFLICT, "RESERVATION_ALREADY_USED",
                    "La reserva ya fue utilizada o liberada.");
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND",
                    "La reserva no existe.");
        } catch (RestClientException exception) {
            throw new ExternalDependencyException(
                    "No se pudo confirmar la reserva en ms-carrito.",
                    exception);
        }
    }

    @Override
    public void releaseReservation(String bearerToken, String reservationId) {
        try {
            client.delete()
                    .uri("/api/v1/cart/reservations/{id}", reservationId)
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ExternalDependencyException(
                    "No se pudo liberar la reserva en ms-carrito.",
                    exception);
        }
    }
}
