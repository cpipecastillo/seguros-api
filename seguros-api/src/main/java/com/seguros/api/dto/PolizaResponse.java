package com.seguros.api.dto;

import com.seguros.api.domain.EstadoPoliza;
import com.seguros.api.domain.TipoPoliza;
import com.seguros.api.entity.Poliza;
import java.math.BigDecimal;

public record PolizaResponse(
        Long id,
        TipoPoliza tipo,
        EstadoPoliza estado,
        int mesesVigencia,
        BigDecimal canonMensual,
        BigDecimal prima) {

    public static PolizaResponse from(Poliza poliza) {
        return new PolizaResponse(
                poliza.getId(),
                poliza.getTipo(),
                poliza.getEstado(),
                poliza.getMesesVigencia(),
                poliza.getCanonMensual(),
                poliza.getPrima());
    }
}
