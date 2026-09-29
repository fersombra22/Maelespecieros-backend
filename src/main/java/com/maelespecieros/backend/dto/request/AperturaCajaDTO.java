package com.maelespecieros.backend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AperturaCajaDTO(

        @NotNull(message = "El monto inicial es obligatorio.")
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto inicial no puede ser negativo.")
        BigDecimal montoInicial,

        @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.")
        String observaciones

) {
}
