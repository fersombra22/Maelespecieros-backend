package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.maelespecieros.backend.entities.EstadoCaja;

public record CajaResponseDTO(
        Long id,
        BigDecimal montoInicial,
        BigDecimal montoFinal,
        BigDecimal montoVentas,
        BigDecimal diferencia,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        EstadoCaja estado,
        String observaciones,
        Long usuarioId,
        String usuarioUsername,
        String usuarioNombreCompleto,
        BigDecimal totalEfectivo,
        BigDecimal totalDebito,
        BigDecimal totalCredito,
        BigDecimal totalTransferencia,
        BigDecimal totalDigital,
        Long cantidadVentas,
        BigDecimal totalEgresos,
        BigDecimal totalEgresosEfectivo
) {
}
