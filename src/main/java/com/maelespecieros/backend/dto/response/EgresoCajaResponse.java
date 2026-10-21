package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.maelespecieros.backend.entities.TipoMedioEgreso;

public record EgresoCajaResponse(
        Long id,
        Long cajaId,
        BigDecimal monto,
        TipoMedioEgreso tipoMedio,
        String motivo,
        String comprobante,
        LocalDateTime fecha,
        Long usuarioId,
        String usuarioNombre
) {}
