package com.maelespecieros.backend.dto.response;

public record AnomaliaAuditoriaResponse(
    String entidad,
    String identificador,
    String camposAfectados,
    String mecanismoSeguridad,
    String tipoAnomalia,
    String descripcion
) {}
