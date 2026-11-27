package com.gamerzone.auth.service;

import com.gamerzone.auth.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ContactMessageCleanupClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactMessageCleanupClient.class);

    private final RestClient restClient;

    public ContactMessageCleanupClient(
            @Value("${auth.contact.base-url:http://localhost:8085}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void deleteForUser(String userId, String accessToken) {
        try {
            restClient.delete()
                    .uri("/api/v1/admin/contact-messages/by-user/{userId}", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            LOGGER.warn("No se pudieron eliminar mensajes de contacto para la cuenta {}", userId, exception);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "CONTACT_CLEANUP_FAILED",
                    "No fue posible eliminar los mensajes de contacto asociados. La cuenta no fue dada de baja.",
                    exception);
        }
    }
}
