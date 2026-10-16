package com.seguros.api.dto;

public record CoreEventoResponse(
        String status, String mensaje, String evento, Long polizaId) {}
