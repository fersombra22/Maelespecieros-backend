package com.maelespecieros.backend.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.maelespecieros.backend.entities.FormaPago;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record VentaRequest(

        Long clienteId,

        @NotNull
        FormaPago formaPago,

        BigDecimal descuento,

        @Valid
        @NotEmpty
        List<DetalleVentaRequest> detalles

) {
}