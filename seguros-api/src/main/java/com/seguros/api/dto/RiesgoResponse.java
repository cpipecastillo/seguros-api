package com.seguros.api.dto;

import com.seguros.api.domain.EstadoRiesgo;
import com.seguros.api.entity.Riesgo;
import java.math.BigDecimal;

public record RiesgoResponse(
        Long id,
        Long polizaId,
        EstadoRiesgo estado,
        String identificadorInmueble,
        String direccion,
        BigDecimal canonMensual,
        BigDecimal prima) {

    public static RiesgoResponse from(Riesgo riesgo) {
        return new RiesgoResponse(
                riesgo.getId(),
                riesgo.getPolizaId(),
                riesgo.getEstado(),
                riesgo.getIdentificadorInmueble(),
                riesgo.getDireccion(),
                riesgo.getCanonMensual(),
                riesgo.getPrima());
    }
}
