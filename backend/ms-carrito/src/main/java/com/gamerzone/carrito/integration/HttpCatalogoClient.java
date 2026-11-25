package com.gamerzone.carrito.integration;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class HttpCatalogoClient implements CatalogoClient {

    private final RestClient client;

    public HttpCatalogoClient(
            RestClient.Builder builder,
            @Value("${carrito.integration.catalogo-url}") String baseUrl,
            @Value("${carrito.integration.catalogo-connect-timeout:3000}") long connectTimeout,
            @Value("${carrito.integration.catalogo-read-timeout:5000}") long readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeout));
        this.client = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public CatalogoProductResponse findProduct(String productId) {
        try {
            CatalogoProductResponse product = client.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .body(CatalogoProductResponse.class);
            if (product == null || product.id() == null || product.id().isBlank()
                    || product.titulo() == null || product.titulo().isBlank()
                    || product.precio() == null || product.precio() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "ms-catalogo respondió con datos de producto incompletos.");
            }
            return product;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El producto no existe o está archivado.",
                    exception);
        } catch (HttpClientErrorException.BadRequest exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El identificador de producto no es válido.",
                    exception);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "ms-catalogo rechazó la consulta.",
                    exception);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo consultar ms-catalogo.",
                    exception);
        }
    }
}
