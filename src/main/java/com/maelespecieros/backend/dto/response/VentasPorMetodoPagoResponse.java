package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;
import com.maelespecieros.backend.entities.FormaPago;

public record VentasPorMetodoPagoResponse(
        FormaPago formaPago,
        BigDecimal total
) {
}
