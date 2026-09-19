package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;

public record ComparacionVentasResponse(
    BigDecimal actual,
    BigDecimal anterior,
    BigDecimal porcentajeVariacion
) {
}
