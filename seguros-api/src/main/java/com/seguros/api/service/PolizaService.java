package com.seguros.api.service;

import com.seguros.api.config.ApiKeyProperties;
import com.seguros.api.domain.EstadoPoliza;
import com.seguros.api.domain.EstadoRiesgo;
import com.seguros.api.domain.TipoPoliza;
import com.seguros.api.dto.AgregarRiesgoRequest;
import com.seguros.api.dto.PolizaResponse;
import com.seguros.api.dto.RiesgoResponse;
import com.seguros.api.entity.Poliza;
import com.seguros.api.entity.Riesgo;
import com.seguros.api.exception.RecursoNoEncontradoException;
import com.seguros.api.exception.ReglaNegocioException;
import com.seguros.api.repository.PolizaRepository;
import com.seguros.api.repository.RiesgoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolizaService {

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final CoreIntegrationService coreIntegrationService;
    private final ApiKeyProperties apiKeyProperties;

    public PolizaService(
            PolizaRepository polizaRepository,
            RiesgoRepository riesgoRepository,
            CoreIntegrationService coreIntegrationService,
            ApiKeyProperties apiKeyProperties) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
        this.coreIntegrationService = coreIntegrationService;
        this.apiKeyProperties = apiKeyProperties;
    }

    @Transactional(readOnly = true)
    public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
        return polizaRepository.findByFiltros(tipo, estado).stream()
                .map(PolizaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RiesgoResponse> listarRiesgos(Long polizaId) {
        obtenerPoliza(polizaId);
        return riesgoRepository.findByPoliza_IdOrderById(polizaId).stream()
                .map(RiesgoResponse::from)
                .toList();
    }

    @Transactional
    public PolizaResponse renovar(Long polizaId) {
        Poliza poliza = obtenerPoliza(polizaId);
        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se puede renovar una póliza cancelada");
        }

        BigDecimal factor = factorIpc();
        poliza.setCanonMensual(aplicarIpc(poliza.getCanonMensual(), factor));
        poliza.setPrima(aplicarIpc(poliza.getPrima(), factor));
        poliza.setEstado(EstadoPoliza.RENOVADA);

        List<Riesgo> activos =
                riesgoRepository.findByPoliza_IdAndEstado(polizaId, EstadoRiesgo.ACTIVO);
        for (Riesgo riesgo : activos) {
            riesgo.setCanonMensual(aplicarIpc(riesgo.getCanonMensual(), factor));
            riesgo.setPrima(aplicarIpc(riesgo.getPrima(), factor));
        }

        polizaRepository.save(poliza);
        coreIntegrationService.notificarActualizacion(polizaId);
        return PolizaResponse.from(poliza);
    }

    @Transactional
    public PolizaResponse cancelar(Long polizaId) {
        Poliza poliza = obtenerPoliza(polizaId);
        poliza.setEstado(EstadoPoliza.CANCELADA);
        for (Riesgo riesgo : riesgoRepository.findByPoliza_IdOrderById(polizaId)) {
            riesgo.setEstado(EstadoRiesgo.CANCELADO);
        }
        polizaRepository.save(poliza);
        coreIntegrationService.notificarActualizacion(polizaId);
        return PolizaResponse.from(poliza);
    }

    @Transactional
    public RiesgoResponse agregarRiesgo(Long polizaId, AgregarRiesgoRequest request) {
        Poliza poliza = obtenerPoliza(polizaId);
        if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
            throw new ReglaNegocioException("Solo las pólizas colectivas permiten agregar riesgos");
        }
        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se puede agregar un riesgo a una póliza cancelada");
        }

        BigDecimal primaRiesgo =
                request.canonMensual()
                        .multiply(BigDecimal.valueOf(poliza.getMesesVigencia()))
                        .setScale(2, RoundingMode.HALF_UP);

        Riesgo riesgo = new Riesgo();
        riesgo.setPoliza(poliza);
        riesgo.setEstado(EstadoRiesgo.ACTIVO);
        riesgo.setIdentificadorInmueble(request.identificadorInmueble());
        riesgo.setDireccion(request.direccion());
        riesgo.setCanonMensual(request.canonMensual().setScale(2, RoundingMode.HALF_UP));
        riesgo.setPrima(primaRiesgo);
        riesgoRepository.save(riesgo);

        recalcularAgregados(poliza);
        polizaRepository.save(poliza);
        coreIntegrationService.notificarActualizacion(polizaId);
        return RiesgoResponse.from(riesgo);
    }

    @Transactional
    public void recalcularAgregados(Poliza poliza) {
        List<Riesgo> activos =
                riesgoRepository.findByPoliza_IdAndEstado(poliza.getId(), EstadoRiesgo.ACTIVO);
        BigDecimal canon = BigDecimal.ZERO;
        BigDecimal prima = BigDecimal.ZERO;
        for (Riesgo riesgo : activos) {
            canon = canon.add(riesgo.getCanonMensual());
            prima = prima.add(riesgo.getPrima());
        }
        poliza.setCanonMensual(canon.setScale(2, RoundingMode.HALF_UP));
        poliza.setPrima(prima.setScale(2, RoundingMode.HALF_UP));
    }

    Poliza obtenerPoliza(Long polizaId) {
        return polizaRepository
                .findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Póliza no encontrada: " + polizaId));
    }

    private BigDecimal factorIpc() {
        return BigDecimal.valueOf(1 + apiKeyProperties.ipc());
    }

    private BigDecimal aplicarIpc(BigDecimal valor, BigDecimal factor) {
        return valor.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
