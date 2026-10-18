package com.maelespecieros.backend.dto.request;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AumentoMasivoRequest(
        @NotEmpty(message = "Debe proporcionar al menos un ID de producto")
        List<Long> ids,
        
        @NotNull(message = "El porcentaje no puede ser nulo")
        @Positive(message = "El porcentaje debe ser mayor a 0")
        BigDecimal porcentaje
) {
}
