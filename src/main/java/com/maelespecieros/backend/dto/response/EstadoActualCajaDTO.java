package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;

public record EstadoActualCajaDTO(
        boolean abierta,
        CajaResponseDTO caja,
        BigDecimal montoVentasActual,
        BigDecimal montoEsperadoActual,
        BigDecimal totalEfectivoActual,
        BigDecimal totalDebitoActual,
        BigDecimal totalCreditoActual,
        BigDecimal totalTransferenciaActual,
        BigDecimal totalDigitalActual,
        Long cantidadVentasActual
) {
}
