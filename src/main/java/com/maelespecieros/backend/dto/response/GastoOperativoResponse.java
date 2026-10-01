package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.maelespecieros.backend.entities.CategoriaGasto;
import com.maelespecieros.backend.entities.FormaPago;

public record GastoOperativoResponse(
        Long id,
        BigDecimal monto,
        String concepto,
        CategoriaGasto categoriaGasto,
        FormaPago formaPago,
        String comprobanteNro,
        LocalDateTime fecha,
        Boolean anulado,
        Long cajaId,
        Long usuarioId,
        String usuarioUsername,
        String usuarioNombreCompleto
) {
}
