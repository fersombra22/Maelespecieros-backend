package com.maelespecieros.backend.services.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maelespecieros.backend.dto.response.DashboardResponse;
import com.maelespecieros.backend.entities.EstadoVenta;
import com.maelespecieros.backend.repositories.ProductoRepository;
import com.maelespecieros.backend.repositories.VentaRepository;
import com.maelespecieros.backend.services.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    public DashboardServiceImpl(
            ProductoRepository productoRepository,
            VentaRepository ventaRepository) {

        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    public DashboardResponse obtenerDashboard() {

        long totalProductos = productoRepository.count();

        long productosActivos =
                productoRepository.countByActivoTrue();

        long stockBajo =
                productoRepository.countByStockLessThanEqualAndActivoTrue(5);

        long totalVentas =
                ventaRepository.countByEstado(
                        EstadoVenta.COMPLETADA);

        BigDecimal totalFacturado =
                ventaRepository.obtenerTotalFacturado();
                
        // Variacion Mensual
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        java.time.LocalDateTime inicioMesActual = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        java.time.LocalDateTime inicioMesAnterior = inicioMesActual.minusMonths(1);
        java.time.LocalDateTime finMesAnterior = inicioMesActual.minusSeconds(1);
        
        BigDecimal facturadoMesActual = ventaRepository.obtenerTotalFacturadoEntreFechas(inicioMesActual, ahora);
        BigDecimal facturadoMesAnterior = ventaRepository.obtenerTotalFacturadoEntreFechas(inicioMesAnterior, finMesAnterior);
        
        BigDecimal porcentajeVariacionMensual = BigDecimal.ZERO;
        if (facturadoMesAnterior.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeVariacionMensual = facturadoMesActual.subtract(facturadoMesAnterior)
                    .divide(facturadoMesAnterior, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        } else if (facturadoMesActual.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeVariacionMensual = new BigDecimal("100"); // 100% growth if previous was 0
        }

        return new DashboardResponse(

                totalProductos,

                productosActivos,

                stockBajo,

                totalVentas,

                totalFacturado != null ? totalFacturado : BigDecimal.ZERO,
                
                porcentajeVariacionMensual,
                
                ventaRepository.obtenerTopProductos(),
                
                ventaRepository.obtenerVentasPorMetodoPago()

        );

    }

}