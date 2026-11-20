package com.gamerzone.pedido.integration;

import com.gamerzone.pedido.dto.integration.ProductoResponse;
import com.gamerzone.pedido.exception.ApiException;
import com.gamerzone.pedido.exception.ExternalDependencyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpCatalogoClient implements CatalogoClient {

    private final RestClient client;

    public HttpCatalogoClient(RestClient.Builder builder,
            @Value("${pedido.integration.catalogo-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    @Override
    public ProductoResponse findProduct(String productId) {
        try {
            ProductoResponse product = client.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .body(ProductoResponse.class);
            if (product == null || product.id() == null || product.precio() == null
                    || product.titulo() == null || product.titulo().isBlank()) {
                throw new ExternalDependencyException("ms-catalogo respondió con datos de producto incompletos.");
            }
            return product;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "PRODUCT_NOT_FOUND",
                    "El carrito contiene un producto que no existe en el catálogo.");
        } catch (RestClientException exception) {
            throw new ExternalDependencyException("No se pudo consultar el producto en ms-catalogo.", exception);
        }
    }
}
