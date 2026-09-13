package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;

public record TopProductoResponse(
        String nombre,
        Long cantidadVendida,
        BigDecimal totalGenerado
) {
}
