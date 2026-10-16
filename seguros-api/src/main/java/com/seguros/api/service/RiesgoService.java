package com.seguros.api.service;

import com.seguros.api.domain.EstadoRiesgo;
import com.seguros.api.dto.RiesgoResponse;
import com.seguros.api.entity.Poliza;
import com.seguros.api.entity.Riesgo;
import com.seguros.api.exception.RecursoNoEncontradoException;
import com.seguros.api.exception.ReglaNegocioException;
import com.seguros.api.repository.PolizaRepository;
import com.seguros.api.repository.RiesgoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiesgoService {

    private final RiesgoRepository riesgoRepository;
    private final PolizaRepository polizaRepository;
    private final PolizaService polizaService;
    private final CoreIntegrationService coreIntegrationService;

    public RiesgoService(
            RiesgoRepository riesgoRepository,
            PolizaRepository polizaRepository,
            PolizaService polizaService,
            CoreIntegrationService coreIntegrationService) {
        this.riesgoRepository = riesgoRepository;
        this.polizaRepository = polizaRepository;
        this.polizaService = polizaService;
        this.coreIntegrationService = coreIntegrationService;
    }

    @Transactional
    public RiesgoResponse cancelar(Long riesgoId) {
        Riesgo riesgo =
                riesgoRepository
                        .findById(riesgoId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Riesgo no encontrado: " + riesgoId));
        if (riesgo.getEstado() == EstadoRiesgo.CANCELADO) {
            throw new ReglaNegocioException("El riesgo ya está cancelado");
        }

        riesgo.setEstado(EstadoRiesgo.CANCELADO);
        Poliza poliza = riesgo.getPoliza();
        polizaService.recalcularAgregados(poliza);
        riesgoRepository.save(riesgo);
        polizaRepository.save(poliza);
        coreIntegrationService.notificarActualizacion(poliza.getId());
        return RiesgoResponse.from(riesgo);
    }
}
