package com.seguros.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AgregarRiesgoRequest(
        @NotNull @DecimalMin(value = "0.01", message = "canonMensual debe ser mayor que cero")
                BigDecimal canonMensual,
        @NotBlank String identificadorInmueble,
        @NotBlank String direccion) {}
