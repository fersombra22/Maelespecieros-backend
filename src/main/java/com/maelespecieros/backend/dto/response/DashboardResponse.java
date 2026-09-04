package com.maelespecieros.backend.dto.response;

import java.math.BigDecimal;

public record DashboardResponse(

        Long totalProductos,

        Long productosActivos,

        Long productosStockBajo,

        Long totalVentas,

        BigDecimal totalFacturado

) {
}