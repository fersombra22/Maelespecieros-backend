package com.maelespecieros.backend.dto.response;

import java.time.LocalDateTime;

import com.maelespecieros.backend.entities.TipoMovimiento;

public record MovimientoStockResponse(

        Long id,

        String codigoProducto,

        String producto,

        TipoMovimiento tipoMovimiento,

        Integer cantidad,

        Integer stockAnterior,

        Integer stockNuevo,

        String motivo,

        LocalDateTime fecha

) {
}