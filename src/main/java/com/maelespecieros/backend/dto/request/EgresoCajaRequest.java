package com.maelespecieros.backend.dto.request;

import java.math.BigDecimal;

import com.maelespecieros.backend.entities.TipoMedioEgreso;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EgresoCajaRequest(
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal monto,

        @NotNull(message = "El medio de egreso (EFECTIVO o TRANSFERENCIA) es obligatorio")
        TipoMedioEgreso tipoMedio,

        @NotBlank(message = "El motivo o concepto del egreso es obligatorio")
        @Size(max = 255, message = "El motivo no puede exceder los 255 caracteres")
        String motivo,

        @Size(max = 100, message = "El comprobante no puede exceder los 100 caracteres")
        String comprobante
) {}
