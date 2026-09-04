package com.maelespecieros.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DetalleVentaRequest(

        @NotNull
        Long productoId,

        @NotNull
        @Min(1)
        Integer cantidad

) {
}