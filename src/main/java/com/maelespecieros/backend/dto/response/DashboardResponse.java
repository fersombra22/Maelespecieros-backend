package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(

        Long totalProductos,

        Long productosActivos,

        Long productosStockBajo,

        Long totalVentas,

        BigDecimal totalFacturado,
        
        BigDecimal porcentajeVariacionMensual,
        
        List<TopProductoResponse> topProductos,
        
        List<VentasPorMetodoPagoResponse> ventasPorMetodoPago

) {
}