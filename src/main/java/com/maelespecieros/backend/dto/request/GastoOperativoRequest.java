package com.maelespecieros.backend.dto.request;

import java.math.BigDecimal;

import com.maelespecieros.backend.entities.CategoriaGasto;
import com.maelespecieros.backend.entities.FormaPago;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GastoOperativoRequest(

        @NotNull(message = "El monto es obligatorio.")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero.")
        BigDecimal monto,

        @NotBlank(message = "El concepto o motivo del gasto es obligatorio.")
        @Size(max = 250, message = "El concepto no puede superar los 250 caracteres.")
        String concepto,

        @NotNull(message = "La categoría del gasto es obligatoria.")
        CategoriaGasto categoriaGasto,

        @NotNull(message = "La forma de pago es obligatoria.")
        FormaPago formaPago,

        @Size(max = 50, message = "El número de comprobante no puede superar los 50 caracteres.")
        String comprobanteNro

) {
}
