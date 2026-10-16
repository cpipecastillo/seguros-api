package com.seguros.api.controller;

import com.seguros.api.dto.CoreEventoRequest;
import com.seguros.api.dto.CoreEventoResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    private static final Logger log = LoggerFactory.getLogger(CoreMockController.class);

    @PostMapping("/evento")
    public CoreEventoResponse registrarEvento(@Valid @RequestBody CoreEventoRequest request) {
        log.info("[CORE-MOCK] Intento de envio al CORE: evento={} polizaId={}", request.evento(), request.polizaId());
        return new CoreEventoResponse(
                "OK",
                "Evento registrado en el log de la consola donde corre mvn spring-boot:run",
                request.evento(),
                request.polizaId());
    }
}
