package com.gamerzone.pedido.integration;

import com.gamerzone.pedido.dto.integration.CarritoResponse;
import com.gamerzone.pedido.exception.ExternalDependencyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpCarritoClient implements CarritoClient {

    private final RestClient client;

    public HttpCarritoClient(RestClient.Builder builder,
            @Value("${pedido.integration.carrito-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    @Override
    public CarritoResponse getCurrentCart(String bearerToken) {
        try {
            CarritoResponse cart = client.get()
                    .uri("/api/v1/carrito")
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
}
