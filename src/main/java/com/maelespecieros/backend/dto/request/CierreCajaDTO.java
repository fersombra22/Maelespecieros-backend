package com.maelespecieros.backend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CierreCajaDTO(

        @DecimalMin(value = "0.0", inclusive = true, message = "El monto final no puede ser negativo.")
        BigDecimal montoFinal,

        @DecimalMin(value = "0.0", inclusive = true, message = "El efectivo contado no puede ser negativo.")
        BigDecimal montoEfectivo,

        @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.")
        String observaciones

) {
}
