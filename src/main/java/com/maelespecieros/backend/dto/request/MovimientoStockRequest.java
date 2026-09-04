package com.maelespecieros.backend.dto.request;

import com.maelespecieros.backend.entities.TipoMovimiento;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovimientoStockRequest(

        @NotNull
        Long productoId,

        @NotNull
        TipoMovimiento tipoMovimiento,

        @NotNull
        Integer cantidad,

        @Size(max = 250)
        String motivo

) {
}