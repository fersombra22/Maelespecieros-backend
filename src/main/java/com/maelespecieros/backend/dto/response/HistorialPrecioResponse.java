package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistorialPrecioResponse(
        Long id,
        Long productoId,
        BigDecimal precioAnterior,
        BigDecimal precioNuevo,
        LocalDateTime fechaCambio,
        String usuarioResponsable
) {
}
