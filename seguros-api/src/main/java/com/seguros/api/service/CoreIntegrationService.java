package com.seguros.api.service;

import com.seguros.api.config.ApiKeyFilter;
import com.seguros.api.config.ApiKeyProperties;
import com.seguros.api.dto.CoreEventoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class CoreIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(CoreIntegrationService.class);

    private final RestClient restClient;
    private final ApiKeyProperties apiKeyProperties;
    private final int serverPort;

    public CoreIntegrationService(
            RestClient restClient, ApiKeyProperties apiKeyProperties, @Value("${server.port}") int serverPort) {
        this.restClient = restClient;
        this.apiKeyProperties = apiKeyProperties;
        this.serverPort = serverPort;
    }

    public void notificarActualizacion(Long polizaId) {
        var body = new CoreEventoRequest("ACTUALIZACION", polizaId);
        try {
            restClient
                    .post()
                    .uri("http://localhost:" + serverPort + "/core-mock/evento")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(ApiKeyFilter.API_KEY_HEADER, apiKeyProperties.apiKey())
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("core-mock invocado correctamente para polizaId={}", polizaId);
        } catch (RestClientException ex) {
            log.warn("No se pudo invocar core-mock para polizaId={}: {}", polizaId, ex.getMessage());
        }
    }
}
