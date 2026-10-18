package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.maelespecieros.backend.entities.EstadoVenta;
import com.maelespecieros.backend.entities.FormaPago;

public record VentaResponse(

        Long id,

        String numeroVenta,

        LocalDateTime fecha,

        BigDecimal subtotal,

        BigDecimal descuento,

        BigDecimal total,

        FormaPago formaPago,

        EstadoVenta estado,

        Long usuarioId,

        String usuario,

        Long clienteId,

        String clienteNombreCompleto,

        List<DetalleVentaResponse> detalles

) {
}
