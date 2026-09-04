package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;

public record DetalleVentaResponse(

        Long productoId,

        String codigoProducto,

        String nombreProducto,

        Integer cantidad,

        BigDecimal precioUnitario,

        BigDecimal subtotal

) {
}